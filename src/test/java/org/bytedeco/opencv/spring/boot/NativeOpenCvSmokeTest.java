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
package org.bytedeco.opencv.spring.boot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.helper.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_java;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Proves that both OpenCV Java and JavaCV native bindings can decode a real image. */
@EnabledOnOs(OS.LINUX)
@EnabledIfSystemProperty(named = "os.arch", matches = "amd64|x86_64")
class NativeOpenCvSmokeTest {

    @TempDir
    Path tempDir;

    @Test
    void decodesPngWithBothNativeBindings() throws Exception {
        File file = tempDir.resolve("pixels.png").toFile();
        BufferedImage source = new BufferedImage(8, 6, BufferedImage.TYPE_3BYTE_BGR);
        source.setRGB(2, 3, 0x00ff0000);
        assertTrue(ImageIO.write(source, "png", file));

        assertNotNull(Loader.load(opencv_java.class));
        Mat matrix = Imgcodecs.imread(file.getAbsolutePath());
        assertFalse(matrix.empty());
        assertEquals(8, matrix.cols());
        assertEquals(6, matrix.rows());
        assertArrayEquals(new double[] { 0, 0, 255 }, matrix.get(3, 2), 0.0);

        IplImage image = opencv_imgcodecs.cvLoadImage(file.getAbsolutePath());
        assertNotNull(image);
        assertEquals(8, image.width());
        assertEquals(6, image.height());
        opencv_core.cvReleaseImage(image);
        matrix.release();
    }
}
