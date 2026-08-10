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

/**
 * Axis-aligned rectangle describing a face region of interest.
 *
 * <p>This POJO mirrors the legacy ArcSoft {@code com.arcsoft.face.Rect}
 * contract so that detection results from either the native SDK or the
 * JavaCV/OpenCV pipeline can be exchanged without conversion. Coordinates
 * are pixel-space and inclusive of the {@link #left} and {@link #top}
 * borders but exclusive of {@link #right} and {@link #bottom} (the
 * standard "half-open" rectangle convention).</p>
 *
 * <p>Instances are mutable; callers can either use the constructor with
 * individual edge coordinates or the copy constructor that deep-copies
 * another {@code Rect}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see ImageFactory#getBestRect(int, int, Rect)
 */
public class Rect {
    /**
     * X-coordinate of the leftmost edge of the rectangle (inclusive).
     */
    public int left;

    /**
     * Y-coordinate of the topmost edge of the rectangle (inclusive).
     */
    public int top;

    /**
     * X-coordinate of the rightmost edge of the rectangle (exclusive).
     */
    public int right;

    /**
     * Y-coordinate of the bottommost edge of the rectangle (exclusive).
     */
    public int bottom;

    /**
     * No-argument constructor that initialises all four edges to {@code 0}.
     */
    public Rect() {
    }

    /**
     * Constructs a rectangle from the four explicit edge coordinates.
     *
     * @param left   the X-coordinate of the left edge (inclusive).
     * @param top    the Y-coordinate of the top edge (inclusive).
     * @param right  the X-coordinate of the right edge (exclusive).
     * @param bottom the Y-coordinate of the bottom edge (exclusive).
     */
    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Copy constructor that produces a deep copy of {@code r}. When the
     * argument is {@code null} the result is initialised with all four
     * edges set to {@code 0}.
     *
     * @param r the source rectangle to copy, or {@code null} for an
     *          all-zero rectangle.
     */
    public Rect(Rect r) {
        if (r == null) {
            this.left = this.top = this.right = this.bottom = 0;
        } else {
            this.left = r.left;
            this.top = r.top;
            this.right = r.right;
            this.bottom = r.bottom;
        }
    }

    /**
     * Returns a debug-friendly representation of the rectangle.
     *
     * <p>The output mirrors the legacy ArcSoft {@code toString()} format
     * to ease log correlation with native components:
     * {@code com.arcsoft.face.Rect(left, top - right, bottom)}.</p>
     *
     * @return a formatted string describing the four edges.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("com.arcsoft.face.Rect(");
        sb.append(this.left);
        sb.append(", ");
        sb.append(this.top);
        sb.append(" - ");
        sb.append(this.right);
        sb.append(", ");
        sb.append(this.bottom);
        sb.append(")");
        return sb.toString();
    }

    /**
     * Returns the X-coordinate of the left edge (inclusive).
     *
     * @return the left edge.
     */
    public int getLeft() {
        return left;
    }

    /**
     * Updates the X-coordinate of the left edge (inclusive).
     *
     * @param left the new left edge.
     */
    public void setLeft(int left) {
        this.left = left;
    }

    /**
     * Returns the Y-coordinate of the top edge (inclusive).
     *
     * @return the top edge.
     */
    public int getTop() {
        return top;
    }

    /**
     * Updates the Y-coordinate of the top edge (inclusive).
     *
     * @param top the new top edge.
     */
    public void setTop(int top) {
        this.top = top;
    }

    /**
     * Returns the X-coordinate of the right edge (exclusive).
     *
     * @return the right edge.
     */
    public int getRight() {
        return right;
    }

    /**
     * Updates the X-coordinate of the right edge (exclusive).
     *
     * @param right the new right edge.
     */
    public void setRight(int right) {
        this.right = right;
    }

    /**
     * Returns the Y-coordinate of the bottom edge (exclusive).
     *
     * @return the bottom edge.
     */
    public int getBottom() {
        return bottom;
    }

    /**
     * Updates the Y-coordinate of the bottom edge (exclusive).
     *
     * @param bottom the new bottom edge.
     */
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }
}