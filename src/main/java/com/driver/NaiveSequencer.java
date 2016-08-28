package com.driver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Naive sequencer that figures out the unique overlap between each fragment
 * by trying each fragment against every other fragment. O(n^2 * time of overlap algorithm)
 */
public class NaiveSequencer
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
}
