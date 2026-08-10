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

import lombok.Data;

/**
 * Immutable-style value object describing a single decoded image.
 *
 * <p>An {@code ImageInfo} bundles the raw pixel bytes together with the
 * {@link #getWidth() width}, {@link #getHeight() height} and the originating
 * {@link #getImageFormat() pixel format}. It is the canonical output of
 * {@link ImageFactory} and the canonical input to consumers such as ND4J
 * tensors or OpenCV {@code Mat} objects.</p>
 *
 * <p>The class is annotated with Lombok's {@link Data}, so getters, setters,
 * {@code toString()}, {@code equals(Object)} and {@code hashCode()} are
 * generated at compile time.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see ImageFormat
 * @see ImageFactory
 */
@Data
public class ImageInfo {

    /**
     * Raw pixel bytes for the image, in the row-major order expected by the
     * accompanying {@link #getImageFormat() format}. For
     * {@link ImageFormat#CP_PAF_BGR24} each pixel is encoded as three bytes
     * (B, G, R); for {@link ImageFormat#CP_PAF_GRAY} each pixel is a single
     * byte. May be {@code null} until the buffer has been populated.
     */
    private byte[] imageData;

    /**
     * Decoded image width in pixels, aligned to a multiple of four by the
     * producing {@link ImageFactory} so it is safe to feed into native
     * OpenCV or DL4J pipelines that require 4-byte alignment.
     */
    private Integer width;

    /**
     * Decoded image height in pixels, aligned to a multiple of four by the
     * producing {@link ImageFactory}.
     */
    private Integer height;

    /**
     * Pixel layout used by {@link #getImageData()}. Determines how the byte
     * array should be interpreted by downstream consumers.
     */
    private ImageFormat imageFormat;
}