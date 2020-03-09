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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import software.amazon.smithy.build.TransformContext;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.loader.Prelude;
import software.amazon.smithy.model.transform.ModelTransformer;

/**
 * {@code includeShapesByTag} removes shapes and trait definitions
 * that are not tagged with at least one of the tags provided
 * in the {@code tags} argument.
 *
 * <p>Prelude shapes are not removed by this transformer.
 */
public final class IncludeShapesByTag extends BackwardCompatHelper<IncludeShapesByTag.Config> {

    /**
     * {@code includeShapesByTag} configuration.
     */
    public static final class Config {
        private Set<String> tags = Collections.emptySet();

        /**
         * Gets the set of tags that cause shapes to be included.
         *
         * @return Returns the inclusion tags.
         */
        public Set<String> getTags() {
            return tags;
        }

        /**
         * Sets the set of tags that cause shapes to be included.
         *
         * @param tags Tags that cause shapes to be included.
         */
        public void setTags(Set<String> tags) {
            this.tags = tags;
        }
    }

    @Override
    public Class<Config> getConfigType() {
        return Config.class;
    }

    @Override
    public String getName() {
        return "includeShapesByTag";
    }

    @Override
    public Collection<String> getAliases() {
        return Collections.singleton("includeByTag");
    }

    @Override
    String getBackwardCompatibleNameMapping() {
        return "tags";
    }

    @Override
    protected Model transformWithConfig(TransformContext context, Config config) {
        Set<String> includeTags = config.getTags();
        ModelTransformer transformer = context.getTransformer();
        Model model = context.getModel();
        return transformer.filterShapes(model, shape -> {
            return Prelude.isPreludeShape(shape) || shape.getTags().stream().anyMatch(includeTags::contains);
        });
    }
}
