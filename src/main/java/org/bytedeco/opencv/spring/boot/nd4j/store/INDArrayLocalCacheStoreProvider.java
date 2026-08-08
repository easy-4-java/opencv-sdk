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
 * Local cache-backed INDArray store.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
public class INDArrayLocalCacheStoreProvider implements INDArrayStoreProvider {

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

	@Override
	public void store(String group, String memberId, INDArray ndarray) {
		indArrayCaches.put(String.join("-", group, memberId), Optional.of(ndarray));
	}

	@Override
	public INDArray get(String group, String memberId) {
		Optional<INDArray> ndarray = indArrayCaches.getIfPresent(String.join("-", group, memberId));
		return ndarray.isPresent() ? ndarray.get() : null;
	}
}
