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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import software.amazon.smithy.model.shapes.CollectionShape;
import software.amazon.smithy.model.shapes.MapShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeIndex;
import software.amazon.smithy.model.shapes.SimpleShape;
import software.amazon.smithy.model.traits.EnumTrait;

/**
 * Automatically de-conflicts simple shapes, map shapes, list shapes,
 * and set shapes by sorting conflicting shapes by ID and then appending
 * an automatically incrementing number to the end of the shape.
 *
 * <p>String shapes marked with the enum trait are never allowed to
 * conflict since they can easily drift away from compatibility over
 * time. Structures and unions are not allowed to conflict either.
 *
 * <p>Simple types that have the exact same traits and do not have
 * an enum trait are elided into the same JSON schema definition.
 */
final class DeconflictingStrategy implements RefStrategy {

    private static final Logger LOGGER = Logger.getLogger(DeconflictingStrategy.class.getName());

    private final RefStrategy delegate;
    private final Map<ShapeId, String> pointers = new HashMap<>();
    private final Map<String, ShapeId> reversePointers = new HashMap<>();

    DeconflictingStrategy(ShapeIndex index, RefStrategy delegate) {
        this.delegate = delegate;

        // Pre-compute a map of all converted shape refs. Sort the shapes
        // to make the result deterministic.
        index.shapes().sorted().forEach(shape -> {
            if (isIgnoredShape(shape)) {
                return;
            }

            String pointer = delegate.toPointer(shape.getId());
            if (isOkToPutShape(index, shape.getId(), pointer)) {
                pointers.put(shape.getId(), pointer);
                reversePointers.put(pointer, shape.getId());
            } else {
                String deconflictedPointer = deconflict(shape, pointer, reversePointers);
                LOGGER.info(() -> String.format(
                        "De-conflicted `%s` JSON schema pointer from `%s` to `%s`",
                        shape.getId(), pointer, deconflictedPointer));
                pointers.put(shape.getId(), deconflictedPointer);
                reversePointers.put(deconflictedPointer, shape.getId());
            }
        });
    }

    // Some shapes aren't converted to JSON schema at all because they
    // don't have a corresponding definition.
    private boolean isIgnoredShape(Shape shape) {
        return shape.isResourceShape() || shape.isServiceShape() || shape.isOperationShape() || shape.isMemberShape();
    }

    private String deconflict(Shape shape, String pointer, Map<String, ShapeId> reversePointers) {
        LOGGER.info(() -> String.format(
                "Attempting to de-conflict `%s` JSON schema pointer `%s` that conflicts with `%s`",
                shape.getId(), pointer, reversePointers.get(pointer)));

        if (!isSafeToDeconflict(shape)) {
            throw new ConflictingShapeNameException(String.format(
                    "Shape %s conflicts with %s using a JSON schema pointer of %s",
                    shape, reversePointers.get(pointer), pointer));
        }

        // Create a de-conflicted JSON schema pointer that just appends an incrementing
        // number until there are no conflicts. Note that this requires a sorted
        // list of shapes from the start in order to not result in chaotic diffs.
        for (int i = 2; ; i++) {
            String incrementedPointer = pointer + i;
            if (!reversePointers.containsKey(incrementedPointer)) {
                return incrementedPointer;
            }
        }
    }

    private boolean isOkToPutShape(ShapeIndex index, ShapeId id, String pointer) {
        // If there's no conflict, then do nothing.
        if (!reversePointers.containsKey(pointer)) {
            return true;
        }

        // Grab the two conflicting shapes for comparison.
        Shape shape = index.getShape(id)
                .orElseThrow(() -> new SmithyJsonSchemaException("Invalid shape ID: " + id));
        Shape resolvedShape = index.getShape(reversePointers.get(pointer))
                .orElseThrow(() -> new SmithyJsonSchemaException("Invalid shape ID: " + reversePointers.get(pointer)));

        // If we cannot elide the shapes into a single definition, then
        // it is not ok to add the shape to the mappings, and we need
        // to try to de-conflict them.
        if (!canShapesBeElidedIntoSingleShape(resolvedShape, shape)) {
            return false;
        }

        LOGGER.fine(() -> String.format(
                "Ignoring JSON schema shape name conflict between %s and %s because they are equivalent. The "
                + "resulting JSON schema document will refer to a single type even though the Smithy model referred "
                + "to multiple types.",
                resolvedShape, shape));

        return true;
    }

    // Determines if the two given conflicting shapes are equivalent enough
    // and safe enough to elide into a single JSON schema definition.
    // We only do this for shapes that are of simple types, have the
    // same traits, and don't have an enum trait.
    private boolean canShapesBeElidedIntoSingleShape(Shape a, Shape b) {
        // The shapes must be the same simple type.
        if (a.getType() != b.getType() || !(a instanceof SimpleShape)) {
            LOGGER.fine(() -> String.format(
                    "Shape %s conflicts with %s because they are not both simple shapes of the same type", a, b));
            return false;
        }

        // neither shape can have an enum trait.
        if (a.hasTrait(EnumTrait.class)) {
            LOGGER.fine(() -> String.format("Shape %s conflicts with %s because of an enum trait", a, b));
            return false;
        }

        // both shapes must have the same exact traits.
        if (!a.getAllTraits().equals(b.getAllTraits())) {
            LOGGER.fine(() -> String.format("Shape %s conflicts with %s because of differing traits", a, b));
            return false;
        }

        return true;
    }

    // We only want to de-conflict shapes that are generally not code-generated
    // because the de-conflicts names can potentially change over time as shapes
    // are added and removed. Things like structures, unions, and enums should
    // never be de-conflicted from this class. Note that at this point, it's
    // already been determined that the shapes in question are not equivalent
    // (allowed to be elided into a single JSON schema type).
    private boolean isSafeToDeconflict(Shape shape) {
        return !shape.hasTrait(EnumTrait.class)
               && (shape instanceof SimpleShape || shape instanceof CollectionShape || shape instanceof MapShape);
    }

    @Override
    public String toPointer(ShapeId id) {
        return pointers.computeIfAbsent(id, delegate::toPointer);
    }

    @Override
    public boolean isInlined(Shape shape) {
        return delegate.isInlined(shape);
    }
}
