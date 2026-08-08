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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ImageFactory}.
 *
 * <p>The tests build synthetic {@link BufferedImage}s in memory, encode
 * them with {@link ImageIO} and exercise every public entry point of
 * {@link ImageFactory} (RGB / grayscale, file / byte[] / InputStream)
 * plus {@link ImageFactory#getBestRect(int, int, Rect)}.</p>
 *
 * @since 3.0.0
 */
class ImageFactoryTest {

    /**
     * Builds a square solid-colour {@link BufferedImage} of the
     * requested type.
     *
     * @param side the side length in pixels.
     * @param type the {@link BufferedImage} type.
     * @param rgb  the ARGB colour to fill the image with.
     * @return the freshly created {@link BufferedImage}.
     */
    private static BufferedImage solidImage(int side, int type, int rgb) {
        BufferedImage img = new BufferedImage(side, side, type);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(new Color(rgb, true));
            g.fillRect(0, 0, side, side);
        } finally {
            g.dispose();
        }
        return img;
    }

    /**
     * Encodes a {@link BufferedImage} to PNG bytes for the
     * byte-array / stream entry points.
     *
     * @param image the source image.
     * @return the PNG-encoded bytes.
     * @throws IOException if {@link ImageIO} fails to encode.
     */
    private static byte[] encodePng(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    /**
     * Verifies that {@code null} inputs always return {@code null}.
     */
    @Test
    void shouldReturnNullForNullInputs() {
        assertNull(ImageFactory.getRGBData((File) null));
        assertNull(ImageFactory.getGrayData((File) null));
        assertNull(ImageFactory.getRGBData((byte[]) null));
        assertNull(ImageFactory.getGrayData((byte[]) null));
        assertNull(ImageFactory.getRGBData((InputStream) null));
        assertNull(ImageFactory.getGrayData((InputStream) null));
    }

    /**
     * Verifies that {@link ImageFactory#getRGBData(File)} decodes a PNG
     * file into a BGR {@link ImageInfo}.
     */
    @Test
    void shouldDecodeRgbFromFile() throws IOException {
        BufferedImage img = solidImage(64, BufferedImage.TYPE_INT_RGB, 0xFF8040);
        File tmp = File.createTempFile("opencv-sdk-rgb-", ".png");
        try {
            ImageIO.write(img, "png", tmp);

            ImageInfo info = ImageFactory.getRGBData(tmp);

            assertNotNull(info);
            assertEquals(ImageFormat.CP_PAF_BGR24, info.getImageFormat());
            assertEquals(Integer.valueOf(64), info.getWidth());
            assertEquals(Integer.valueOf(64), info.getHeight());
            assertNotNull(info.getImageData());
            assertEquals(64 * 64 * 3, info.getImageData().length);
        } finally {
            Files.deleteIfExists(tmp.toPath());
        }
    }

    /**
     * Verifies that {@link ImageFactory#getGrayData(File)} decodes a PNG
     * file into a grayscale {@link ImageInfo}.
     */
    @Test
    void shouldDecodeGrayFromFile() throws IOException {
        BufferedImage img = solidImage(64, BufferedImage.TYPE_INT_RGB, 0x808080);
        File tmp = File.createTempFile("opencv-sdk-gray-", ".png");
        try {
            ImageIO.write(img, "png", tmp);

            ImageInfo info = ImageFactory.getGrayData(tmp);

            assertNotNull(info);
            assertEquals(ImageFormat.CP_PAF_GRAY, info.getImageFormat());
            assertEquals(Integer.valueOf(64), info.getWidth());
            assertEquals(Integer.valueOf(64), info.getHeight());
            assertEquals(64 * 64, info.getImageData().length);
        } finally {
            Files.deleteIfExists(tmp.toPath());
        }
    }

    /**
     * Verifies the byte-array overload of {@code getRGBData}.
     */
    @Test
    void shouldDecodeRgbFromByteArray() throws IOException {
        byte[] bytes = encodePng(solidImage(32, BufferedImage.TYPE_INT_RGB, 0x123456));

        ImageInfo info = ImageFactory.getRGBData(bytes);

        assertNotNull(info);
        assertEquals(ImageFormat.CP_PAF_BGR24, info.getImageFormat());
        assertEquals(Integer.valueOf(32), info.getWidth());
        assertEquals(Integer.valueOf(32), info.getHeight());
    }

    /**
     * Verifies the byte-array overload of {@code getGrayData}.
     */
    @Test
    void shouldDecodeGrayFromByteArray() throws IOException {
        byte[] bytes = encodePng(solidImage(32, BufferedImage.TYPE_INT_RGB, 0x808080));

        ImageInfo info = ImageFactory.getGrayData(bytes);

        assertNotNull(info);
        assertEquals(ImageFormat.CP_PAF_GRAY, info.getImageFormat());
        assertEquals(Integer.valueOf(32), info.getWidth());
        assertEquals(Integer.valueOf(32), info.getHeight());
    }

    /**
     * Verifies the {@link InputStream} overload of {@code getRGBData}
     * also closes the stream.
     */
    @Test
    void shouldDecodeRgbFromInputStream() throws IOException {
        byte[] bytes = encodePng(solidImage(32, BufferedImage.TYPE_INT_RGB, 0xFF00FF));
        TrackingInputStream tracker = new TrackingInputStream(new ByteArrayInputStream(bytes));

        ImageInfo info = ImageFactory.getRGBData(tracker);

        assertNotNull(info);
        assertEquals(ImageFormat.CP_PAF_BGR24, info.getImageFormat());
        assertTrue(tracker.closed, "RGB InputStream must be closed");
    }

    /**
     * Verifies the {@link InputStream} overload of {@code getGrayData}
     * also closes the stream and emits a grayscale payload.
     */
    @Test
    void shouldDecodeGrayFromInputStream() throws IOException {
        byte[] bytes = encodePng(solidImage(32, BufferedImage.TYPE_INT_RGB, 0x808080));
        TrackingInputStream tracker = new TrackingInputStream(new ByteArrayInputStream(bytes));

        ImageInfo info = ImageFactory.getGrayData(tracker);

        assertNotNull(info);
        assertEquals(ImageFormat.CP_PAF_GRAY, info.getImageFormat());
        assertTrue(tracker.closed, "Gray InputStream must be closed");
    }

    /**
     * Verifies that decoding an unreadable file returns {@code null}.
     */
    @Test
    void shouldReturnNullWhenFileCannotBeRead() {
        File bogus = new File("/this/path/should/never/exist.png");

        assertNull(ImageFactory.getRGBData(bogus));
        assertNull(ImageFactory.getGrayData(bogus));
    }

    /**
     * Verifies that decoding a corrupt input stream returns {@code null}.
     */
    @Test
    void shouldReturnNullWhenInputStreamIsCorrupt() {
        InputStream garbage = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });

        assertNull(ImageFactory.getRGBData(garbage));
        assertNull(ImageFactory.getGrayData(garbage));
    }

    /**
     * Verifies that {@link ImageFactory#bufferedImage2ImageInfo} returns
     * a {@code TYPE_3BYTE_BGR} payload without performing the colour
     * conversion branch when the source is already that type.
     */
    @Test
    void shouldConvertBufferedImageAlreadyInBgrFormat() {
        BufferedImage src = solidImage(8, BufferedImage.TYPE_3BYTE_BGR, 0xFF8040);

        ImageInfo info = ImageFactory.bufferedImage2ImageInfo(src);

        assertEquals(ImageFormat.CP_PAF_BGR24, info.getImageFormat());
        assertEquals(Integer.valueOf(8), info.getWidth());
        assertEquals(Integer.valueOf(8), info.getHeight());
        assertEquals(8 * 8 * 3, info.getImageData().length);
    }

    /**
     * Verifies that {@link ImageFactory#bufferedImage2ImageInfo} goes
     * through the colour-conversion branch when the source image is not
     * already {@code TYPE_3BYTE_BGR}.
     */
    @Test
    void shouldConvertBufferedImageThroughColorConversion() {
        BufferedImage src = solidImage(8, BufferedImage.TYPE_INT_RGB, 0xFF8040);

        ImageInfo info = ImageFactory.bufferedImage2ImageInfo(src);

        assertEquals(ImageFormat.CP_PAF_BGR24, info.getImageFormat());
        assertEquals(Integer.valueOf(8), info.getWidth());
        assertEquals(Integer.valueOf(8), info.getHeight());
        assertEquals(8 * 8 * 3, info.getImageData().length);
    }

    /**
     * Verifies that {@link ImageFactory#bufferedImage2GrayImageInfo}
     * produces a one-byte-per-pixel grayscale buffer.
     */
    @Test
    void shouldConvertBufferedImageToGray() {
        BufferedImage src = solidImage(8, BufferedImage.TYPE_INT_RGB, 0x808080);

        ImageInfo info = ImageFactory.bufferedImage2GrayImageInfo(src);

        assertEquals(ImageFormat.CP_PAF_GRAY, info.getImageFormat());
        assertEquals(Integer.valueOf(8), info.getWidth());
        assertEquals(Integer.valueOf(8), info.getHeight());
        assertEquals(8 * 8, info.getImageData().length);
    }

    /**
     * Verifies that {@link ImageFactory#getBestRect(int, int, Rect)}
     * returns {@code null} for a {@code null} source rectangle.
     */
    @Test
    void shouldReturnNullFromGetBestRectForNullInput() {
        assertNull(ImageFactory.getBestRect(100, 100, null));
    }

    /**
     * Verifies that an already-overflowing rectangle is shrunk
     * towards the centre.
     */
    @Test
    void shouldShrinkOverflowingRectangle() {
        Rect overflow = new Rect(-10, -5, 110, 105);

        Rect rect = ImageFactory.getBestRect(100, 100, overflow);

        // The worst overflow is -10, so the rectangle is shifted by +10
        // on every edge.
        assertEquals(0, rect.left);
        assertEquals(5, rect.top);
        assertEquals(100, rect.right);
        assertEquals(95, rect.bottom);
    }

    /**
     * Verifies that a rectangle that comfortably fits inside the image
     * bounds is grown outward by half its height.
     */
    @Test
    void shouldExpandComfortableRectangle() {
        Rect src = new Rect(20, 20, 60, 60);

        Rect rect = ImageFactory.getBestRect(100, 100, src);

        // Padding = (60-20)/2 = 20. All four edges are shifted outward by 20.
        assertEquals(0, rect.left);
        assertEquals(0, rect.top);
        assertEquals(80, rect.right);
        assertEquals(80, rect.bottom);
    }

    /**
     * Verifies that the padding helper is reduced when the naive
     * padding would overflow the parent bounds.
     */
    @Test
    void shouldClampPaddingWhenExpansionWouldOverflow() {
        Rect src = new Rect(10, 10, 90, 90);

        Rect rect = ImageFactory.getBestRect(100, 100, src);

        // Padding = (90-10)/2 = 40. All four edges would overflow, so
        // padding becomes the smallest margin: min(10, 10, 10, 10) = 10.
        assertEquals(0, rect.left);
        assertEquals(0, rect.top);
        assertEquals(100, rect.right);
        assertEquals(100, rect.bottom);
    }

    /**
     * Verifies that dimensions are aligned to a multiple of four during
     * BGR conversion.
     */
    @Test
    void shouldAlignDimensionsToMultipleOfFour() {
        BufferedImage src = solidImage(15, BufferedImage.TYPE_3BYTE_BGR, 0xFF8040);

        ImageInfo info = ImageFactory.bufferedImage2ImageInfo(src);

        assertEquals(Integer.valueOf(12), info.getWidth());
        assertEquals(Integer.valueOf(12), info.getHeight());
        assertEquals(12 * 12 * 3, info.getImageData().length);
    }

    /**
     * Verifies that the rgb-to-gray conversion actually reduces the
     * byte count and produces grayscale byte data.
     */
    @Test
    void shouldProduceDistinctByteCountsForRgbAndGray() {
        BufferedImage src = solidImage(16, BufferedImage.TYPE_INT_RGB, 0xFF8040);

        ImageInfo rgb = ImageFactory.bufferedImage2ImageInfo(src);
        ImageInfo gray = ImageFactory.bufferedImage2GrayImageInfo(src);

        assertNotNull(rgb.getImageData());
        assertNotNull(gray.getImageData());
        assertNotEquals(rgb.getImageData().length, gray.getImageData().length);
        assertEquals(16 * 16 * 3, rgb.getImageData().length);
        assertEquals(16 * 16, gray.getImageData().length);
    }

    /**
     * Simple delegating {@link InputStream} that remembers whether
     * {@link #close()} has been invoked. Used to assert that the
     * factory closes its inputs.
     */
    private static final class TrackingInputStream extends InputStream {

        private final InputStream delegate;
        private boolean closed;

        TrackingInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            delegate.close();
        }
    }
}