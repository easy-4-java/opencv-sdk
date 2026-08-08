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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link INDArrayInfo}.
 *
 * <p>The class is a Lombok {@code @Data} value object, so the tests
 * verify getters / setters and the generated {@code equals()},
 * {@code hashCode()} and {@code toString()} methods.</p>
 *
 * @since 3.0.0
 */
class INDArrayInfoTest {

    /**
     * Verifies the no-arg constructor leaves every field {@code null}.
     */
    @Test
    void shouldStartWithAllFieldsNull() {
        INDArrayInfo info = new INDArrayInfo();

        assertNull(info.getGroup());
        assertNull(info.getMemberId());
        assertNull(info.getNdarray());
    }

    /**
     * Verifies that setters populate the fields and getters return
     * the same values.
     */
    @Test
    void shouldAcceptSetterValues() {
        INDArrayInfo info = new INDArrayInfo();

        info.setGroup("employees");
        info.setMemberId("42");

        assertEquals("employees", info.getGroup());
        assertEquals("42", info.getMemberId());
    }

    /**
     * Verifies that two instances with identical field values are
     * considered equal by Lombok's generated {@code equals()}.
     */
    @Test
    void shouldBeEqualWhenFieldsMatch() {
        INDArrayInfo a = new INDArrayInfo();
        a.setGroup("g1");
        a.setMemberId("m1");

        INDArrayInfo b = new INDArrayInfo();
        b.setGroup("g1");
        b.setMemberId("m1");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * Verifies that instances with different group values are not equal.
     */
    @Test
    void shouldNotBeEqualWhenGroupDiffers() {
        INDArrayInfo a = new INDArrayInfo();
        a.setGroup("g1");
        a.setMemberId("m1");

        INDArrayInfo b = new INDArrayInfo();
        b.setGroup("g2");
        b.setMemberId("m1");

        assertNotEquals(a, b);
    }

    /**
     * Verifies that instances with different memberId values are not equal.
     */
    @Test
    void shouldNotBeEqualWhenMemberIdDiffers() {
        INDArrayInfo a = new INDArrayInfo();
        a.setGroup("g1");
        a.setMemberId("m1");

        INDArrayInfo b = new INDArrayInfo();
        b.setGroup("g1");
        b.setMemberId("m2");

        assertNotEquals(a, b);
    }

    /**
     * Verifies that {@code toString()} includes field names.
     */
    @Test
    void shouldProduceMeaningfulToString() {
        INDArrayInfo info = new INDArrayInfo();
        info.setGroup("test-group");
        info.setMemberId("test-member");

        String text = info.toString();
        assertTrue(text.contains("test-group"));
        assertTrue(text.contains("test-member"));
    }

    /**
     * Verifies that an {@link INDArrayInfo} is not equal to {@code null}.
     */
    @Test
    void shouldNotBeEqualToNull() {
        INDArrayInfo info = new INDArrayInfo();
        info.setGroup("g");
        info.setMemberId("m");

        assertNotEquals(null, info);
    }
}
