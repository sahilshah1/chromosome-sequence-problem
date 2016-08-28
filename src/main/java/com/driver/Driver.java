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
public class Driver {

    public static void main(final String[] args)
            throws IOException {
        final Path filePath = Paths.get(args[0]);
        final List<String> fragments = readFragmentsFromDataFile(filePath);
        final String sequenced = new NaiveSequencer().sequence(fragments, new StringOverlapAlgorithm.KMPAlgorithm());
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

}
