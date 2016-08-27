package com.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by sahil on 8/23/16.
 */
public class ChromosomeAssembler {

    public static void main(final String[] args)
            throws IOException {
        final Path filePath = Paths.get(args[0]);
        final List<String> fragments = readFragmentsFromDataFile(filePath);
        final String sequenced = new NaiveSequencer().sequence(fragments, new NaiveOverlapAlgorithm());
        System.out.println(sequenced);
    }

    public static List<String> readFragmentsFromDataFile(final Path path)
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

    public static class NaiveSequencer
        implements Sequencer {

        public String sequence(final List<String> fragments, final StringOverlapAlgorithm overlapper) {
            final int fragmentsToSequence = fragments.size();

            //find the beginning of the sequence. the one string for which no other string has an overlapping suffix
            String startingFragment = "";
            for (int i = 0; i < fragments.size(); i++) {
                final String prefixString = fragments.get(i);
                boolean foundOverlappingSuffix = false;
                for (int j = 0; j < fragments.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    if (overlapper.computeOverlapIndex(fragments.get(j), prefixString) != -1) {
                        foundOverlappingSuffix = true;
                        break;
                    }
                }
                if (!foundOverlappingSuffix) {
                    startingFragment = prefixString;
                    fragments.remove(i);
                    break;
                }
            }

            //find the matching prefix for each suffix
            final List<SequencedFragment> sequencedFragments = new ArrayList<>(fragments.size());
            String currentSuffix = startingFragment;
            while (fragmentsToSequence != sequencedFragments.size()) {

                //end of the fragments
                if (fragments.size() == 0) {
                    sequencedFragments.add(new SequencedFragment(currentSuffix, -1));
                }

                //when searching for a suffix
                for (int i = 0; i < fragments.size(); i++) {
                    final int overlap = overlapper.computeOverlapIndex(currentSuffix, fragments.get(i));
                    if (overlap != -1) {
                        sequencedFragments.add(new SequencedFragment(currentSuffix, overlap));
                        currentSuffix = fragments.get(i);
                        fragments.remove(i);
                        break;
                    }
                }
            }


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

    public static class SequencedFragment {
        public final String fragment;
        public final int overlapIndexOfFollowingPrefix;

        public SequencedFragment(final String fragment, final int overlapIndexOfFollowingPrefix) {
            this.fragment = fragment;
            this.overlapIndexOfFollowingPrefix = overlapIndexOfFollowingPrefix;
        }

        @Override
        public String toString() {
            return this.fragment + "(" + this.overlapIndexOfFollowingPrefix + ")";
        }
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
}
