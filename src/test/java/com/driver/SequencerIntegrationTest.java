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

    @Parameterized.Parameters(name = "{0} with {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                    { new NaiveSequencer(), new StringOverlapAlgorithm.NaiveOverlapAlgorithm() },
                    { new NaiveSequencer(), new StringOverlapAlgorithm.KMPAlgorithm() },
                    { new ParallelSequencer(2), new StringOverlapAlgorithm.NaiveOverlapAlgorithm() },
                    { new ParallelSequencer(2), new StringOverlapAlgorithm.KMPAlgorithm() },
                    { new ParallelSequencer(4), new StringOverlapAlgorithm.KMPAlgorithm() }
                });
    }

    private Sequencer sequencer;
    private StringOverlapAlgorithm algorithm;

    public SequencerIntegrationTest(Sequencer sequencer, StringOverlapAlgorithm algorithm) {
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
        return Driver.readFragmentsFromDataFile(Paths.get(path));

    }
}
