/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.jsonschema;

import java.util.regex.Pattern;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeIndex;
import software.amazon.smithy.model.shapes.SimpleShape;
import software.amazon.smithy.model.traits.EnumTrait;
import software.amazon.smithy.utils.StringUtils;

final class DefaultRefStrategy implements RefStrategy {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\.");
    private static final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[^A-Za-z0-9]");

    private final ShapeIndex index;
    private final boolean alphanumericOnly;
    private final boolean keepNamespaces;
    private final String rootPointer;
    private final PropertyNamingStrategy propertyNamingStrategy;
    private final ObjectNode config;

    DefaultRefStrategy(ShapeIndex index, ObjectNode config, PropertyNamingStrategy propertyNamingStrategy) {
        this.index = index;
        this.propertyNamingStrategy = propertyNamingStrategy;
        this.config = config;
        rootPointer = computePointer(config);
        alphanumericOnly = config.getBooleanMemberOrDefault(JsonSchemaConstants.ALPHANUMERIC_ONLY_REFS);
        keepNamespaces = config.getBooleanMemberOrDefault(JsonSchemaConstants.KEEP_NAMESPACES);
    }

    private static String computePointer(ObjectNode config) {
        String pointer = config.getStringMemberOrDefault(JsonSchemaConstants.DEFINITION_POINTER, DEFAULT_POINTER);
        if (!pointer.endsWith("/")) {
            pointer += "/";
        }
        return pointer;
    }

    @Override
    public String toPointer(ShapeId id) {
        String inlinedPointer = computeInlinedMemberPointer(id);

        if (inlinedPointer != null) {
            return inlinedPointer;
        }

        StringBuilder builder = new StringBuilder();
        appendNamespace(builder, id);
        builder.append(id.getName());

        // If we ever *have* to generate custom types for members (for example,
        // if a type-refining trait is added to a member that affects how a
        // structure, union, list, set, or map are serialized, then we can
        // expand this to generate good synthesized member names.
        id.getMember().ifPresent(memberName -> {
            throw new SmithyJsonSchemaException("Cannot generate a pointer for a member: " + id);
        });

        return rootPointer + stripNonAlphaNumericCharsIfNecessary(builder.toString());
    }

    @Override
    public boolean isInlined(Shape shape) {
        // We could add more logic here in the future if needed to account for
        // member shapes that absolutely must generate a synthesized schema.
        if (shape.asMemberShape().isPresent()) {
            MemberShape member = shape.asMemberShape().get();
            Shape target = index.getShape(member.getTarget())
                    .orElseThrow(() -> new SmithyJsonSchemaException("Invalid member target: " + member));
            return isInlined(target);
        }

        return shape instanceof SimpleShape && !shape.hasTrait(EnumTrait.class);
    }

    private String computeInlinedMemberPointer(ShapeId id) {
        if (!id.getMember().isPresent()) {
            return null;
        }

        MemberShape memberShape = index.getShape(id).flatMap(Shape::asMemberShape)
                .orElseThrow(() -> new SmithyJsonSchemaException("Invalid member ID: " + id));

        if (!isInlined(memberShape)) {
            return null;
        }

        Shape container = index.getShape(memberShape.getContainer())
                .orElseThrow(() -> new SmithyJsonSchemaException("Invalid member: " + memberShape.getContainer()));
        String parentPointer = toPointer(id.withoutMember());

        switch (container.getType()) {
            case LIST:
            case SET:
                return parentPointer + "/items";
            case MAP:
                return memberShape.getMemberName().equals("key")
                       ? parentPointer + "/propertyNames"
                       : parentPointer + "/additionalProperties";
            default: // union | structure
                return parentPointer + "/properties/" + propertyNamingStrategy.toPropertyName(
                        container, memberShape, config);
        }
    }

    private void appendNamespace(StringBuilder builder, ShapeId id) {
        // Append each namespace part, capitalizing each segment.
        // For example, "smithy.example" becomes "SmithyExample".
        if (keepNamespaces) {
            for (String part : SPLIT_PATTERN.split(id.getNamespace())) {
                builder.append(StringUtils.capitalize(part));
            }
        }
    }

    private String stripNonAlphaNumericCharsIfNecessary(String result) {
        return alphanumericOnly
               ? NON_ALPHA_NUMERIC.matcher(result).replaceAll("")
               : result;
    }
}
