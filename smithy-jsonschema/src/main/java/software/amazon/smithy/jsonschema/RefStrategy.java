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

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;

/**
 * Defines a strategy for converting Shape IDs to JSON schema $ref values.
 *
 * <p>This API is currently package-private, but could be exposed in the
 * future if we *really* need to. Ideally we don't.
 */
interface RefStrategy {
    String DEFAULT_POINTER = "#/definitions";

    /**
     * Given a shape ID, returns the value used in a $ref to refer to it.
     *
     * <p>The return value is expected to be a JSON pointer.
     *
     * @param id Shape ID to convert to a $ref string.
     * @return Returns the $ref string (e.g., "#/responses/MyShape").
     */
    String toPointer(ShapeId id);

    /**
     * Returns true if the given shape should be inlined into
     * its container or if the shape should be a ref.
     *
     * @param shape Shape to check.
     * @return Returns true if this shape should be inlined.
     */
    boolean isInlined(Shape shape);

    /**
     * Creates a default strategy for converting shape IDs to $refs.
     *
     * <p>This default strategy will make the created value consist
     * of only alphanumeric characters. When a namespace is included
     * (because "stripNamespaces" is not set), the namespace is added
     * to the beginning of the created name by capitalizing the first
     * letter of each part of the namespace, removing the "."
     * (for example, "smithy.example" becomes "SmithyExample"). Next,
     * the shape name is appended.
     *
     * <p>For example, given the following shape ID "smithy.example#Foo",
     * the following ref is created "#/definitions/SmithyExampleFoo".
     *
     * <p>This implementation honors the value configured in
     * {@link JsonSchemaConstants#DEFINITION_POINTER} to create a $ref
     * pointer to a shape.
     *
     * @param model Model being converted.
     * @param config Conversion configuration.
     * @param propertyNamingStrategy Property naming strategy.
     * @return Returns the created strategy.
     */
    static RefStrategy createDefaultStrategy(
            Model model,
            ObjectNode config,
            PropertyNamingStrategy propertyNamingStrategy
    ) {
        RefStrategy delegate = new DefaultRefStrategy(model, config, propertyNamingStrategy);
        return new DeconflictingStrategy(model, delegate);
    }
}
