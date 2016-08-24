package com.driver;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by sahil on 8/23/16.
 */
public class ChromosomeAssemblerTest {

    @Test
    public void testLps() {
        final int[] expected = {0, 1, 0, 1, 2, 3, 4, 5, 2};
        final int[] lps = ChromosomeAssembler.calculateLps("aabaabaaa");

        assertArrayEquals(expected, lps);
    }

    @Test
    public void testOverlap() {
        final int[] expected = {0, 1, 0, 1, 2, 3, 4, 5, 2};
        final int[] lps = ChromosomeAssembler.calculateLps("aabaabaaa");
        final String a = "ABABDABACDABABCABAB";
        final String b = "ABABCABAB";

        ChromosomeAssembler.findOverlapIndex(a, b);
    }

    @Test
    public void testCannotCombine() {
        final int[] expected = {0, 1, 0, 1, 2, 3, 4, 5, 2};
        final String a = "ATTAGACCTG";
        final String b = "CCTGCCGGAA";

        ChromosomeAssembler.findOverlapIndex(a, b);
    }

    @Test
    public void testCombineStartOfAAndEndOfB() {
        final int[] expected = {0, 1, 0, 1, 2, 3, 4, 5, 2};
        final String a = "AGACCTGCCG";
        final String b = "GTGTAGACCT";

        ChromosomeAssembler.findOverlapIndex(a, b);
    }
}
