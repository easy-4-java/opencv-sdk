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

/**
 * Catalogue of supported image loader strategies.
 *
 * <p>The enum values map to specific subclasses of
 * {@link org.datavec.image.loader.BaseImageLoader} supplied by DataVec.
 * Callers pick one strategy and {@link Nd4jTemplate} delegates the actual
 * decoding to the matching loader.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 */
public enum ImageLoader {

    /**
     * Loader specialised for the CIFAR-10 dataset.
     *
     * <p>Reference: <em>Learning Multiple Layers of Features from Tiny
     * Images</em>, Alex Krizhevsky, 2009. Uses a custom pre-processor
     * that normalises the dataset based on Sergey Zagoruyko's example
     * (<a href="https://github.com/szagoruyko/cifar.torch">szagoruyko/cifar.torch</a>).</p>
     *
     * @see org.datavec.image.loader.CifarLoader
     */
    CIFAR,

    /**
     * Generic image-to-matrix loader, the default choice for arbitrary
     * JPEG/PNG inputs.
     *
     * @see org.datavec.image.loader.ImageLoader
     */
    DEFAULT,

    /**
     * Loader for the Labeled Faces in the Wild (LFW) dataset.
     *
     * <p>The dataset contains 5,749 individuals: 1,680 people have two or
     * more images and 4,069 people have a single image. Most images are
     * 250&times;250 colour JPEGs (a few are grayscale). Image dimensions
     * can be customised through the loader configuration.</p>
     */
    LFW,

    /**
     * JavaCV-backed native image loader. Supports BMP, GIF, JPG, JPEG,
     * JP2, PBM, PGM, PPM, PNM, PNG, TIF, TIFF, EXR and WebP.
     *
     * @see org.datavec.image.loader.NativeImageLoader
     */
    NATIVE;

}