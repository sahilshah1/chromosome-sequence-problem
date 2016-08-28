package com.driver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sahil on 8/28/16.
 */
interface Sequencer {
    /**
     * Sequences a list of fragments and returns the sequenced fragments.
     *
     * @param fragments  list of unordered fragments that overlap
     * @param overlapper algorithm to detect overlap of 2 strings
     * @return ordered list of fragments that overlap
     */
    String sequence(List<String> fragments, final StringOverlapAlgorithm overlapper);

}
