package com.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by sahil on 8/23/16.
 */
public class ChromosomeAssembler {

    public static void main(final String[] args)
            throws IOException {
        final Path filePath = Paths.get(args[0]);

        final List<String> fragments = readFragmentsFromDataFile(filePath);
        final String joined = joinFragments(fragments, new NaiveStringOverlapper());

        System.out.println(joined);

       // final String sequenced = sequenceFragments(fragments);
    }

    private static List<String> readFragmentsFromDataFile(final Path path)
            throws IOException {
        final List<String> fragments = new ArrayList<>();

        try (final BufferedReader br = Files.newBufferedReader(path)) {
            StringBuilder fragment = new StringBuilder();

            //read each line of the file, combining lines of fragments and adding to list
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(">")) { //indication that a new fragment is beginning
                    if (fragment.length() > 0) {
                      //  joinFragments(fragments, fragment.toString(), new NaiveStringOverlapper());
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


    private static String joinFragments(final List<String> fragments, final StringOverlapper overlapper) {

        System.out.println(fragments.size());
        if (fragments.size() == 1) {
            return fragments.get(0);
        }

        final List<String> joinedFragments = new ArrayList<>();
        final Set<Integer> indexesAlreadyJoined = new HashSet<>();

        //try to overlap every string with every other string O(n^2)
        for (int i = 0; i < fragments.size(); i++) {

            if (indexesAlreadyJoined.contains(i)) {
                continue;
            }

            for (int j = 0; j < fragments.size(); j++) {
                //don't join with self
                if (j == i || indexesAlreadyJoined.contains(j)) {
                    continue;
                }

                final Optional<String> overlapped = overlapper.computeOverlap(fragments.get(i), fragments.get(j));
                if (overlapped.isPresent()) {
                    joinedFragments.add(overlapped.get());
                    indexesAlreadyJoined.add(i);
                    indexesAlreadyJoined.add(j);
                    break; //found unique pair, break out
                }
            }
        }

        //add all the unpaired fragments leftover
        for (int i = 0; i < fragments.size(); i++) {
            if (!indexesAlreadyJoined.contains(i)) {
                joinedFragments.add(fragments.get(i));
            }
        }

        return joinFragments(joinedFragments, overlapper);
    }




    interface StringOverlapper {
        /**
         * Attempts to compute an overlap between String a and String b. An
         * overlap is defined as more than half of String A and String B are equal
         * such that the start of String A overlaps with the end of String B or
         * the end of String B overlaps with the start of String A.
         * @param a first string
         * @param b second string
         * @return Optional that contains the overlap or is empty if there is no computable overlap
         */
        Optional<String> computeOverlap(String a, String b);
    }

    static class NaiveStringOverlapper
        implements StringOverlapper {

        @Override
        public Optional<String> computeOverlap(String a, String b) {

            final String bigger = a.length() >= b.length() ? a : b;
            final String smaller = a.length() >= b.length() ? b : a;

            //check overlap end of smaller string into start of bigger string
            int endSmallerToStartBiggerOverlapIndex = -1;
            for (int i = 0; i < smaller.length(); i++) {
                if (i >= bigger.length() / 2 && i >= smaller.length() / 2) {

                    if (bigger.substring(0, i + 1).equals(smaller.substring(smaller.length() - 1 - i, smaller.length()))) {
                        endSmallerToStartBiggerOverlapIndex = Math.max(endSmallerToStartBiggerOverlapIndex, i);
                    }
                }
            }

            int endBiggerToStartSmallerOverlapIndex = -1;
            //check overlap end of smaller string into start of bigger string
            for (int i = 0; i < smaller.length(); i++) {
                if (i >= bigger.length() / 2 && i >= smaller.length() / 2) {

                    if (bigger.substring(bigger.length() - 1 - i, bigger.length()).equals(smaller.substring(0, i + 1))) {
                        endBiggerToStartSmallerOverlapIndex = Math.max(endBiggerToStartSmallerOverlapIndex, i);
                    }
                }
            }

            if (endSmallerToStartBiggerOverlapIndex == -1 && endBiggerToStartSmallerOverlapIndex == -1) {
                return Optional.empty();
            }
            // theoretically these should never be equal because the fragments are guaranteed to be unique in their order
            if (endSmallerToStartBiggerOverlapIndex > endBiggerToStartSmallerOverlapIndex) {
                return Optional.of(smaller.substring(0, smaller.length() - endSmallerToStartBiggerOverlapIndex - 1).concat(bigger));
            }
            else {
                return Optional.of(bigger.substring(0, bigger.length() - endBiggerToStartSmallerOverlapIndex - 1).concat(smaller));
            }
        }
    }
}
