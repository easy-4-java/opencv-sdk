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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


/**
 * Utility class that converts decoded images into the SDK's normalised
 * {@link ImageInfo} representation.
 *
 * <p>All entry points are {@code static}; the class is not meant to be
 * instantiated. The conversion pipeline is intentionally simple:</p>
 *
 * <ol>
 *     <li>Decode the source {@link File}, {@code byte[]} or {@link InputStream}
 *         via {@link ImageIO}.</li>
 *     <li>Crop the width and height down to a multiple of four so the result
 *         is safe to feed to native code that requires 4-byte alignment.</li>
 *     <li>Convert the pixel buffer to either 24-bit BGR or 8-bit grayscale
 *         depending on the entry point.</li>
 *     <li>Wrap the result in an {@link ImageInfo}.</li>
 * </ol>
 *
 * <p>The class also exposes a {@link #getBestRect(int, int, Rect)} helper
 * that grows (or shrinks) a face rectangle so that it sits comfortably
 * inside the parent image bounds.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ImageInfo
 * @see ImageFormat
 */
public class ImageFactory {


    /**
     * Loads {@code file} and decodes it as a 24-bit BGR {@link ImageInfo}.
     *
     * @param file the image file to load; may be {@code null}.
     * @return the decoded {@link ImageInfo} or {@code null} when the file is
     *         {@code null}, the file cannot be read, or the underlying
     *         {@link ImageIO} call throws.
     */
    public static ImageInfo getRGBData(File file) {
        if (file == null)
            return null;
        ImageInfo imageInfo;
        try {
            //Decode the file into an in-memory BufferedImage.
            BufferedImage image = ImageIO.read(file);
            imageInfo = bufferedImage2ImageInfo(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imageInfo;
    }

    /**
     * Loads {@code file} and decodes it as an 8-bit grayscale
     * {@link ImageInfo}.
     *
     * @param file the image file to load; may be {@code null}.
     * @return the decoded {@link ImageInfo} or {@code null} when the file is
     *         {@code null}, the file cannot be read, or the underlying
     *         {@link ImageIO} call throws.
     */
    public static ImageInfo getGrayData(File file) {
        if (file == null)
            return null;
        ImageInfo imageInfo;
        try {
            //Decode the file into an in-memory BufferedImage.
            BufferedImage image = ImageIO.read(file);
            imageInfo = bufferedImage2GrayImageInfo(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imageInfo;
    }

    /**
     * Convenience overload that wraps {@code bytes} in a
     * {@link ByteArrayInputStream} before delegating to
     * {@link #getRGBData(InputStream)}.
     *
     * @param bytes the encoded image bytes; may be {@code null}.
     * @return the decoded {@link ImageInfo} or {@code null} when
     *         {@code bytes} is {@code null}.
     */
    public static ImageInfo getRGBData(byte[] bytes) {
        if (bytes == null)
            return null;
        return getRGBData(new ByteArrayInputStream(bytes));
    }

    /**
     * Convenience overload that wraps {@code bytes} in a
     * {@link ByteArrayInputStream} before delegating to
     * {@link #getGrayData(InputStream)}.
     *
     * @param bytes the encoded image bytes; may be {@code null}.
     * @return the decoded {@link ImageInfo} or {@code null} when
     *         {@code bytes} is {@code null}.
     */
    public static ImageInfo getGrayData(byte[] bytes) {
        if (bytes == null)
            return null;
        return getGrayData(new ByteArrayInputStream(bytes));
    }


    /**
     * Reads an image from {@code input} and decodes it as a 24-bit BGR
     * {@link ImageInfo}.
     *
     * <p>The {@code input} stream is always closed in a {@code finally}
     * block; any {@link IOException} raised while closing is silently
     * swallowed to keep the public contract null-friendly.</p>
     *
     * @param input the source stream; may be {@code null}.
     * @return the decoded {@link ImageInfo} or {@code null} when the input
     *         is {@code null}, the image could not be decoded, or an
     *         {@link IOException} occurred.
     * @throws IOException never propagated; failures are surfaced as a
     *         {@code null} return.
     */
    public static ImageInfo getRGBData(InputStream input) {
        if (input == null)
            return null;
        ImageInfo imageInfo;
        try {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                return null;
            }
            imageInfo = bufferedImage2ImageInfo(image);

        } catch (IOException e) {
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
            }

        }
        return imageInfo;
    }

    /**
     * Reads an image from {@code input} and decodes it as an 8-bit
     * grayscale {@link ImageInfo}.
     *
     * <p>The {@code input} stream is always closed in a {@code finally}
     * block; any {@link IOException} raised while closing is silently
     * swallowed to keep the public contract null-friendly.</p>
     *
     * @param input the source stream; may be {@code null}.
     * @return the decoded {@link ImageInfo} or {@code null} when the input
     *         is {@code null}, the image could not be decoded, or an
     *         {@link IOException} occurred.
     * @throws IOException never propagated; failures are surfaced as a
     *         {@code null} return.
     */
    public static ImageInfo getGrayData(InputStream input) {
        if (input == null)
            return null;
        ImageInfo imageInfo;
        try {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                return null;
            }
            imageInfo = bufferedImage2GrayImageInfo(image);

        } catch (IOException e) {
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
            }

        }
        return imageInfo;
    }


