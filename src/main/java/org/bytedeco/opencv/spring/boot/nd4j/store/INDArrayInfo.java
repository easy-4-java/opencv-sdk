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

import lombok.Data;

/**
 * Immutable-style value object representing a single cached face embedding.
 *
 * <p>An {@code INDArrayInfo} bundles the logical group and member identifier
 * of a face together with its {@link INDArray} embedding vector. It is the
 * canonical entry stored by implementations of
 * {@link INDArrayStoreProvider}.</p>
 *
 * <p>The class is annotated with Lombok's {@link Data}, so getters, setters,
 * {@code toString()}, {@code equals(Object)} and {@code hashCode()} are
 * generated at compile time.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see INDArrayStoreProvider
 * @see INDArrayLocalCacheStoreProvider
 */
@Data
public class INDArrayInfo {

    /**
     * Logical grouping key (for example {@code "employees"}). Combined
     * with {@link #memberId} it forms the cache key used by the
     * {@link INDArrayStoreProvider}.
     */
    private String group;

    /**
     * Member identifier inside the group (for example a primary key).
     * Combined with {@link #group} it forms the cache key.
     */
    private String memberId;

    /**
     * The actual embedding vector produced by the FaceNet-style model
     * for this member.
     */
    private INDArray ndarray;
}