package com.driver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sahil on 8/27/16.
 */
interface StringOverlapAlgorithm {
    /**
     * Attempts to compute an overlap between String a and String b. An
     * overlap is defined as when a suffix of A that is longer than A / 2 is equal
     * to a prefix of B that is longer than B / 2.
     * @param suffixString string A
     * @param prefixString string B
     * @return index of end of matching prefix of B (inclusive), or -1 if not found
     */
    int computeOverlapIndex(String suffixString, String prefixString);

    /**
     * Naive algorithm that slides the suffix string across the beginning of the prefix
     * String, and does backtracking for every character. O(mn)
     */
    class NaiveOverlapAlgorithm
        implements StringOverlapAlgorithm {

        @Override
        public int computeOverlapIndex(String suffixString, String prefixString) {
            final int smallerStringLength = Math.min(suffixString.length(), prefixString.length());
            int overlapIndex = -1;
            for (int i = 0; i < smallerStringLength; i++) {
                if (prefixString.substring(0, i + 1).equals(suffixString.substring(suffixString.length() - 1 - i, suffixString.length()))) {
                    overlapIndex = Math.max(overlapIndex, i);
                }
            }

            final boolean isMatchingSuffixLongEnough = overlapIndex + 1 >= suffixString.length() / 2 + 1;
            final boolean isMatchingPrefixLongEnough = overlapIndex + 1 >= prefixString.length() / 2 + 1;
            return isMatchingSuffixLongEnough && isMatchingPrefixLongEnough ? overlapIndex : -1;
        }
    }

    /**
     * Uses Knuth-Morris-Pratt to detect the string overlap. O(m + n)
     */
    class KMPAlgorithm
            implements StringOverlapAlgorithm {

        //performance optimization: if we've already computed the LPS for a String,
        //retrieve it instead of re-computing it
        private static Map<String, int[]> STRING_TO_LPS = new HashMap<>();

        @Override
        public int computeOverlapIndex(final String text, final String pattern) {
            int[] lsp = computeLps(pattern);

            int overlapIndex = -1;
            int j = 0;  // number of chars matched
            for (int i = 0; i < text.length(); i++) {
                // fall back until 0 or the position of the end of the last matching prefix
                while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                    j = lsp[j - 1];
                }
                if (text.charAt(i) == pattern.charAt(j)) {
                    j++; // char matched, increment position

                    //matched portion must be greater than half of each string
                    if (j > text.length() / 2 && j > pattern.length() / 2) {
                        overlapIndex = Math.max(j - 1, overlapIndex);
                    }
                }
            }

            return overlapIndex;
        }

        private static int[] computeLps(final String pattern) {
            if (STRING_TO_LPS.containsKey(pattern)) {
                return STRING_TO_LPS.get(pattern);
            }

            final int[] lsp = new int[pattern.length()];
            lsp[0] = 0;
            for (int i = 1; i < pattern.length(); i++) {
                final char currentLetter = pattern.charAt(i);

                int j = lsp[i - 1];

                //if there is a mismatch, fall back until it sees the same letter again
                //or 0 if it doesn't exist.
                while (j > 0 && currentLetter != pattern.charAt(j)) {
                    j = lsp[j - 1];
                }

                //increment the number of characters seen thus far
                if (currentLetter == pattern.charAt(j)) {
                    j++;
                }

                lsp[i] = j;
            }

            STRING_TO_LPS.put(pattern, lsp);
            return lsp;
        }
    }
}