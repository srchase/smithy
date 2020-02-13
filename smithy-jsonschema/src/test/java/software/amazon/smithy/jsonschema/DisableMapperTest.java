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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.StringShape;

public class DisableMapperTest {
    @Test
    public void removesDisabledKeywords() {
        StringShape shape = StringShape.builder().id("smithy.example#String").build();
        Schema.Builder builder = Schema.builder().type("string").format("foo");
        ObjectNode config = Node.objectNodeBuilder()
                .withMember(JsonSchemaConstants.DISABLE_FORMAT, true)
                .build();
        Schema schema = new DisableMapper().updateSchema(shape, builder, config).build();

        assertThat(schema.getType().get(), equalTo("string"));
        assertFalse(schema.getFormat().isPresent());
    }
}
