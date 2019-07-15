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

package software.amazon.smithy.codegen.traits;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.StringShape;
import software.amazon.smithy.model.traits.StringTrait;
import software.amazon.smithy.model.traits.TraitDefinition;
import software.amazon.smithy.utils.StringUtils;

/**
 * Generates traits that target a string.
 *
 * <p>TODO: Special handling for enums?
 */
final class StringTraitGenerator implements TraitGenerator {
    private TraitDefinition definition;
    private StringShape shape;

    StringTraitGenerator(TraitDefinition definition, StringShape shape) {
        this.definition = definition;
        this.shape = shape;
    }

    @Override
    public TraitDefinition getTraitDefinition() {
        return definition;
    }

    @Override
    public String getCode() {
        String className = getClassName();
        JavaCodeWriter writer = new JavaCodeWriter(getPackageName());

        definition.getDocumentation().ifPresent(docs -> {
            String formattedDocs = StringUtils.wrap(docs, 74);
            writer.javadoc(() -> writer.write(formattedDocs));
        });

        writer.openBlock("public final class $L extends $T {", className, StringTrait.class)
                .write("public static final String NAME = $S;", getTraitDefinition().getFullyQualifiedName())
                .write()
                .openBlock("public $L(String value, $T sourceLocation) {", className, SourceLocation.class)
                    .write("super(NAME, value, sourceLocation);")
                .closeBlock("}")
                .write()
                .openBlock("public $L(String value) {", className)
                    .write("this(value, $T.NONE);", SourceLocation.class)
                .closeBlock("}")
                .write()
                .openBlock("public static final class Provider extends $T.Provider<$L> {", className, StringTrait.class)
                    .openBlock("public Provider() {")
                        .write("super(NAME, $L::new);", className)
                    .closeBlock("}")
                .closeBlock("}")
                .closeBlock("}");

        return writer.toString();
    }
}
