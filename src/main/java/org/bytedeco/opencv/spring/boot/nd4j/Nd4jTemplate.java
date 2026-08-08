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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.bytedeco.opencv.spring.boot.nd4j.store.INDArrayStoreProvider;
import org.datavec.image.loader.BaseImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * ND4J-backed template that turns images into face embeddings.
 *
 * <p>The template wires three collaborators together:</p>
 * <ul>
 *     <li>a {@link ComputationGraph} that produces a 128-dimensional
 *         {@code L2}-normalised embedding,</li>
 *     <li>a {@link BaseImageLoader} that decodes images into INDArrays, and</li>
 *     <li>an {@link INDArrayStoreProvider} where embeddings are persisted.</li>
 * </ul>
 *
 * <p>Typical use:</p>
 * <pre>{@code
 * Nd4jTemplate tpl = new Nd4jTemplate(graph, new NativeImageLoader(96, 96, 3), 96, 96);
 * tpl.setStoreProvider(new INDArrayLocalCacheStoreProvider());
 * tpl.faceNew("employees", "42", imageFile);
 * }</pre>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see Nd4jUtils
 * @see INDArrayStoreProvider
 */
public class Nd4jTemplate {

    /**
     * The pre-trained FaceNet-style graph used to compute embeddings.
     */
    private ComputationGraph computationGraph;

    /**
     * The image loader used to decode incoming files / bytes / streams
     * into {@link INDArray} tensors.
     */
    private BaseImageLoader imageLoader;

    /**
     * Pluggable storage for embeddings. May be {@code null} until the
     * caller wires one in via {@link #setStoreProvider}.
     */
    private INDArrayStoreProvider storeProvider;

    /**
     * Target image height in pixels. Used to shape the tensor fed to the
     * computation graph.
     */
    private int height;

    /**
     * Target image width in pixels. Used to shape the tensor fed to the
     * computation graph.
     */
    private int width;

    /**
     * Constructs a new template.
     *
     * @param computationGraph the FaceNet-style graph; must not be
     *                         {@code null}.
     * @param imageLoader      the image loader; must not be {@code null}.
     * @param height           target image height in pixels.
     * @param width            target image width in pixels.
     */
    public Nd4jTemplate(ComputationGraph computationGraph, BaseImageLoader imageLoader, int height, int width) {
        this.computationGraph = computationGraph;
        this.imageLoader = imageLoader;
        this.height = height;
        this.width = width;
    }

    /**
     * Stores a new face embedding decoded from a byte array.
     *
     * @param group      the logical grouping key; must not be
     *                   {@code null}.
     * @param memberId   the member identifier inside {@code group}; must
     *                   not be {@code null}.
     * @param imageBytes the encoded image bytes; must not be
     *                   {@code null}.
     * @throws IOException if the underlying image loader fails to decode
     *                     {@code imageBytes}.
     */
    public void faceNew(String group, String memberId, byte[] imageBytes) throws IOException {
        INDArray read = asMatrix(imageBytes);
        storeProvider.store(group, memberId, forwardPass(normalize(read)));
    }

    /**
     * Stores a new face embedding decoded from a file.
     *
     * @param group     the logical grouping key; must not be {@code null}.
     * @param memberId  the member identifier inside {@code group}; must
     *                  not be {@code null}.
     * @param imagePath the source image file; must not be {@code null}.
     * @throws IOException if the underlying image loader fails to decode
     *                     {@code imagePath}.
     */
    public void faceNew(String group, String memberId, File imagePath) throws IOException {
        INDArray read = asMatrix(imagePath);
        storeProvider.store(group, memberId, forwardPass(normalize(read)));
    }

    /**
     * Stores a new face embedding decoded from a file path.
     *
     * @param group     the logical grouping key; must not be {@code null}.
     * @param memberId  the member identifier inside {@code group}; must
     *                  not be {@code null}.
     * @param imagePath the source image file path; must not be
     *                  {@code null}.
     * @throws IOException if the underlying image loader fails to decode
     *                     {@code imagePath}.
     */
    public void faceNew(String group, String memberId, String imagePath) throws IOException {
        INDArray read = asMatrix(imagePath);
        storeProvider.store(group, memberId, forwardPass(normalize(read)));
    }

    /**
     * Scales every pixel value to the {@code [0, 1]} range expected by the
     * FaceNet-style graph (which is trained on 8-bit pixel data).
     *
     * @param read the source tensor.
     * @return a new tensor where every value has been divided by {@code 255}.
     */
    private static INDArray normalize(INDArray read) {
        return read.div(255.0);
    }

