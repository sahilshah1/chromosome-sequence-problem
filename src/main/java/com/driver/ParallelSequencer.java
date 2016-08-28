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
 * Clean, parallel implementation of the {@link NaiveSequencer}. O(n^2 * time of overlap algorithm),
 * but with work split up by threads.
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
        final Map<String, SequencedToNext> rawStringToSequenced = new ConcurrentHashMap<>();

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
            final SequencedToNext sequencedToNext = rawStringToSequenced.get(currentSequence);
            sequencedFragments.add(sequencedToNext.sequencedFragment);
            currentSequence = sequencedToNext.nextFragment;
        }
        sequencedFragments.add(new SequencedFragment(currentSequence, -1));

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
     * @param rawStringToSequenced maps each raw string to a {@link SequencedToNext} to enable lookup by
     *                             raw String, since the next immutable {@link SequencedFragment} is not known when
     *                             computing the current immutable {@link SequencedFragment}
     */
    private void fillOverlapMatrixAndSequenceMap(final List<String> fragments,
                                                 final StringOverlapAlgorithm algorithm,
                                                 final int[][] overlapMatrix,
                                                 final Map<String, SequencedToNext> rawStringToSequenced) {
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
                            final SequencedToNext sequencedToNext = new SequencedToNext(
                                    new SequencedFragment(suffixString, overlapMatrix[row][col]),
                                    prefixString);
                            rawStringToSequenced.put(suffixString, sequencedToNext);
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

    /**
     * Simple POJO that referenced {@link SequencedFragment}s and the String that
     * is supposed to come after the sequence.
     */
    private static class SequencedToNext {
        final SequencedFragment sequencedFragment;
        final String nextFragment;

        SequencedToNext(final SequencedFragment sequencedFragment, final String nextFragment) {
            this.sequencedFragment = sequencedFragment;
            this.nextFragment = nextFragment;
        }

        @Override
        public String toString() {
            return this.sequencedFragment.toString() + ", next=" + this.nextFragment;
        }
    }

    @Override
    public String toString() {
        return ParallelSequencer.class.getSimpleName() + " (numThreads=" + this.numWorkers + ")";
    }
}
