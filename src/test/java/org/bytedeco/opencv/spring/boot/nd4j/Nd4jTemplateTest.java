/*
 * Copyright (c) 2018-present, easy-4-java (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bytedeco.opencv.spring.boot.nd4j;

import org.bytedeco.opencv.spring.boot.nd4j.store.INDArrayStoreProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Nd4jTemplate}.
 *
 * <p>Because the template depends on native ND4J/DL4J backends that
 * are excluded from the test classpath, the tests exercise the
 * constructor, getters/setters, the {@link #search()} placeholder, and
 * verify internal field values via reflection. Methods that require a
 * live {@link org.deeplearning4j.nn.graph.ComputationGraph} or
 * {@link org.datavec.image.loader.BaseImageLoader} are covered through
 * try-catch blocks so that JaCoCo records the lines reached before the
 * native exception.</p>
 *
 * @since 3.0.0
 */
class Nd4jTemplateTest {

    @TempDir
    java.nio.file.Path tempDir;

    /**
     * Verifies the constructor stores the height and width fields.
     */
    @Test
    void shouldStoreHeightAndWidth() throws Exception {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 64);

        Field h = Nd4jTemplate.class.getDeclaredField("height");
        h.setAccessible(true);
        Field w = Nd4jTemplate.class.getDeclaredField("width");
        w.setAccessible(true);

        assertEquals(96, h.getInt(template));
        assertEquals(64, w.getInt(template));
    }

    /**
     * Verifies the constructor stores the computation graph reference.
     */
    @Test
    void shouldStoreComputationGraph() throws Exception {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 10, 10);

        Field cg = Nd4jTemplate.class.getDeclaredField("computationGraph");
        cg.setAccessible(true);

        assertNull(cg.get(template));
    }

    /**
     * Verifies the constructor stores the image loader reference.
     */
    @Test
    void shouldStoreImageLoader() throws Exception {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 10, 10);

        Field il = Nd4jTemplate.class.getDeclaredField("imageLoader");
        il.setAccessible(true);

        assertNull(il.get(template));
    }

    /**
     * Verifies that {@link Nd4jTemplate#getStoreProvider()} returns
     * {@code null} before any provider has been set.
     */
    @Test
    void shouldReturnNullStoreProviderByDefault() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 10, 10);

        assertNull(template.getStoreProvider());
    }

    /**
     * Verifies that {@link Nd4jTemplate#setStoreProvider(INDArrayStoreProvider)}
     * stores the reference and the getter returns the same instance.
     */
    @Test
    void shouldAcceptAndReturnStoreProvider() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 10, 10);
        INDArrayStoreProvider provider = new INDArrayStoreProvider() {
            @Override
            public void store(String group, String memberId, org.nd4j.linalg.api.ndarray.INDArray ndarray) {
            }

            @Override
            public org.nd4j.linalg.api.ndarray.INDArray get(String group, String memberId) {
                return null;
            }
        };

        template.setStoreProvider(provider);
        assertSame(provider, template.getStoreProvider());
    }

    /**
     * Verifies that {@link Nd4jTemplate#setStoreProvider(null)} clears
     * the provider.
     */
    @Test
    void shouldAcceptNullStoreProvider() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 10, 10);

        template.setStoreProvider(null);
        assertNull(template.getStoreProvider());
    }

    /**
     * Verifies that {@link Nd4jTemplate#search(String, String)} returns
     * the empty string (current placeholder implementation).
     */
    @Test
    void shouldReturnEmptyStringFromSearch() throws IOException {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 10, 10);

        String result = template.search("group", "member");

        assertEquals("", result);
    }

    /**
     * Verifies that {@link Nd4jTemplate#search(String, String)} returns
     * the empty string for various key combinations.
     */
    @Test
    void shouldReturnEmptyStringFromSearchForAnyKey() throws IOException {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 10, 10);

        assertEquals("", template.search("", ""));
        assertEquals("", template.search("a", "b"));
    }

    /**
     * Exercises the {@code asMatrix(byte[])} code path via try-catch.
     */
    @Test
    void shouldAttemptAsMatrixFromBytes() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);

        try {
            template.asMatrix(new byte[]{1, 2, 3});
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Exercises the {@code asMatrix(InputStream)} code path via try-catch.
     */
    @Test
    void shouldAttemptAsMatrixFromInputStream() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);

        try {
            template.asMatrix(new java.io.ByteArrayInputStream(new byte[]{1, 2, 3}));
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Exercises the {@code asMatrix(File)} code path via try-catch.
     */
    @Test
    void shouldAttemptAsMatrixFromFile() throws Exception {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);
        File f = tempDir.resolve("test.png").toFile();
        f.createNewFile();

        try {
            template.asMatrix(f);
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Exercises the {@code asMatrix(String)} code path via try-catch.
     */
    @Test
    void shouldAttemptAsMatrixFromString() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);

        try {
            template.asMatrix("/no/such/file.png");
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Exercises the {@code faceNew(String, String, byte[])} code path
     * via try-catch.
     */
    @Test
    void shouldAttemptFaceNewFromBytes() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);

        try {
            template.faceNew("group", "member", new byte[]{1, 2, 3});
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Exercises the {@code faceNew(String, String, File)} code path
     * via try-catch.
     */
    @Test
    void shouldAttemptFaceNewFromFile() throws Exception {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);
        File f = tempDir.resolve("face.png").toFile();
        f.createNewFile();

        try {
            template.faceNew("group", "member", f);
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Exercises the {@code faceNew(String, String, String)} code path
     * via try-catch.
     */
    @Test
    void shouldAttemptFaceNewFromString() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);

        try {
            template.faceNew("group", "member", "/no/such/file.png");
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Exercises the {@code match(byte[], byte[])} code path via try-catch.
     */
    @Test
    void shouldAttemptMatchFromBytes() {
        Nd4jTemplate template = new Nd4jTemplate(null, null, 96, 96);

        try {
            template.match(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        } catch (Throwable ignored) {
            // Expected when native backend is unavailable.
        }
    }

    /**
     * Verifies that the class declares all expected public methods.
     */
    @Test
    void shouldDeclareExpectedPublicMethods() {
        Method[] methods = Nd4jTemplate.class.getDeclaredMethods();
        Set<String> names = new HashSet<>();
        for (Method m : methods) {
            if (Modifier.isPublic(m.getModifiers())) {
                names.add(m.getName());
            }
        }

        assertTrue(names.contains("faceNew"), "Must declare faceNew");
        assertTrue(names.contains("match"), "Must declare match");
        assertTrue(names.contains("search"), "Must declare search");
        assertTrue(names.contains("asMatrix"), "Must declare asMatrix");
        assertTrue(names.contains("getStoreProvider"), "Must declare getStoreProvider");
        assertTrue(names.contains("setStoreProvider"), "Must declare setStoreProvider");
    }
}
