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

import java.util.concurrent.TimeUnit;

import org.nd4j.linalg.api.ndarray.INDArray;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * In-process implementation of {@link INDArrayStoreProvider} backed by a
 * Guava {@link LoadingCache}.
 *
 * <p>Embeddings are keyed by the {@code group-memberId} string and expire
 * after 29 days of inactivity. The cache is capped at 10 entries with an
 * initial capacity of 2; eviction triggers are logged to {@link System#out}
 * via the registered {@link RemovalListener}.</p>
 *
 * <p>The cache uses an "always-absent" {@link CacheLoader} that returns
 * {@link Optional#absent()} on lookup misses; this allows {@link #get} to
 * distinguish a hit from a miss without falling back to {@code null}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see INDArrayStoreProvider
 * @see LoadingCache
 */
public class INDArrayLocalCacheStoreProvider implements INDArrayStoreProvider {

    /**
     * Guava-backed cache storing optional INDArray embeddings keyed by the
     * {@code group-memberId} pair. Configured for an 8-thread concurrency
     * level, a 29-day expiry after write, an initial capacity of 2, a
     * maximum size of 10 entries, with statistics recording enabled.
     */
    private final LoadingCache<String, Optional<INDArray>> indArrayCaches = CacheBuilder.newBuilder()
            .concurrencyLevel(8)
            .expireAfterWrite(29, TimeUnit.DAYS)
            .initialCapacity(2)
            .maximumSize(10)
            .recordStats()
            .removalListener(new RemovalListener<String, Optional<INDArray>>() {
                @Override
                public void onRemoval(RemovalNotification<String, Optional<INDArray>> notification) {
                    System.out.println(notification.getKey() + " was removed, cause is " + notification.getCause());
                }
            })
            .build(new CacheLoader<String, Optional<INDArray>>() {
                @Override
                public Optional<INDArray> load(String keySecret) throws Exception {
                    return Optional.fromNullable(null);
                }
            });

    /**
     * Stores (or replaces) the embedding under the composite
     * {@code group-memberId} key.
     *
     * @param group    the logical grouping key; must not be {@code null}.
     * @param memberId the member identifier inside {@code group}; must
     *                 not be {@code null}.
     * @param ndarray  the embedding vector to store; must not be
     *                 {@code null}.
     */
    @Override
    public void store(String group, String memberId, INDArray ndarray) {
        indArrayCaches.put(String.join("-", group, memberId), Optional.of(ndarray));
    }

    /**
     * Retrieves the embedding stored under {@code group-memberId}.
     *
     * @param group    the logical grouping key; must not be {@code null}.
     * @param memberId the member identifier inside {@code group}; must
     *                 not be {@code null}.
     * @return the previously stored {@link INDArray}, or {@code null} when
     *         no embedding has been stored for the pair.
     */
    @Override
    public INDArray get(String group, String memberId) {
        Optional<INDArray> ndarray = indArrayCaches.getIfPresent(String.join("-", group, memberId));
        return ndarray.isPresent() ? ndarray.get() : null;
    }
}