    /**
     * Converts a {@link BufferedImage} into a 24-bit BGR {@link ImageInfo}.
     *
     * <p>Width and height are aligned down to a multiple of four so the
     * result is safe to consume by native APIs that require 4-byte
     * alignment. If the source image is not already
     * {@link BufferedImage#TYPE_3BYTE_BGR}, it is converted through
     * {@link ColorConvertOp} using a linear RGB {@link ColorSpace}.</p>
     *
     * @param image the decoded source image; must not be {@code null}.
     * @return an {@link ImageInfo} carrying the BGR pixel buffer plus its
     *         aligned dimensions.
     */
    public static ImageInfo bufferedImage2ImageInfo(BufferedImage image) {
        ImageInfo imageInfo = new ImageInfo();
        int width = image.getWidth();
        int height = image.getHeight();
        // Align dimensions to a multiple of four.
        width = width & (~3);
        height = height & (~3);
        imageInfo.setWidth(width);
        imageInfo.setHeight(height);
        //Copy pixels into a fresh buffer of the same type as the source.
        BufferedImage resultImage = new BufferedImage(width, height, image.getType());
        //Extract the ARGB pixel matrix from the source.
        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        //Paint the extracted pixels back into the aligned buffer.
        resultImage.setRGB(0, 0, width, height, rgb, 0, width);
        //Ensure the destination buffer is in BGR byte order.
        BufferedImage dstImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        if (resultImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            ColorConvertOp colorConvertOp = new ColorConvertOp(cs, dstImage.createGraphics().getRenderingHints());
            colorConvertOp.filter(resultImage, dstImage);
        } else {
            dstImage = resultImage;
        }
        //Expose the raw bytes and the pixel format.
        imageInfo.setImageFormat(ImageFormat.CP_PAF_BGR24);
        imageInfo.setImageData(((DataBufferByte) (dstImage.getRaster().getDataBuffer())).getData());
        return imageInfo;
    }

    /**
     * Converts a {@link BufferedImage} into an 8-bit grayscale
     * {@link ImageInfo} by averaging the R/G/B channels per pixel.
     *
     * <p>Width and height are aligned down to a multiple of four. The
     * grayscale conversion uses a fixed-point approximation of the BT.601
     * luma formula ({@code 66 * R + 129 * G + 25 * B + 128 >> 8 + 16}).</p>
     *
     * @param image the decoded source image; must not be {@code null}.
     * @return an {@link ImageInfo} carrying the grayscale byte buffer plus
     *         its aligned dimensions.
     */
    public static ImageInfo bufferedImage2GrayImageInfo(BufferedImage image) {
        ImageInfo imageInfo = new ImageInfo();
        int width = image.getWidth();
        int height = image.getHeight();
        // Align dimensions to a multiple of four.
        width = width & (~3);
        height = height & (~3);
        imageInfo.setWidth(width);
        imageInfo.setHeight(height);

        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        byte[] bytes = rgbToGray(rgb, width, height);
        imageInfo.setImageFormat(ImageFormat.CP_PAF_GRAY);
        imageInfo.setImageData(bytes);
        return imageInfo;
    }


    /**
     * Converts an ARGB pixel matrix into an 8-bit grayscale byte buffer
     * using a fixed-point BT.601 approximation.
     *
     * @param argb   the source pixel matrix in {@code ARGB} packed-int form.
     * @param width  the number of columns to convert.
     * @param height the number of rows to convert.
     * @return a freshly allocated {@code width * height} byte buffer
     *         containing the grayscale pixels.
     */
    private static byte[] rgbToGray(int[] argb, int width, int height) {

        int yIndex = 0;
        int index = 0;
        byte[] gray = new byte[width * height];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
//                int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
//                int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
                gray[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                ++index;
            }
        }
        return gray;
    }


    /**
     * Expands (or contracts) {@code srcRect} so that it sits comfortably
     * inside an image of size {@code (width, height)}.
     *
     * <p>If any edge of the source rectangle already overflows the image
     * bounds, the rectangle is shrunk toward the centre by the worst-case
     * overflow. Otherwise the rectangle is grown outward by half its
     * height; if that would overflow, the padding is reduced to the
     * smallest of the four margins.</p>
     *
     * @param width   the parent image width.
     * @param height  the parent image height.
     * @param srcRect the original rectangle; may be {@code null}.
     * @return a new rectangle centred on the original one and clipped to
     *         the parent bounds, or {@code null} when {@code srcRect} is
     *         {@code null}.
     */
    public static Rect getBestRect(int width, int height, Rect srcRect) {
        if (srcRect == null) {
            return null;
        }
        Rect rect = new Rect(srcRect);
        int maxOverFlow = Math.max(-rect.left, Math.min(-rect.top, Math.min(width - rect.right, height - rect.bottom)));
        // The rectangle overflows the parent bounds: shrink it.
        if (maxOverFlow > 0) {
            rect.left += maxOverFlow;
            rect.top += maxOverFlow;
            rect.right -= maxOverFlow;
            rect.bottom -= maxOverFlow;
            return rect;
        }
        // The rectangle fits inside the parent: expand it by half its height.
        int padding = (rect.bottom - rect.top) / 2;
        // If the naive padding overflows, shrink it to the smallest margin.
        if (!(rect.left - padding > 0
                && rect.right + padding < width
                && rect.top - padding > 0
                && rect.bottom + padding < height)) {
            padding = Math.min(Math.min(Math.min(rect.left, width - rect.right), height - rect.bottom), rect.top);
        }
        rect.left -= padding;
        rect.top -= padding;
        rect.right += padding;
        rect.bottom += padding;
        return rect;
    }

}