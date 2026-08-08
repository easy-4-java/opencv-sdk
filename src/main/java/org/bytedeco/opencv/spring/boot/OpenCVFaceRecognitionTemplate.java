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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.helper.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.CvHistogram;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * High-level template that performs face detection and grayscale-histogram
 * comparison using the JavaCV / OpenCV bindings.
 *
 * <p>The template expects two collaborators wired in by the caller:</p>
 * <ul>
 *     <li>a pre-trained {@link CascadeClassifier} (typically loaded from
 *         an {@code haarcascade_frontalface_*.xml} resource), and</li>
 *     <li>a temporary directory that the template can use to stage files
 *         passed in as raw bytes.</li>
 * </ul>
 *
 * <p>The three public workflows are:</p>
 * <ul>
 *     <li>{@link #smooth(String)} &mdash; apply an OpenCV smoothing filter
 *         in place.</li>
 *     <li>{@link #detect(File)} (and its overloads) &mdash; run face
 *         detection and return either a result payload or
 *         {@code null} when zero / multiple faces are detected.</li>
 *     <li>{@link #match(File, File)} (and its overloads) &mdash; compare
 *         two grayscale images by histogram correlation.</li>
 * </ul>
 *
 * <p>All public methods tolerate missing inputs: invalid file references
 * or native errors are caught and surfaced as a JSON payload containing
 * {@code error_code = 500} (for detection) or as an empty result (for
 * matching). The template never throws to its caller.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CascadeClassifier
 * @see Imgcodecs
 */
public class OpenCVFaceRecognitionTemplate {

    /** SLF4J logger used for diagnostic messages. */
    private static final Logger log = LoggerFactory.getLogger(OpenCVFaceRecognitionTemplate.class);

    /**
     * Pre-trained Haar cascade classifier used by the
     * {@link #detect(File)} workflow.
     */
    private final CascadeClassifier faceDetector;

    /**
     * Filesystem path of a scratch directory used to stage byte-array
     * inputs. The directory is created on demand by
     * {@link #createTempImageFile(String)}.
     */
    private final String tempDirectory;

    /**
     * Builds a new template.
     *
     * @param faceDetector  the pre-trained face cascade; must not be
     *                      {@code null}.
     * @param tempDirectory filesystem path of a scratch directory used to
     *                      stage byte-array inputs; may be {@code null}
     *                      only when no byte-array overload is invoked.
     */
    public OpenCVFaceRecognitionTemplate(CascadeClassifier faceDetector, String tempDirectory) {
        this.faceDetector = faceDetector;
        this.tempDirectory = tempDirectory;
    }

    /**
     * Applies an OpenCV smoothing filter to the image at {@code path} and
     * writes the result back to the same path.
     *
     * <p>If the image cannot be decoded the method silently returns
     * without modifying the file.</p>
     *
     * @param path the image path to process in place; must not be
     *             {@code null}.
     */
    public void smooth(String path) {
        IplImage image = opencv_imgcodecs.cvLoadImage(path);
        if (Objects.nonNull(image)) {
            opencv_imgproc.cvSmooth(image, image);
            opencv_imgcodecs.cvSaveImage(path, image);
            opencv_core.cvReleaseImage(image);
        }
    }

    /**
     * Convenience overload of {@link #detect(File)} that accepts a path
     * string.
     *
     * @param imagePath the image path to analyse; must not be
     *                  {@code null}.
     * @return a JSON payload describing the detection result or
     *         {@code null} when zero / multiple faces are detected.
     */
    public JSONObject detect(String imagePath) {
        return detect(new File(imagePath));
    }

    /**
     * Runs face detection on the supplied image bytes.
     *
     * <p>The bytes are staged to a temporary file via
     * {@link #createTempImageFile(String)} before being delegated to
     * {@link #detect(File)}.</p>
     *
     * @param imageBytes the encoded image bytes; must not be
     *                   {@code null}.
     * @param filename   a filename whose extension is used when naming
     *                   the temporary file; must not be {@code null}.
     * @return a JSON payload describing the detection result or
     *         {@code null} when zero / multiple faces are detected.
     * @throws Exception if the bytes cannot be written to a temporary
     *                   file.
     */
    public JSONObject detect(byte[] imageBytes, String filename) throws Exception {
        File imageFile = createTempImageFile(filename);
        try (InputStream source = new ByteArrayInputStream(imageBytes)) {
            FileUtils.copyInputStreamToFile(source, imageFile);
            return detect(imageFile);
        }
    }

    /**
     * Runs face detection on the supplied image file.
     *
     * <p>The result is one of:</p>
     * <ul>
     *     <li>a JSON object carrying {@code error_code = 500} when the
     *         file is {@code null} or does not exist,</li>
     *     <li>{@code null} when zero or more than one face is detected,
     *         or</li>
     *     <li>an (otherwise empty) JSON object when exactly one face is
     *         detected.</li>
     * </ul>
     *
     * <p>Exceptions raised by the underlying OpenCV calls are caught and
     * logged; the empty JSON object is returned in that case.</p>
     *
     * @param imageFile the image file to analyse; may be {@code null}.
     * @return a JSON payload describing the detection result or
     *         {@code null} when zero / multiple faces are detected.
     */
    public JSONObject detect(File imageFile) {

        JSONObject result = new JSONObject();

        try {
            log.info("人脸检测开始……");

            if (Objects.isNull(imageFile) || !imageFile.exists()) {
                result.put("error_code", 500);
                result.put("error_msg", "");
                return result;
            }

            Mat image = Imgcodecs.imread(imageFile.getPath());
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(image, faceDetections);

            Rect[] rects = faceDetections.toArray();
            if (Objects.isNull(rects) || rects.length == 0 || rects.length > 1) {
                return null;
            }

            log.info(String.format("检测到人脸： %s", rects.length));
        } catch (Exception e) {
            log.error("Face detection failed.", e);
        }
        return result;
    }

    /**
     * Convenience overload of {@link #match(File, File)} that accepts
     * path strings.
     *
     * @param imagePath1 the first image path; must not be {@code null}.
     * @param imagePath2 the second image path; must not be {@code null}.
     * @return a JSON payload carrying the histogram correlation
     *         {@code score}, or an error payload when an input is
     *         missing.
     */
    public JSONObject match(String imagePath1, String imagePath2) {
        return match(new File(imagePath1), new File(imagePath2));
    }

    /**
     * Compares two images supplied as byte arrays by histogram
     * correlation.
     *
     * @param imageBytes1 the first encoded image; must not be
     *                    {@code null}.
     * @param imageBytes2 the second encoded image; must not be
     *                    {@code null}.
     * @param filename    a filename whose extension is used when naming
     *                    the temporary files; must not be {@code null}.
     * @return a JSON payload carrying the {@code score} field.
     * @throws Exception if the bytes cannot be staged to temporary
     *                   files.
     */
    public JSONObject match(byte[] imageBytes1, byte[] imageBytes2, String filename) throws Exception {
        File imageFile1 = createTempImageFile(filename);
        File imageFile2 = createTempImageFile(filename);
        try (InputStream source1 = new ByteArrayInputStream(imageBytes1);
                InputStream source2 = new ByteArrayInputStream(imageBytes2)) {
            FileUtils.copyInputStreamToFile(source1, imageFile1);
            FileUtils.copyInputStreamToFile(source2, imageFile2);
            return match(imageFile1, imageFile2);
        }
    }

    /**
     * Compares two image files by histogram correlation.
     *
     * <p>Both images are loaded as grayscale, accumulated into 20-bin
     * histograms, L2-normalised, and finally correlated with
     * {@link Imgproc#CV_COMP_CORREL}. The resulting
     * {@code score} (in the closed range {@code [-1, 1]}) is returned
     * inside a JSON payload. Higher scores indicate higher similarity.</p>
     *
     * @param imageFile1 the first image file; may be {@code null} or
     *                   non-existent.
     * @param imageFile2 the second image file; may be {@code null} or
     *                   non-existent.
     * @return a JSON payload carrying the {@code score} field, or an
     *         {@code error_code = 500} payload when an input is missing.
     */
    public JSONObject match(File imageFile1, File imageFile2) {

        JSONObject result = new JSONObject();

        try {
            if (Objects.isNull(imageFile1) || !imageFile1.exists()) {
                result.put("error_code", 500);
                result.put("error_msg", "");
                return result;
            }

            if (Objects.isNull(imageFile2) || !imageFile2.exists()) {
                result.put("error_code", 500);
                result.put("error_msg", "");
                return result;
            }

            int lBins = 20;
            int[] histSize = { lBins };

            float[] vRanges = { 0, 100 };
            float[][] ranges = { vRanges };

            IplImage image1 = opencv_imgcodecs.cvLoadImage(imageFile1.getPath(), Imgcodecs.IMREAD_GRAYSCALE);
            IplImage image2 = opencv_imgcodecs.cvLoadImage(imageFile2.getPath(), Imgcodecs.IMREAD_GRAYSCALE);

            IplImage[] imageArr1 = { image1 };
            IplImage[] imageArr2 = { image2 };

            CvHistogram histogram1 = CvHistogram.create(1, histSize, Imgproc.HISTCMP_CORREL, ranges, 1);
            CvHistogram histogram2 = CvHistogram.create(1, histSize, Imgproc.HISTCMP_CORREL, ranges, 1);

            opencv_imgproc.cvCalcHist(imageArr1, histogram1, 0, null);
            opencv_imgproc.cvCalcHist(imageArr2, histogram2, 0, null);

            opencv_imgproc.cvNormalizeHist(histogram1, 100.0);
            opencv_imgproc.cvNormalizeHist(histogram2, 100.0);

            double score = opencv_imgproc.cvCompareHist(histogram1, histogram2, Imgproc.CV_COMP_CORREL);
            result.put("score", score);
        } catch (Exception e) {
            log.error("Face comparison failed.", e);
        }
        return result;
    }

    /**
     * Allocates a unique temporary file inside the configured temp
     * directory. The directory is created lazily when it does not
     * already exist.
     *
     * @param filename a sample filename whose extension is preserved by
     *                 the generated file name.
     * @return a {@link File} handle for a non-existing temporary file
     *         inside {@link #getTempDirectory()}.
     */
    private File createTempImageFile(String filename) {
        File tempDir = new File(getTempDirectory());
        if (!tempDir.exists()) {
            tempDir.setReadable(true);
            tempDir.setWritable(true);
            tempDir.mkdir();
        }
        return new File(tempDir, UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(filename));
    }

    /**
     * Returns the {@link CascadeClassifier} configured at construction
     * time.
     *
     * @return the configured face detector.
     */
    public CascadeClassifier getFaceDetector() {
        return faceDetector;
    }

    /**
     * Returns the temp directory configured at construction time.
     *
     * @return the temp directory path, never {@code null} after a
     *         non-null constructor argument.
     */
    public String getTempDirectory() {
        return tempDirectory;
    }
}