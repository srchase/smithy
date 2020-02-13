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
import static org.hamcrest.Matchers.not;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.utils.SetUtils;

public class SchemaTest {
    /**
     * This is a basic integration test that makes sure each disable setting
     * can be called and doesn't break the builder.
     */
    @Test
    public void canRemoveSettings() {
        Schema.Builder builder = Schema.builder();
        Set<String> values = SetUtils.of(
                JsonSchemaConstants.DISABLE_CONTENT_MEDIA_TYPE,
                JsonSchemaConstants.DISABLE_ADDITIONAL_PROPERTIES,
                JsonSchemaConstants.DISABLE_ALL_OF,
                JsonSchemaConstants.DISABLE_ANY_OF,
                JsonSchemaConstants.DISABLE_COMMENT,
                JsonSchemaConstants.DISABLE_CONST,
                JsonSchemaConstants.DISABLE_CONTENT_ENCODING,
                JsonSchemaConstants.DISABLE_DEFAULT,
                JsonSchemaConstants.DISABLE_DESCRIPTION,
                JsonSchemaConstants.DISABLE_ENUM,
                JsonSchemaConstants.DISABLE_EXAMPLES,
                JsonSchemaConstants.DISABLE_EXCLUSIVE_MAXIMUM,
                JsonSchemaConstants.DISABLE_EXCLUSIVE_MINIMUM,
                JsonSchemaConstants.DISABLE_FORMAT,
                JsonSchemaConstants.DISABLE_ITEMS,
                JsonSchemaConstants.DISABLE_MAX_ITEMS,
                JsonSchemaConstants.DISABLE_MAX_LENGTH,
                JsonSchemaConstants.DISABLE_MAX_PROPERTIES,
                JsonSchemaConstants.DISABLE_MAXIMUM,
                JsonSchemaConstants.DISABLE_MIN_ITEMS,
                JsonSchemaConstants.DISABLE_MIN_LENGTH,
                JsonSchemaConstants.DISABLE_MIN_PROPERTIES,
                JsonSchemaConstants.DISABLE_MINIMUM,
                JsonSchemaConstants.DISABLE_MULTIPLE_OF,
                JsonSchemaConstants.DISABLE_NOT,
                JsonSchemaConstants.DISABLE_ONE_OF,
                JsonSchemaConstants.DISABLE_PATTERN,
                JsonSchemaConstants.DISABLE_PROPERTIES,
                JsonSchemaConstants.DISABLE_PROPERTY_NAMES,
                JsonSchemaConstants.DISABLE_READ_ONLY,
                JsonSchemaConstants.DISABLE_REQUIRED,
                JsonSchemaConstants.DISABLE_TITLE,
                JsonSchemaConstants.DISABLE_UNIUQE_ITEMS,
                JsonSchemaConstants.DISABLE_WRITE_ONLY
        );

        for (String value : values) {
            builder.disableProperty(value);
        }

        builder.build();
    }

    @Test
    public void basicEqualityTest() {
        Schema a = Schema.builder().title("foo").build();
        Schema b = Schema.builder().title("foo").build();
        Schema c = Schema.builder().type("string").build();

        assertThat(a, equalTo(b));
        assertThat(a, equalTo(a));
        assertThat(b, equalTo(a));
        assertThat(c, not(equalTo(a)));
    }

    @Test
    public void getsAllOfSelector() {
        Schema subschema = Schema.builder().type("string").build();
        Schema schema = Schema.builder().allOf(Collections.singletonList(subschema)).build();

        assertThat(schema.selectSchema("allOf", "0"), equalTo(Optional.of(subschema)));
        assertThat(schema.selectSchema("allOf"), equalTo(Optional.empty()));
    }

    @Test
    public void getsOneOfSelector() {
        Schema subschema = Schema.builder().type("string").build();
        Schema schema = Schema.builder().oneOf(Collections.singletonList(subschema)).build();

        assertThat(schema.selectSchema("oneOf", "0"), equalTo(Optional.of(subschema)));
        assertThat(schema.selectSchema("oneOf"), equalTo(Optional.empty()));
    }

    @Test
    public void getsAnyOfSelector() {
        Schema subschema = Schema.builder().type("string").build();
        Schema schema = Schema.builder().anyOf(Collections.singletonList(subschema)).build();

        assertThat(schema.selectSchema("anyOf", "0"), equalTo(Optional.of(subschema)));
        assertThat(schema.selectSchema("anyOf"), equalTo(Optional.empty()));
    }

    @Test
    public void throwsWhenInvalidPositionGiven() {
        Schema subschema = Schema.builder().type("string").build();
        Schema schema = Schema.builder().anyOf(Collections.singletonList(subschema)).build();

        Assertions.assertThrows(SmithyJsonSchemaException.class, () -> {
            schema.selectSchema("anyOf", "foo");
        });
    }

    @Test
    public void getsPropertyNamesSelector() {
        Schema propertyNames = Schema.builder().type("string").build();
        Schema schema = Schema.builder().propertyNames(propertyNames).build();

        assertThat(schema.selectSchema("propertyNames"), equalTo(Optional.of(propertyNames)));
    }

    @Test
    public void getsItemsSelector() {
        Schema items = Schema.builder().type("string").build();
        Schema schema = Schema.builder().items(items).build();

        assertThat(schema.selectSchema("items"), equalTo(Optional.of(items)));
    }

    @Test
    public void doesNotThrowOnInvalidPropertySelector() {
        Schema schema = Schema.builder().putProperty("foo", Schema.builder().type("string").build()).build();

        assertThat(schema.selectSchema("properties"), equalTo(Optional.empty()));
    }

    @Test
    public void getsAdditionalPropertiesSelector() {
        Schema items = Schema.builder().type("string").build();
        Schema schema = Schema.builder().not(items).build();

        assertThat(schema.selectSchema("not"), equalTo(Optional.of(items)));
    }

    @Test
    public void getsNotSelector() {
        Schema items = Schema.builder().type("string").build();
        Schema schema = Schema.builder().additionalProperties(items).build();

        assertThat(schema.selectSchema("additionalProperties"), equalTo(Optional.of(items)));
    }

    @Test
    public void ignoresUnsupportedProperties() {
        Schema schema = Schema.builder().build();

        assertThat(schema.selectSchema("foof", "doof"), equalTo(Optional.empty()));
    }

    @Test
    public void ignoresNegativeNumericPositions() {
        Schema subschema = Schema.builder().type("string").build();
        Schema schema = Schema.builder().allOf(Collections.singletonList(subschema)).build();

        assertThat(schema.selectSchema("allOf", "-1"), equalTo(Optional.empty()));
    }
}
