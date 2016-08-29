package com.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Naive sequencer that figures out the unique overlap between each fragment
 * by trying each fragment against every other fragment. O(n^2 * time of overlap algorithm)
 * The computation to calculate overlaps can be divided by n number of threads.
 *
 * Created by sahil on 8/28/16.
 */
public class ParallelSequencer
        implements Sequencer {

    private final int numWorkers;
    private final ExecutorService threadPool;

    public ParallelSequencer(final int numWorkers) {
        this.numWorkers = numWorkers;
        this.threadPool = Executors.newFixedThreadPool(numWorkers);
    }

    @Override
    public String sequence(final List<String> fragments, final StringOverlapAlgorithm algorithm) {

        final int[][] overlapMatrix = new int[fragments.size()][fragments.size()];
        final Map<String, SequencedFragment> rawStringToSequenced = new ConcurrentHashMap<>();

        fillOverlapMatrixAndSequenceMap(fragments, algorithm, overlapMatrix, rawStringToSequenced);

        //find the first string in the sequence. that will be the string for
        //that is not a prefix for any other string (all the values in the column will be -1)
        final OptionalInt indexWithNoOverlap = IntStream.range(0, overlapMatrix[0].length)
                .filter(col ->
                        IntStream.range(0, overlapMatrix.length)
                                .allMatch(row -> overlapMatrix[row][col] == -1))
                .findFirst();
        if (!indexWithNoOverlap.isPresent()) {
            System.out.println("No string without suffix found!");
            return null;
        }
        String currentSequence = fragments.get(indexWithNoOverlap.getAsInt());


        //follow the rawStringToSequenced map to assemble the sequencedFragment
        final List<SequencedFragment> sequencedFragments = new ArrayList<>(fragments.size());
        while (sequencedFragments.size() < fragments.size() - 1) {
            final SequencedFragment sequencedFragment = rawStringToSequenced.get(currentSequence);
            sequencedFragments.add(sequencedFragment);
            currentSequence = sequencedFragment.nextFragment;
        }
        sequencedFragments.add(new SequencedFragment(currentSequence, -1, ""));

        return SequencedFragment.combineSequencedFragments(sequencedFragments);
    }

    /**
     * For every fragment in the fragments list, compute the overlap with every other string
     * and save the results in a matrix. Computing the overlap is split up among threads.
     * Rows [0, fragments.length) and columns [0, fragments.length] refer to each index in
     * the fragments list.
     *
     * Example, given fragments:
     * [ATTAGACCTG, CCTGCCGGAA, AGACCTGCCG, GCCGGAATAC]
     *
     * The resulting matrix is:
     * ATTAGACCTG [-1, -1,  6, -1]
     * CCTGCCGGAA [-1, -1, -1,  6]
     * AGACCTGCCG [-1,  6, -1, -1]
     * GCCGGAATAC [-1, -1, -1, -1]
     *
     * The resulting rawStringToSequence is:
     * {ATTAGACCTG=ATTAGACCTG(6), next=AGACCTGCCG,
     *  AGACCTGCCG=AGACCTGCCG(6), next=CCTGCCGGAA,
     *  CCTGCCGGAA=CCTGCCGGAA(6), next=GCCGGAATAC}
     *
     * @param fragments list of fragments
     * @param algorithm string overlap algorithm
     * @param overlapMatrix -1 per index if there is no valid overlap, otherwise a number indicating
     *                      what the overlap is
     * @param rawStringToSequenced maps each raw string to a {@link SequencedFragment} to enable lookup by
     *                             raw String
     */
    private void fillOverlapMatrixAndSequenceMap(final List<String> fragments,
                                                 final StringOverlapAlgorithm algorithm,
                                                 final int[][] overlapMatrix,
                                                 final Map<String, SequencedFragment> rawStringToSequenced) {
        final List<Callable<Void>> tasks = new ArrayList<>(this.numWorkers);

        for (int i = 0; i < this.numWorkers; i++) {
            //divide the work up by allocating rows per thread
            final int startRowInclusive = fragments.size() / this.numWorkers * i;
            final int endRowExclusive = i == this.numWorkers - 1 ?
                    fragments.size() : fragments.size() / this.numWorkers * (i + 1);

            tasks.add(() -> {
                for (int row = startRowInclusive; row < endRowExclusive; row++) {
                    final String suffixString = fragments.get(row);
                    for (int col = 0; col < overlapMatrix[row].length; col++) {

                        if (row == col) { //do not overlap with self
                            overlapMatrix[row][col] = -1;
                            continue;
                        }

                        final String prefixString = fragments.get(col);
                        overlapMatrix[row][col] = algorithm.computeOverlapIndex(suffixString, prefixString);

                        //if overlap exists, it means the unique pairing between suffix and prefix
                        //is found. add to map
                        if (overlapMatrix[row][col] > 0) {
                            rawStringToSequenced.put(suffixString,
                                    new SequencedFragment(suffixString, overlapMatrix[row][col], prefixString));
                        }
                    }
                }
                return null;
            });

        }

        try {
            this.threadPool.invokeAll(tasks);
        }
        catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return ParallelSequencer.class.getSimpleName() + " (numThreads=" + this.numWorkers + ")";
    }

    /**
     * POJO that describes how much of the companion prefix string
     * overlaps with the fragment.
     */
    public static class SequencedFragment {
        final String fragment;
        final int overlapIndexOfFollowingPrefix;
        final String nextFragment;

        SequencedFragment(final String fragment, final int overlapIndexOfFollowingPrefix, final String nextFragment) {
            this.fragment = fragment;
            this.overlapIndexOfFollowingPrefix = overlapIndexOfFollowingPrefix;
            this.nextFragment = nextFragment;
        }

        @Override
        public String toString() {
            return this.fragment + "(" + this.overlapIndexOfFollowingPrefix + ")" + this.nextFragment;
        }


        static String combineSequencedFragments(final List<SequencedFragment> sequencedFragments) {
            final StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(sequencedFragments.get(0).fragment);
            for (int i = 0; i < sequencedFragments.size() - 1; i++) {
                final int overlap = sequencedFragments.get(i).overlapIndexOfFollowingPrefix;
                final String nextFrag = sequencedFragments.get(i + 1).fragment;
                stringBuilder.append(nextFrag, overlap + 1, nextFrag.length());
            }

            return stringBuilder.toString();
        }
    }
}
