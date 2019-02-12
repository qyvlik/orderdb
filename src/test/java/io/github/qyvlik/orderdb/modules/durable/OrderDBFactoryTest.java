package io.github.qyvlik.orderdb.modules.durable;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OrderDBFactoryTest {

    @Test
    public void testMatchScope() {
        final String regexStr = "^[\\.0-9a-zA-Z_-]+$";
        assertTrue("", "a-a".matches(regexStr));
        assertTrue("", "a_a".matches(regexStr));
        assertTrue("", "1_a".matches(regexStr));
        assertTrue("", "1a".matches(regexStr));
        assertTrue("", "z1".matches(regexStr));
        assertTrue("", "z1-_-_-_".matches(regexStr));
        assertTrue("", ".z1-_-_-_".matches(regexStr));
        assertTrue("", "1.---1.".matches(regexStr));
        assertTrue("", !"1. ---1.".matches(regexStr));
    }
}