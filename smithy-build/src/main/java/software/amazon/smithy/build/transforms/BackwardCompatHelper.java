/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.smithy.build.transforms;

import java.util.logging.Logger;
import software.amazon.smithy.build.TransformContext;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.ObjectNode;

/**
 * Helper class used to allow older versions of smithy-build.json files to
 * automatically rewrite a list of strings in an object to a member named
 * "__args" that contains the list of strings.
 *
 * <p>For example, the following deprecated JSON:
 *
 * <pre>{@code
 * {
 *     "version": "1.0",
 *     "projections": {
 *         "projection-name": {
 *             "transforms": [
 *                 {
 *                     "name": "transform-name",
 *                     "args": [
 *                         "argument1",
 *                         "argument2"
 *                     ]
 *                 }
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>Is rewritten to the following JSON using {@code ConfigLoader}:
 *
 * <pre>{@code
 * {
 *     "version": "1.0",
 *     "projections": {
 *         "projection-name": {
 *             "transforms": [
 *                 {
 *                     "name": "transform-name",
 *                     "args": {
 *                         "__args": [
 *                             "argument1",
 *                             "argument2"
 *                         ]
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>And this, in turn, uses the result of {@link #getBackwardCompatibleNameMapping()}
 * to rewrite the JSON to the preferred format:
 *
 * <pre>{@code
 * {
 *     "version": "1.0",
 *     "projections": {
 *         "projection-name": {
 *             "transforms": [
 *                 {
 *                     "name": "transform-name",
 *                     "args": {
 *                         "<result of getBackwardCompatibleNameMapping()>": [
 *                             "argument1",
 *                             "argument2"
 *                         ]
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * }</pre>
 *
 * @param <T> Type of configuration object to deserialize into.
 */
abstract class BackwardCompatHelper<T> extends ConfigurableProjectionTransformer<T> {

    private static final Logger LOGGER = Logger.getLogger(BackwardCompatHelper.class.getName());
    private static final String ARGS = "__args";

    /**
     * Gets the name that "__args" is to be rewritten to.
     *
     * @return Returns the name to rewrite.
     */
    abstract String getBackwardCompatibleNameMapping();

    @Override
    public final Model transform(TransformContext context) {
        ObjectNode original = context.getSettings();

        if (!original.getMember(ARGS).isPresent()) {
            return super.transform(context);
        }

        LOGGER.warning(() -> String.format(
                "Deprecated projection transform arguments detected for `%s`; change this list of strings "
                + "to an object with a property named `%s`", getName(), getBackwardCompatibleNameMapping()));

        ObjectNode updated = original.toBuilder()
                .withMember(getBackwardCompatibleNameMapping(), original.getMember(ARGS).get())
                .withoutMember(ARGS)
                .build();

        return super.transform(context.toBuilder().settings(updated).build());
    }
}