    /**
     * Compares two images and prints {@code match} or {@code dismatch}
     * based on whether the embedding distance is below {@code 0.45}.
     *
     * @param imageBytes1 the first encoded image; must not be
     *                     {@code null}.
     * @param imageBytes2 the second encoded image; must not be
     *                     {@code null}.
     * @throws IOException if the underlying image loader fails to decode
     *                     either image.
     */
    public void match(byte[] imageBytes1, byte[] imageBytes2) throws IOException {
        INDArray r1 = asMatrix(imageBytes1);
        INDArray r2 = asMatrix(imageBytes2);

        INDArray e1 = forwardPass(normalize(r1));
        INDArray e2 = forwardPass(normalize(r2));

        double dis = Nd4jUtils.distance(e1, e2);
        System.out.println("distance is : " + dis);
        if (dis < 0.45) {
            System.out.println("match");
        } else {
            System.out.println("dismatch");
        }
    }

    /**
     * Placeholder for a future similarity search. The current
     * implementation always returns the empty string.
     *
     * @param group    the logical grouping key.
     * @param memberId the member identifier inside {@code group}.
     * @return the empty string.
     * @throws IOException never thrown by the current implementation.
     */
    public String search(String group, String memberId) throws IOException {
        return "";
    }

    /**
     * Decodes {@code imageBytes} into a tensor and permutes it to the
     * channels-first layout expected by the graph.
     *
     * @param imageBytes the encoded image bytes; must not be {@code null}.
     * @return the decoded tensor, shape {@code (1, 3, height, width)}.
     * @throws IOException if the underlying image loader fails to decode
     *                     {@code imageBytes}.
     */
    public INDArray asMatrix(byte[] imageBytes) throws IOException {
        return asMatrix(new ByteArrayInputStream(imageBytes));
    }

    /**
     * Decodes {@code imageStream} into a tensor and permutes it to the
     * channels-first layout expected by the graph.
     *
     * @param imageStream the source image stream; must not be
     *                    {@code null}. The stream is consumed but not
     *                    closed by this method.
     * @return the decoded tensor, shape {@code (1, 3, height, width)}.
     * @throws IOException if the underlying image loader fails to decode
     *                     {@code imageStream}.
     */
    public INDArray asMatrix(InputStream imageStream) throws IOException {
        INDArray indArray = imageLoader.asMatrix(imageStream);
        return Nd4jUtils.transpose(indArray, height, width);
    }

    /**
     * Decodes {@code imagePath} into a tensor and permutes it to the
     * channels-first layout expected by the graph.
     *
     * @param imagePath the source image file; must not be {@code null}.
     * @return the decoded tensor, shape {@code (1, 3, height, width)}.
     * @throws IOException if the underlying image loader fails to decode
     *                     {@code imagePath}.
     */
    public INDArray asMatrix(File imagePath) throws IOException {
        INDArray indArray = imageLoader.asMatrix(imagePath);
        return Nd4jUtils.transpose(indArray, height, width);
    }

    /**
     * Decodes {@code imagePath} into a tensor and permutes it to the
     * channels-first layout expected by the graph.
     *
     * @param imagePath the source image file path; must not be
     *                  {@code null}.
     * @return the decoded tensor, shape {@code (1, 3, height, width)}.
     * @throws IOException if the underlying image loader fails to decode
     *                     {@code imagePath}.
     */
    public INDArray asMatrix(String imagePath) throws IOException {
        INDArray indArray = imageLoader.asMatrix(new File(imagePath));
        return Nd4jUtils.transpose(indArray, height, width);
    }

    /**
     * Runs the graph's {@code encodings} vertex on the supplied tensor.
     *
     * @param indArray the input tensor (channels-first, normalised).
     * @return the embedding vector produced by the {@code encodings}
     *         vertex.
     */
    private INDArray forwardPass(INDArray indArray) {
        Map<String, INDArray> output = computationGraph.feedForward(indArray, false);
        GraphVertex embeddings = computationGraph.getVertex("encodings");
        INDArray dense = output.get("dense");
        embeddings.setInputs(dense);
        INDArray embeddingValues = embeddings.doForward(false, LayerWorkspaceMgr.builder().defaultNoWorkspace().build());
        System.out.println("dense =                 " + dense);
        System.out.println("encodingsValues =                 " + embeddingValues);
        return embeddingValues;
    }

    /**
     * Returns the configured {@link INDArrayStoreProvider}, may be
     * {@code null} when none has been set yet.
     *
     * @return the current store provider, or {@code null}.
     */
    public INDArrayStoreProvider getStoreProvider() {
        return storeProvider;
    }

    /**
     * Replaces the {@link INDArrayStoreProvider} used by
     * {@link #faceNew(String, String, byte[])} and its overloads.
     *
     * @param storeProvider the new store provider; may be {@code null}
     *                      to detach storage.
     */
    public void setStoreProvider(INDArrayStoreProvider storeProvider) {
        this.storeProvider = storeProvider;
    }
}