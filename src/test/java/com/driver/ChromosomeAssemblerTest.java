package com.driver;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Created by sahil on 8/23/16.
 */
public class ChromosomeAssemblerTest {

    private static final ChromosomeAssembler.StringOverlapper overlapper = new ChromosomeAssembler.NaiveStringOverlapper();

    @Test
    public void testCannotCombineBecauseNotMoreThanHalfOverlapFromBack() {
        final String a = "ATTAGACCTG";
        final String b = "CCTGCCGGAA";

        assertEquals(Optional.empty(), overlapper.computeOverlap(a, b));
        assertEquals(Optional.empty(), overlapper.computeOverlap(b, a));

    }

    @Test
    public void testCannotCombineBecauseNotMoreThanHalfOverlapFromFront() {
        final String a = "CCTGGAATAG";
        final String b = "TTGGTACCTG";

        assertEquals(Optional.empty(), overlapper.computeOverlap(a, b));
        assertEquals(Optional.empty(), overlapper.computeOverlap(b, a));

    }

    @Test
    public void testCombineEndOfBiggerStringWithStartOfSmallerString() {
        final String a = "ATTAGAC";
        final String b = "CTATTA";

        assertEquals("CTATTAGAC", overlapper.computeOverlap(a, b).get());
        assertEquals("CTATTAGAC", overlapper.computeOverlap(b, a).get());

    }

    @Test
    public void testCombineStartOfBiggerStringWithEndOfSmallerString() {
        final String a = "ATTAGAC";
        final String b = "AGACT";

        assertEquals("ATTAGACT", overlapper.computeOverlap(a, b).get());
        assertEquals("ATTAGACT", overlapper.computeOverlap(b, a).get());

    }
}
