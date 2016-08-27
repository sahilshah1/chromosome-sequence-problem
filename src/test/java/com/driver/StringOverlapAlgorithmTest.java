package com.driver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by sahil on 8/23/16.
 */
@RunWith(Parameterized.class)
public class StringOverlapAlgorithmTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { new ChromosomeAssembler.NaiveOverlapAlgorithm() }
        });
    }

    private ChromosomeAssembler.StringOverlapAlgorithm algorithm;

    public StringOverlapAlgorithmTest(final ChromosomeAssembler.StringOverlapAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Test
    public void testOverlap() {
        final String a = "TTGACTA";
        final String b = "ACTAC";

        assertEquals(3, this.algorithm.computeOverlapIndex(a, b));
    }

    @Test
    public void testNoOverlap() {
        final String a = "AGACT";
        final String b = "TTCCA";

        assertEquals(-1, this.algorithm.computeOverlapIndex(a, b));
    }

    @Test
    public void testLessThanMajorityOfSuffixOverlap() {
        final String a = "AAGACT";
        final String b = "ACTA";

        assertEquals(-1, this.algorithm.computeOverlapIndex(a, b));
    }

    @Test
    public void testLessThanMajorityOfPrefixOverlap() {
        final String a = "GACT";
        final String b = "ACTAAA";

        assertEquals(-1, this.algorithm.computeOverlapIndex(a, b));
    }
}
