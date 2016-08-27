package com.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sahil on 8/23/16.
 */
public class ChromosomeSequencer {

    public static void main(final String[] args)
            throws IOException {
        final Path filePath = Paths.get(args[0]);
        final List<String> fragments = readFragmentsFromDataFile(filePath);
        final String sequenced = new NaiveSequencer().sequence(fragments, new KMPAlgorithm());
        System.out.println(sequenced);
    }

    static List<String> readFragmentsFromDataFile(final Path path)
            throws IOException {
        final List<String> fragments = new ArrayList<>();
        try (final BufferedReader br = Files.newBufferedReader(path)) {
            StringBuilder fragment = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(">")) { //indication that a new fragment is beginning
                    if (fragment.length() > 0) {
                        fragments.add(fragment.toString());
                        fragment = new StringBuilder();
                    }
                }
                else {
                    fragment.append(line);
                }
            }
            //after last line, add the remaining fragment
            fragments.add(fragment.toString());
        }
        return fragments;
    }

    interface Sequencer {
        /**
         * Sequences a list of fragments and returns the sequenced fragments.
         * @param fragments list of unordered fragments that overlap
         * @param overlapper algorithm to detect overlap of 2 strings
         * @return ordered list of fragments that overlap
         */
        String sequence(List<String> fragments, final StringOverlapAlgorithm overlapper);
    }

    /**
     * Naive sequencer that figures out the unique overlap between each fragment
     * by trying each fragment against every other fragment. O(n^2 * time of overlap algorithm)
     */
    static class NaiveSequencer
        implements Sequencer {

        public String sequence(final List<String> fragments, final StringOverlapAlgorithm algorithm) {
            //save some ms of perf by not removing from original fragments list and recording sequenced indexes
            final Set<Integer> sequencedIndexes = new HashSet<>();

            //find the beginning of the sequence. the one string for which no other string has an overlapping suffix
            String startingFragment = "";
            for (int i = 0; i < fragments.size(); i++) {
                final String prefixString = fragments.get(i);
                boolean foundOverlappingSuffix = false;
                for (int j = 0; j < fragments.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    if (algorithm.computeOverlapIndex(fragments.get(j), prefixString) != -1) {
                        foundOverlappingSuffix = true;
                        break;
                    }
                }
                if (!foundOverlappingSuffix) {
                    startingFragment = prefixString;
                    sequencedIndexes.add(i);
                    break;
                }
            }

            //find the matching prefix for each suffix
            final List<SequencedFragment> sequencedFragments = new ArrayList<>(fragments.size());
            String currentSuffix = startingFragment;

            while (sequencedIndexes.size() != fragments.size()) {
                for (int i = 0; i < fragments.size(); i++) {
                    if (sequencedIndexes.contains(i)) {
                        continue;
                    }

                    final int overlap = algorithm.computeOverlapIndex(currentSuffix, fragments.get(i));
                    if (overlap != -1) {
                        sequencedFragments.add(new SequencedFragment(currentSuffix, overlap));
                        currentSuffix = fragments.get(i);
                        sequencedIndexes.add(i);
                        break;
                    }
                }
            }
            sequencedFragments.add(new SequencedFragment(currentSuffix, -1));

            return combineSequencedFragments(sequencedFragments);
        }

        private static String combineSequencedFragments(final List<SequencedFragment> sequencedFragments) {
            final StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(sequencedFragments.get(0).fragment);
            for (int i = 0; i < sequencedFragments.size() - 1; i++) {
                final int overlap = sequencedFragments.get(i).overlapIndexOfFollowingPrefix;
                stringBuilder.append(sequencedFragments.get(i + 1).fragment.substring(overlap + 1));
            }

            return stringBuilder.toString();
        }
    }

    /**
     * POJO that describes how much of the companion prefix string (not included in POJO)
     * overlaps with the fragment.
     */
    private static class SequencedFragment {
        final String fragment;
        final int overlapIndexOfFollowingPrefix;

        SequencedFragment(final String fragment, final int overlapIndexOfFollowingPrefix) {
            this.fragment = fragment;
            this.overlapIndexOfFollowingPrefix = overlapIndexOfFollowingPrefix;
        }

        @Override
        public String toString() {
            return this.fragment + "(" + this.overlapIndexOfFollowingPrefix + ")";
        }
    }


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
    }

    /**
     * Naive algorithm that slides the suffix string across the beginning of the prefix
     * String, and does backtracking for every character. O(mn)
     */
    static class NaiveOverlapAlgorithm
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
    static class KMPAlgorithm
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
