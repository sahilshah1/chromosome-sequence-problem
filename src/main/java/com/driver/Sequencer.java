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

}
