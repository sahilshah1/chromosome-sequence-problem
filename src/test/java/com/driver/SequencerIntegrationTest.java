package com.driver;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by sahil on 8/27/16.
 */
@RunWith(Parameterized.class)
public class SequencerIntegrationTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                    { new ChromosomeAssembler.NaiveSequencer(), new ChromosomeAssembler.NaiveOverlapAlgorithm() }
                });
    }

    private ChromosomeAssembler.Sequencer sequencer;
    private ChromosomeAssembler.StringOverlapAlgorithm overlapper;

    public SequencerIntegrationTest(ChromosomeAssembler.Sequencer sequencer, ChromosomeAssembler.StringOverlapAlgorithm overlapper) {
        this.sequencer = sequencer;
        this.overlapper = overlapper;
    }

    @Test
    public void testCodingChallengeDataSet()
            throws IOException {
        final List<String> fragments = readFragmentsFromResource("coding_challenge_data_set.txt");

        final String sequenced = this.sequencer.sequence(fragments, this.overlapper);

        assertEquals("ATTAGACCTGCCGGAATAC", sequenced);

    }

    @Test
    public void testTestDataSet()
            throws IOException {
        final List<String> fragments = readFragmentsFromResource("test_data_set.txt");

        final String sequenced = this.sequencer.sequence(fragments, this.overlapper);

        assertEquals("ATTAGACCTGCCGGAATAC", sequenced);
    }

    private static List<String> readFragmentsFromResource(final String resourceName)
            throws IOException {
        final String path = SequencerIntegrationTest.class.getResource(resourceName).getPath();
        return ChromosomeAssembler.readFragmentsFromDataFile(Paths.get(path));

    }
}
