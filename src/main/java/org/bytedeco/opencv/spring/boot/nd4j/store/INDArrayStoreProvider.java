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
package org.bytedeco.opencv.spring.boot.nd4j.store;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Storage abstraction for face embedding {@link INDArray} vectors.
 *
 * <p>Implementations decide how and where to persist the embeddings;
 * the canonical in-process implementation is
 * {@link INDArrayLocalCacheStoreProvider}, but the interface is also
 * suitable for wrappers around Redis, Hazelcast, JDBC, and so on.</p>
 *
 * <p>Cache keys are derived by implementations from the
 * {@code (group, memberId)} pair; the {@link INDArray} value is the
 * embedding produced by the FaceNet-style model.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see INDArrayLocalCacheStoreProvider
 * @see INDArrayInfo
 */
public interface INDArrayStoreProvider {

    /**
     * Persists (or replaces) the embedding associated with the
     * {@code (group, memberId)} pair.
     *
     * @param group    the logical grouping key; must not be {@code null}.
     * @param memberId the member identifier inside {@code group}; must
     *                 not be {@code null}.
     * @param ndarray  the embedding vector to store; must not be
     *                 {@code null}.
     */
    void store(String group, String memberId, INDArray ndarray);

    /**
     * Retrieves the embedding associated with the {@code (group, memberId)}
     * pair.
     *
     * @param group    the logical grouping key; must not be {@code null}.
     * @param memberId the member identifier inside {@code group}; must
     *                 not be {@code null}.
     * @return the previously stored {@link INDArray}, or {@code null} when
     *         no embedding has been stored for the pair.
     */
    INDArray get(String group, String memberId);
}