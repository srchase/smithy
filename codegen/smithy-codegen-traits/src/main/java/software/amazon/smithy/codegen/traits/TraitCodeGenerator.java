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

import java.util.Objects;
import java.util.function.Predicate;
import software.amazon.smithy.build.FileManifest;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.traits.TraitDefinition;
import software.amazon.smithy.utils.SmithyBuilder;

/**
 * TODO: could possibly be removed. Meant to function as the entry point of generating all traits in a model.
 */
public final class TraitCodeGenerator {
    private final Model model;
    private final FileManifest manifest;
    private final Predicate<TraitDefinition> predicate;

    private TraitCodeGenerator(Builder builder) {
        this.model = SmithyBuilder.requiredState("model", builder.model);
        this.manifest = SmithyBuilder.requiredState("manifest", builder.manifest);
        this.predicate = builder.predicate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void generate() {
        throw new UnsupportedOperationException("TODO");
    }

    public static final class Builder implements SmithyBuilder<TraitCodeGenerator> {
        private Model model;
        private FileManifest manifest;
        private Predicate<TraitDefinition> predicate = def -> true;

        private Builder() {}

        @Override
        public TraitCodeGenerator build() {
            return new TraitCodeGenerator(this);
        }

        public Builder model(Model model) {
            this.model = model;
            return this;
        }

        public Builder manifest(FileManifest manifest) {
            this.manifest = manifest;
            return this;
        }

        public Builder predicate(Predicate<TraitDefinition> predicate) {
            this.predicate = Objects.requireNonNull(predicate);
            return this;
        }
    }
}
