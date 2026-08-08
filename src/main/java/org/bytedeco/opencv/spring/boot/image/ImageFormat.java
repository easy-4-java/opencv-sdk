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


/**
 * Pixel-layout formats used by {@link ImageInfo} and the surrounding
 * OpenCV / ND4J pipelines.
 *
 * <p>Each constant carries the integer code expected by the ArcSoft-style
 * native binding that this SDK historically targets (the {@code value} field
 * mirrors the {@code CP_PAF_*} numeric constants from the C++ header). The
 * enum values cover the common Android camera and computer-vision pixel
 * layouts:</p>
 *
 * <ul>
 *     <li>{@link #CP_PAF_NV21} &mdash; YUV 4:2:0 semi-planar, VU interleaved.</li>
 *     <li>{@link #CP_PAF_NV12} &mdash; YUV 4:2:0 semi-planar, UV interleaved.</li>
 *     <li>{@link #CP_PAF_I420} &mdash; YUV 4:2:0 planar.</li>
 *     <li>{@link #CP_PAF_YUYV} &mdash; YUV 4:2:2 packed.</li>
 *     <li>{@link #CP_PAF_BGR24} &mdash; 24-bit BGR (the default output of
 *         {@link ImageFactory#bufferedImage2ImageInfo}).</li>
 *     <li>{@link #CP_PAF_GRAY} &mdash; 8-bit single-channel grayscale.</li>
 *     <li>{@link #CP_PAF_DEPTH_U16} &mdash; 16-bit unsigned depth map.</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ImageInfo
 * @see ImageFactory
 */
public enum ImageFormat {
    /**
     * NV21 pixel layout: 8-bit Y plane followed by a 2x2 subsampled V/U
     * interleaved chroma plane. Native code value: {@code 2050}.
     */
    CP_PAF_NV21(2050),

    /**
     * NV12 pixel layout: 8-bit Y plane followed by a 2x2 subsampled U/V
     * interleaved chroma plane. Native code value: {@code 2049}.
     */
    CP_PAF_NV12(2049),

    /**
     * I420 (YUV420 planar) pixel layout: independent Y, U and V planes each
     * subsampled 2x2. Native code value: {@code 1537}.
     */
    CP_PAF_I420(1537),

    /**
     * YUYV (YUV422 packed) pixel layout: each macropixel contains
     * {@code Y0, U0, Y1, V0}. Native code value: {@code 1281}.
     */
    CP_PAF_YUYV(1281),

    /**
     * 24-bit BGR, three bytes per pixel laid out as {@code B, G, R}.
     * Native code value: {@code 513}. This is the layout produced by
     * {@link ImageFactory#bufferedImage2ImageInfo}.
     */
    CP_PAF_BGR24(513),

    /**
     * 8-bit single-channel grayscale. Native code value: {@code 1793}.
     */
    CP_PAF_GRAY(1793),

    /**
     * 16-bit unsigned depth map (single channel). Native code value:
     * {@code 3074}.
     */
    CP_PAF_DEPTH_U16(3074);


    /**
     * Native-code numeric identifier for this pixel format, matching the
     * {@code CP_PAF_*} constants from the underlying C API.
     */
    private int value;

    /**
     * Package-private constructor used by the enum constants.
     *
     * @param value the native-code numeric identifier to associate with this
     *              constant.
     */
    ImageFormat(int value) {
        this.value = value;
    }

    /**
     * Returns the native-code numeric identifier associated with this
     * constant. Useful when bridging to C APIs that expect an integer
     * pixel-format code rather than a Java enum.
     *
     * @return the integer constant mirroring the C {@code CP_PAF_*} value.
     */
    public int getValue() {
        return value;
    }
}