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
package org.bytedeco.opencv.spring.boot.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Rect}.
 *
 * <p>Covered behaviours: no-arg construction, value-based construction,
 * deep-copy construction (including the {@code null} branch),
 * {@code toString()} format, and the standard bean-style getters /
 * setters.</p>
 *
 * @since 3.0.0
 */
class RectTest {

    /**
     * Verifies the no-arg constructor initialises every edge to
     * {@code 0}.
     */
    @Test
    void shouldDefaultAllEdgesToZero() {
        Rect rect = new Rect();

        assertEquals(0, rect.getLeft());
        assertEquals(0, rect.getTop());
        assertEquals(0, rect.getRight());
        assertEquals(0, rect.getBottom());
        assertEquals(0, rect.left);
        assertEquals(0, rect.top);
        assertEquals(0, rect.right);
        assertEquals(0, rect.bottom);
    }

    /**
     * Verifies the value-based constructor stores the supplied edges
     * verbatim.
     */
    @Test
    void shouldStoreExplicitEdges() {
        Rect rect = new Rect(1, 2, 3, 4);

        assertEquals(1, rect.getLeft());
        assertEquals(2, rect.getTop());
        assertEquals(3, rect.getRight());
        assertEquals(4, rect.getBottom());
    }

    /**
     * Verifies the copy constructor produces a value-equal but
     * reference-distinct rectangle.
     */
    @Test
    void shouldDeepCopyExistingRectangle() {
        Rect source = new Rect(10, 20, 30, 40);
        Rect copy = new Rect(source);

        assertNotSame(source, copy);
        assertEquals(10, copy.getLeft());
        assertEquals(20, copy.getTop());
        assertEquals(30, copy.getRight());
        assertEquals(40, copy.getBottom());

        // Mutating the copy must not bleed back into the source.
        copy.setLeft(99);
        assertEquals(10, source.getLeft());
    }

    /**
     * Verifies the copy constructor's {@code null} branch initialises
     * all four edges to {@code 0}.
     */
    @Test
    void shouldZeroAllEdgesWhenCopyingNull() {
        Rect copy = new Rect((Rect) null);

        assertEquals(0, copy.getLeft());
        assertEquals(0, copy.getTop());
        assertEquals(0, copy.getRight());
        assertEquals(0, copy.getBottom());
    }

    /**
     * Verifies the bean setters update the fields and the public
     * accessors in lockstep.
     */
    @Test
    void shouldUpdateEdgesViaBeanSetters() {
        Rect rect = new Rect();

        rect.setLeft(5);
        rect.setTop(6);
        rect.setRight(7);
        rect.setBottom(8);

        assertEquals(5, rect.left);
        assertEquals(6, rect.top);
        assertEquals(7, rect.right);
        assertEquals(8, rect.bottom);
    }

    /**
     * Verifies {@code toString()} uses the legacy ArcSoft format.
     */
    @Test
    void shouldRenderLegacyArcSoftFormat() {
        Rect rect = new Rect(10, 20, 30, 40);

        String text = rect.toString();

        assertTrue(text.startsWith("com.arcsoft.face.Rect("));
        assertTrue(text.contains("10"));
        assertTrue(text.contains("20"));
        assertTrue(text.contains("30"));
        assertTrue(text.contains("40"));
        assertTrue(text.endsWith(")"));
    }
}