package com.driver;

import java.util.List;

/**
 * Created by sahil on 8/28/16.
 */
interface Sequencer {
    /**
     * Sequences a list of fragments and returns the sequenced fragments.
     *
     * @param fragments  list of unordered fragments that overlap
     * @param algorithm algorithm to detect overlap index of 2 strings
     * @return ordered list of fragments that overlap
     */
    String sequence(List<String> fragments, final StringOverlapAlgorithm algorithm);

    /**
     * POJO that describes how much of the companion prefix string (not included in POJO)
     * overlaps with the fragment.
     */
    class SequencedFragment {
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


        static String combineSequencedFragments(final List<SequencedFragment> sequencedFragments) {
            final StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(sequencedFragments.get(0).fragment);
            for (int i = 0; i < sequencedFragments.size() - 1; i++) {
                final int overlap = sequencedFragments.get(i).overlapIndexOfFollowingPrefix;
                stringBuilder.append(sequencedFragments.get(i + 1).fragment.substring(overlap + 1));
            }

            return stringBuilder.toString();
        }
    }
}
