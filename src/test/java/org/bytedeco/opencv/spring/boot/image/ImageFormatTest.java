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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link ImageFormat}.
 *
 * <p>The enum's native-code value identifiers are part of the SDK's
 * public contract (consumed by native libraries), so this test pins
 * them down.</p>
 *
 * @since 3.0.0
 */
class ImageFormatTest {

    /**
     * Verifies every {@link ImageFormat} constant carries the expected
     * integer code.
     */
    @Test
    void shouldExposeCanonicalNativeValues() {
        assertEquals(2050, ImageFormat.CP_PAF_NV21.getValue());
        assertEquals(2049, ImageFormat.CP_PAF_NV12.getValue());
        assertEquals(1537, ImageFormat.CP_PAF_I420.getValue());
        assertEquals(1281, ImageFormat.CP_PAF_YUYV.getValue());
        assertEquals(513, ImageFormat.CP_PAF_BGR24.getValue());
        assertEquals(1793, ImageFormat.CP_PAF_GRAY.getValue());
        assertEquals(3074, ImageFormat.CP_PAF_DEPTH_U16.getValue());
    }

    /**
     * Verifies {@link ImageFormat#valueOf(String)} returns the same
     * singleton that {@link ImageFormat} declares for the given name.
     */
    @Test
    void shouldResolveEveryConstantViaValueOf() {
        assertSame(ImageFormat.CP_PAF_NV21, ImageFormat.valueOf("CP_PAF_NV21"));
        assertSame(ImageFormat.CP_PAF_NV12, ImageFormat.valueOf("CP_PAF_NV12"));
        assertSame(ImageFormat.CP_PAF_I420, ImageFormat.valueOf("CP_PAF_I420"));
        assertSame(ImageFormat.CP_PAF_YUYV, ImageFormat.valueOf("CP_PAF_YUYV"));
        assertSame(ImageFormat.CP_PAF_BGR24, ImageFormat.valueOf("CP_PAF_BGR24"));
        assertSame(ImageFormat.CP_PAF_GRAY, ImageFormat.valueOf("CP_PAF_GRAY"));
        assertSame(ImageFormat.CP_PAF_DEPTH_U16, ImageFormat.valueOf("CP_PAF_DEPTH_U16"));
    }
}