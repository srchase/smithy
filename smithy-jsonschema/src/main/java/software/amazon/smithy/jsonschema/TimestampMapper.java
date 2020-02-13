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

import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.TimestampFormatTrait;

/**
 * Updates builders based on timestamp shapes, timestampFormat traits, and
 * the value of {@link JsonSchemaConstants#DEFAULT_TIMESTAMP_FORMAT}.
 */
final class TimestampMapper implements JsonSchemaMapper {
    private static final String DEFAULT_TIMESTAMP_FORMAT = TimestampFormatTrait.DATE_TIME;

    @Override
    public byte getOrder() {
        return -120;
    }

    @Override
    public Schema.Builder updateSchema(Shape shape, Schema.Builder builder, ObjectNode config) {
        String format = extractTimestampFormat(shape, config);

        if (format == null) {
            return builder;
        }

        switch (format) {
            case TimestampFormatTrait.DATE_TIME:
                return builder.type("string").format(format);
            case TimestampFormatTrait.HTTP_DATE:
                return builder.type("string");
            case TimestampFormatTrait.EPOCH_SECONDS:
                return builder.format(null).type("number");
            default:
                // Handle any future "epoch-*" formats like epoch-millis.
                return format.startsWith("epoch-") ? builder.type("number") : builder.type("string");
        }
    }

    private static String extractTimestampFormat(Shape shape, ObjectNode config) {
        if (shape.isTimestampShape() || shape.hasTrait(TimestampFormatTrait.class)) {
            return shape.getTrait(TimestampFormatTrait.class)
                    .map(TimestampFormatTrait::getValue)
                    .orElseGet(() -> config.getStringMemberOrDefault(
                            JsonSchemaConstants.DEFAULT_TIMESTAMP_FORMAT,
                            DEFAULT_TIMESTAMP_FORMAT));
        }

        return null;
    }
}
