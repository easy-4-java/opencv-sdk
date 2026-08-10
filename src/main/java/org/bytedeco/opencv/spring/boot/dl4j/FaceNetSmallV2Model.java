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
package org.bytedeco.opencv.spring.boot.dl4j;

import java.io.IOException;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.L2NormalizeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;

/**
 * Small FaceNet-style embedding model used by the OpenCV starter.
 *
 * <p>The class wires a single {@link ComputationGraphConfiguration}
 * together with the input {@code 3 &times; 96 &times; 96} tensor shape and
 * a {@code 128}-dimensional output embedding. The configuration uses
 * one convolutional layer with {@code 3 &times; 3} kernels and
 * {@code 2 &times; 2} stride, one average-pooling layer, one dense
 * embedding layer and finally an {@link L2NormalizeVertex} to ensure
 * cosine similarity is meaningful.</p>
 *
 * <p>This is a "model definition" class only &mdash; it does not bundle
 * pre-trained weights. Callers typically {@link #init() initialise} the
 * graph after loading weights from disk or after training.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see ComputationGraph
 * @see Nd4jTemplate
 */
public class FaceNetSmallV2Model {

    /**
     * Random seed used for all initialisation routines. Fixed at
     * {@code 1234} so that two instances built with the same
     * configuration produce identical parameters.
     */
    private final long seed = 1234L;

    /**
     * Input tensor shape: {@code (channels=3, height=96, width=96)}.
     */
    private final int[] inputShape = new int[] { 3, 96, 96 };

    /**
     * Width of the L2-normalised embedding vector. Default {@code 128}.
     */
    private int encodings = 128;

    /**
     * Builds the {@link ComputationGraphConfiguration} that drives the
     * embedding model.
     *
     * <p>The configuration uses:</p>
     * <ul>
     *     <li>{@link OptimizationAlgorithm#STOCHASTIC_GRADIENT_DESCENT},</li>
     *     <li>{@link Activation#RELU} for the convolutional and pooling
     *         layers and {@link Activation#IDENTITY} for the dense
     *         embedding,</li>
     *     <li>{@link WeightInit#RELU} and {@code L2 5e-5} regularisation,</li>
     *     <li>an {@link Adam} updater with
     *         {@code (learningRate=0.1, beta1=0.9, beta2=0.999, epsilon=0.01)},</li>
     *     <li>mini-batch training enabled.</li>
     * </ul>
     *
     * @return a fully configured {@link ComputationGraphConfiguration}
     *         ready to be passed to {@link ComputationGraph#init()}.
     */
    public ComputationGraphConfiguration conf() {
        return new NeuralNetConfiguration.Builder()
                .seed(seed)
                .activation(Activation.RELU)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.1, 0.9, 0.999, 0.01))
                .weightInit(WeightInit.RELU)
                .l2(5e-5)
                .miniBatch(true)
                .graphBuilder()
                .addInputs("input1")
                .setInputTypes(InputType.convolutional(inputShape[1], inputShape[2], inputShape[0]))
                .addLayer("conv1", new ConvolutionLayer.Builder(new int[] { 3, 3 }, new int[] { 2, 2 })
                        .nIn(inputShape[0])
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build(), "input1")
                .addLayer("pool1", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[] { 2, 2 })
                        .build(), "conv1")
                .addLayer("dense", new DenseLayer.Builder()
                        .nOut(encodings)
                        .activation(Activation.IDENTITY)
                        .build(), "pool1")
                .addVertex("encodings", new L2NormalizeVertex(new int[] {}, 1e-12), "dense")
                .setOutputs("encodings")
                .build();
    }

    /**
     * Builds, configures and initialises a {@link ComputationGraph} that
     * implements the small FaceNet-style embedding.
     *
     * @return a freshly initialised {@link ComputationGraph}.
     * @throws IOException if the underlying DL4J initialisation routine
     *                     fails (for example because of missing native
     *                     libraries).
     */
    public ComputationGraph init() throws IOException {
        ComputationGraph computationGraph = new ComputationGraph(conf());
        computationGraph.init();
        return computationGraph;
    }
}