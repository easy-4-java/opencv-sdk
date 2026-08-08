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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link ImageLoader}.
 *
 * <p>The enum exposes four named constants; these tests verify they are
 * resolvable through {@link ImageLoader#valueOf(String)} and that the
 * {@link ImageLoader#values()} array lists all four.</p>
 *
 * @since 3.0.0
 */
class ImageLoaderTest {

    /**
     * Verifies {@link ImageLoader#values()} returns exactly the four
     * documented constants.
     */
    @Test
    void shouldExposeFourStrategies() {
        ImageLoader[] values = ImageLoader.values();

        assertNotNull(values);
        assertEquals(4, values.length);
    }

    /**
     * Verifies every constant is resolvable via {@code valueOf} and
     * returns the canonical singleton.
     */
    @Test
    void shouldResolveEveryConstantByName() {
        assertSame(ImageLoader.CIFAR, ImageLoader.valueOf("CIFAR"));
        assertSame(ImageLoader.DEFAULT, ImageLoader.valueOf("DEFAULT"));
        assertSame(ImageLoader.LFW, ImageLoader.valueOf("LFW"));
        assertSame(ImageLoader.NATIVE, ImageLoader.valueOf("NATIVE"));
    }
}