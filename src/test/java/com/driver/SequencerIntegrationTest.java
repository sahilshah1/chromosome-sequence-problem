package com.driver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
                    { new ChromosomeSequencer.NaiveSequencer(), new StringOverlapAlgorithm.NaiveOverlapAlgorithm() },
                    { new ChromosomeSequencer.NaiveSequencer(), new StringOverlapAlgorithm.KMPAlgorithm() }
                });
    }

    private ChromosomeSequencer.Sequencer sequencer;
    private StringOverlapAlgorithm algorithm;

    public SequencerIntegrationTest(ChromosomeSequencer.Sequencer sequencer, StringOverlapAlgorithm algorithm) {
        this.sequencer = sequencer;
        this.algorithm = algorithm;
    }

    @Test
    public void testCodingChallengeDataSet()
            throws IOException {
        final List<String> fragments = readFragmentsFromResource("coding_challenge_data_set.txt");

        final String sequenced = this.sequencer.sequence(fragments, this.algorithm);

        final String expectedPath = SequencerIntegrationTest.class.getResource("coding_challenge_data_set_expected.txt").getPath();
        String expected = new String(Files.readAllBytes(Paths.get(expectedPath)), Charset.defaultCharset());
        assertEquals(expected, sequenced);

    }

    @Test
    public void testTestDataSet()
            throws IOException {
        final List<String> fragments = readFragmentsFromResource("test_data_set.txt");

        final String sequenced = this.sequencer.sequence(fragments, this.algorithm);

        assertEquals("ATTAGACCTGCCGGAATAC", sequenced);
    }

    private static List<String> readFragmentsFromResource(final String resourceName)
            throws IOException {
        final String path = SequencerIntegrationTest.class.getResource(resourceName).getPath();
        return ChromosomeSequencer.readFragmentsFromDataFile(Paths.get(path));

    }
}
