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

package software.amazon.smithy.build.transforms;

import java.util.Collections;
import java.util.Set;
import software.amazon.smithy.build.TransformContext;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.loader.Prelude;
import software.amazon.smithy.model.transform.ModelTransformer;

/**
 * {@code includeNamespaces} filters out shapes and trait definitions
 * that are not part of one of the given {@code namespaces}.
 *
 * <p>Note that this does not filter out prelude shapes or namespaces.
 */
public final class IncludeNamespaces extends BackwardCompatHelper<IncludeNamespaces.Config> {

    /**
     * {@code includeNamespaces} configuration.
     */
    public static final class Config {
        private Set<String> namespaces = Collections.emptySet();

        /**
         * @return Gets the list of namespaces to include.
         */
        public Set<String> getNamespaces() {
            return namespaces;
        }

        /**
         * Sets the list of namespaces to include.
         *
         * @param namespaces Namespaces to include.
         */
        public void setNamespaces(Set<String> namespaces) {
            this.namespaces = namespaces;
        }
    }

    @Override
    public Class<Config> getConfigType() {
        return Config.class;
    }

    @Override
    public String getName() {
        return "includeNamespaces";
    }

    @Override
    String getBackwardCompatibleNameMapping() {
        return "namespaces";
    }

    @Override
    protected Model transformWithConfig(TransformContext context, Config config) {
        Set<String> namespaces = config.getNamespaces();
        Model model = context.getModel();
        ModelTransformer transformer = context.getTransformer();
        return transformer.filterShapes(model, shape -> {
            return Prelude.isPreludeShape(shape) || namespaces.contains(shape.getId().getNamespace());
        });
    }
}
