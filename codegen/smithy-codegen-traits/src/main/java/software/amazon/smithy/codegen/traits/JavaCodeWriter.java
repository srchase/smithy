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

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import software.amazon.smithy.utils.CodeWriter;

/**
 * Simple code writer used to emit Java code.
 */
class JavaCodeWriter extends CodeWriter {

    private final String packageName;
    private static final Set<Class> imports = new TreeSet<>(Comparator.comparing(Class::getCanonicalName));

    JavaCodeWriter(String packageName) {
        this.packageName = Objects.requireNonNull(packageName);

        putFormatter('T', (klass, indent) -> {
            Class<?> value = klass instanceof Class ? (Class<?>) klass : klass.getClass();
            imports.add(value);
            return value.getSimpleName();
        });

        trimTrailingSpaces(true);
    }

    JavaCodeWriter javadoc(Runnable runnable) {
        pushState("javadoc");
        write("/**");
        setNewlinePrefix(" * ");
        runnable.run();
        setNewlinePrefix("");
        write(" */");
        popState();
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("package ").append(packageName).append("\n\n");

        if (!imports.isEmpty()) {
            for (Class<?> klass : imports) {
                result.append("import ").append(klass.getCanonicalName()).append(";\n");
            }

            result.append("\n");
        }

        result.append(super.toString());
        return result.toString();
    }
}
