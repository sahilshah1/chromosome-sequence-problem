package com.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sahil on 8/23/16.
 */
public class ChromosomeAssembler {

    public static void main(final String[] args)
            throws IOException {
        final Path filePath = Paths.get(args[0]);

        final List<String> fragments = readFragmentsFromDataFile(filePath);


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
                        fragments.add(fragment.toString());
                        fragment = new StringBuilder();
                    }
                }
                else {
                    fragment.append(line);
                }
            }
        }
        return fragments;
    }


//    public void findOverlap(final String a, final String b) {
//        final String bigger = a.length() >= b.length() ? a : b;
//        final String smaller = a.length() >= b.length() ? b : a;
//
//        //search occurrence of last character of smaller string in second half of bigger string
//        final char lastCharacter = smaller.charAt(smaller.length() - 1);
//        final String biggerLastHalf = bigger.substring(bigger.length() / 2 - 1);
//
//        int indexOfChar = bigger.charAt(lastCharacter);
//        while (indexOfChar != -1) {
//
//        }
//
//
//        final String = bigger.indexOf(smaller.charAt(smaller.length() - 1));
//        wh
//
//    }

    public static void findOverlapIndex(final String a, final String b) {
        final String haystack = a.length() >= b.length() ? a : b;
        final String needle = a.length() >= b.length() ? b : a;

        final int[] lps = calculateLps(needle);

        int j = 0;
        int i = 0;
        while (i < haystack.length()) {
            if (needle.charAt(j) == haystack.charAt(i)) {
                j++;
                i++;
            }
            if (j == needle.length()) {
                System.out.println("Found pattern at index " + (i-j));
                j = lps[j-1];
            }
            else if (i < haystack.length() && needle.charAt(j) != haystack.charAt(i)) {
                //by not advancing i, keep looping until we find a prefix that matches
                //or j == 0
                if (j != 0) {
                    j = lps[j - 1];
                } //if j == 0, advance i because the lps will be 0, meaning no matching prefix
                else {
                    i++;
                }
            }
        }
    }


    public static int[] calculateLps(final String pattern) {
        int[] lps = new int[pattern.length()];
        lps[0] = 0;

        int j = 0;
        int i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(j) == pattern.charAt(i)) {
                lps[i] = ++j;
                i++;
            }
            else {
                if (j > 0) {
                    j = lps[j - 1];
                }
                else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }
}
