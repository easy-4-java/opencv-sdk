/*
 * Copyright (c) 2018, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
 * @author [@Loong Wan](https://github.com/loong10k)
 */
public class FaceNetSmallV2Model {

	private final long seed = 1234L;
	private final int[] inputShape = new int[] { 3, 96, 96 };
	private int encodings = 128;

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

	public ComputationGraph init() throws IOException {
		ComputationGraph computationGraph = new ComputationGraph(conf());
		computationGraph.init();
		return computationGraph;
	}
}
