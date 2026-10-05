package com.ossprj.commons.web.facade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class FuzzyWaitTest {

    @Test
    public void testFuzzyWait() {
        FuzzyWait fuzzyWait = new FuzzyWait();
        assertDoesNotThrow(() -> fuzzyWait.perform(10, 5));
    }
}
