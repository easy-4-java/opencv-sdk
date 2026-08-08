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

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 * Static helpers for manipulating ND4J tensors in the face-embedding
 * pipeline.
 *
 * <p>The class is not instantiable; both helpers are {@code static} and
 * side-effect-free. {@link #transpose(INDArray, int, int)} reshapes a
 * three-channel BGR tensor into the canonical {@code (1, 3, H, W)} layout
 * expected by the FaceNet-style embedding model, and
 * {@link #distance(INDArray, INDArray)} returns the squared Euclidean
 * distance between two equally-shaped tensors.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see Nd4jTemplate
 */
public class Nd4jUtils {

    /**
     * Re-orders a 3-channel tensor so that the channel dimension is the
     * first non-batch axis.
     *
     * <p>Many {@link org.datavec.image.loader.BaseImageLoader} subclasses
     * return tensors shaped {@code (1, H, W, 3)} (channels-last). This
     * helper permutes that to {@code (1, 3, H, W)} (channels-first) by
     * concatenating three single-channel slices produced via
     * {@link NDArrayIndex#point(int)}.</p>
     *
     * @param indArray the source tensor, expected to be rank 3 with shape
     *                 {@code [1][3][H*W]} (or compatible). Must not be
     *                 {@code null}.
     * @param height   the target height {@code H}.
     * @param width    the target width {@code W}.
     * @return a freshly allocated {@code (1, 3, height, width)} tensor
     *         carrying the same pixel data as {@code indArray}.
     */
    public static INDArray transpose(INDArray indArray, int height, int width) {
        INDArray one = Nd4j.create(new int[] { 1, height, width });
        one.assign(indArray.get(NDArrayIndex.point(0), NDArrayIndex.point(2)));
        INDArray two = Nd4j.create(new int[] { 1, height, width });
        two.assign(indArray.get(NDArrayIndex.point(0), NDArrayIndex.point(1)));
        INDArray three = Nd4j.create(new int[] { 1, height, width });
        three.assign(indArray.get(NDArrayIndex.point(0), NDArrayIndex.point(0)));
        return Nd4j.concat(0, one, two, three).reshape(new int[] { 1, 3, height, width });
    }

    /**
     * Computes the squared Euclidean distance between two equally-shaped
     * tensors.
     *
     * <p>Delegates straight to ND4J's {@link INDArray#distance2(INDArray)}
     * which is preferred over the plain {@link INDArray#distance(INDArray)}
     * when the caller only needs the relative ordering of distances (such
     * as the {@code < 0.45} threshold used by
     * {@link Nd4jTemplate#match(byte[], byte[])}).</p>
     *
     * @param a the first tensor; must not be {@code null}.
     * @param b the second tensor; must not be {@code null} and must
     *          broadcast with {@code a}.
     * @return the squared Euclidean distance between {@code a} and
     *         {@code b}.
     */
    public static double distance(INDArray a, INDArray b) {
        return a.distance2(b);
    }
}