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

package software.amazon.smithy.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IoUtilsTest {
    @Test
    public void testArrayEmptyByteArray() throws Exception {
        byte[] s = IoUtils.toByteArray(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, s.length);
    }

    @Test
    public void testArrayZeroByteStream() throws Exception {
        byte[] s = IoUtils.toByteArray(new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        });
        assertEquals(0, s.length);
    }

    @Test
    public void testStringEmptyByteArray() throws Exception {
        String s = IoUtils.toUtf8String(new ByteArrayInputStream(new byte[0]));
        assertEquals("", s);
    }

    @Test
    public void testStringZeroByteStream() throws Exception {
        String s = IoUtils.toUtf8String(new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        });
        assertEquals("", s);
    }

    @Test
    public void readsFromStringPath() throws Exception {
        // Windows doesn't like the result of URL#getPath, so to test this
        // we create a Path from the URI, convert that to a string, then pass
        // it to the helper method which uses Paths.get again.
        assertTrue(IoUtils.readUtf8File(Paths.get(getClass().getResource("test.txt").toURI()).toString())
                .contains("This is a test."));
    }

    @Test
    public void readsFromPath() throws URISyntaxException {
        assertTrue(IoUtils.readUtf8File(Paths.get(getClass().getResource("test.txt").toURI()))
                .contains("This is a test."));
    }

    @Test
    public void readsFromClass() {
        assertTrue(IoUtils.readUtf8Resource(getClass(), "test.txt").contains("This is a test."));
    }

    @Test
    public void readsFromClassLoader() {
        assertTrue(IoUtils.readUtf8Resource(getClass().getClassLoader(), "software/amazon/smithy/utils/test.txt")
                .contains("This is a test."));
    }

    @Test
    public void throwsWhenProcessFails() {
        RuntimeException e = Assertions.assertThrows(RuntimeException.class, () -> {
            IoUtils.runCommand("thisCommandDoesNotExist" + new Random().nextInt(1000));
        });

        assertThat(e.getMessage(), containsString("failed with exit code"));
    }

    @Test
    public void doesNotThrowWhenGivenOutput() {
        StringBuilder sb = new StringBuilder();
        String name = "thisCommandDoesNotExist" + new Random().nextInt(1000);
        int code = IoUtils.runCommand(name, Paths.get(System.getProperty("user.dir")), sb);

        assertThat(code, not(0));
        assertThat(sb.toString(), not(emptyString()));
    }
}
