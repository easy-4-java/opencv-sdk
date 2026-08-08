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
package org.bytedeco.opencv.spring.boot.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ImageInfo}.
 *
 * <p>The class is primarily a Lombok-generated value object, so the tests
 * verify that getters / setters and the Lombok-generated
 * {@code equals}, {@code hashCode} and {@code toString} methods behave
 * correctly.</p>
 *
 * @since 3.0.0
 */
class ImageInfoTest {

    /**
     * Verifies the no-arg constructor leaves every field {@code null}
     * and that subsequent setters populate the instance.
     */
    @Test
    void shouldStartEmptyAndAcceptSetters() {
        ImageInfo info = new ImageInfo();

        assertNull(info.getImageData());
        assertNull(info.getWidth());
        assertNull(info.getHeight());
        assertNull(info.getImageFormat());

        byte[] data = new byte[] { 1, 2, 3 };
        info.setImageData(data);
        info.setWidth(640);
        info.setHeight(480);
        info.setImageFormat(ImageFormat.CP_PAF_BGR24);

        assertArrayEquals(data, info.getImageData());
        assertEquals(Integer.valueOf(640), info.getWidth());
        assertEquals(Integer.valueOf(480), info.getHeight());
        assertEquals(ImageFormat.CP_PAF_BGR24, info.getImageFormat());
    }

    /**
     * Verifies that two {@link ImageInfo} instances with identical
     * field values are considered equal and produce the same hash code.
     */
    @Test
    void shouldBeEqualWhenFieldsMatch() {
        byte[] data = new byte[] { 9, 8, 7 };
        ImageInfo a = new ImageInfo();
        a.setImageData(data);
        a.setWidth(100);
        a.setHeight(50);
        a.setImageFormat(ImageFormat.CP_PAF_GRAY);

        ImageInfo b = new ImageInfo();
        b.setImageData(data);
        b.setWidth(100);
        b.setHeight(50);
        b.setImageFormat(ImageFormat.CP_PAF_GRAY);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * Verifies that a different {@link ImageFormat} invalidates equality.
     */
    @Test
    void shouldNotBeEqualWhenFormatDiffers() {
        ImageInfo a = new ImageInfo();
        a.setImageData(new byte[] { 0 });
        a.setWidth(10);
        a.setHeight(10);
        a.setImageFormat(ImageFormat.CP_PAF_GRAY);

        ImageInfo b = new ImageInfo();
        b.setImageData(new byte[] { 0 });
        b.setWidth(10);
        b.setHeight(10);
        b.setImageFormat(ImageFormat.CP_PAF_BGR24);

        assertNotEquals(a, b);
    }

    /**
     * Verifies that the setter for the byte array keeps the same
     * reference (no defensive copy is performed by Lombok).
     */
    @Test
    void shouldExposeSameByteArrayReference() {
        ImageInfo info = new ImageInfo();
        byte[] data = new byte[] { 42 };
        info.setImageData(data);

        assertSame(data, info.getImageData());
    }

    /**
     * Verifies that {@code toString()} includes all the field values.
     */
    @Test
    void shouldProduceNonEmptyToString() {
        ImageInfo info = new ImageInfo();
        info.setImageData(new byte[] { 1 });
        info.setWidth(4);
        info.setHeight(4);
        info.setImageFormat(ImageFormat.CP_PAF_BGR24);

        String text = info.toString();
        assertTrue(text.contains("imageData"));
        assertTrue(text.contains("width"));
        assertTrue(text.contains("height"));
        assertTrue(text.contains("imageFormat"));
    }
}