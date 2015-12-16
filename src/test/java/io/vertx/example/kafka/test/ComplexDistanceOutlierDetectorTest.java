package io.vertx.example.kafka.test;

import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.*;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class ComplexDistanceOutlierDetectorTest {


    public static final String NORBERT = "norbert";
    private SamplePersister persister;
    private SampleExtractor extractor;

    @Before
    public void setUp() throws Exception {
        extractor = new BasicSampleExtractor();
        persister = new InMemSamplePersister();
        for (int i = 0; i < 10  ; i++) {
            persister.persist(extractor.extractSample(new JsonObject(buildMessage1(i))).get());
            persister.persist(extractor.extractSample(new JsonObject(buildMessage2(i))).get());
        }
    }

    @Test
    public void testOutlierDistanceNormalized() throws Exception {
        ComplexDistanceOutlierDetector detector = new ComplexDistanceOutlierDetector(persister);
        SampleData sampleData = extractor.extractSample(new JsonObject(buildMessage2(0))).get();
        List<SampleData> outlier = detector.getOutlier(NORBERT, 1, Optional.<Double>empty());
        assertTrue(sampleData.getReadings().length > 0);
        assertTrue(!outlier.isEmpty());

    }

    @Test
    public void testOutlierDistance() throws Exception {
        ComplexDistanceOutlierDetector detector = new ComplexDistanceOutlierDetector(persister);
        SampleData sampleData = extractor.extractSample(new JsonObject(buildMessage1(0))).get();
        List<SampleData> outlier = detector.getOutlier(NORBERT, 1, Optional.<Double>empty());
        assertTrue(sampleData.getReadings().length > 0);
        assertTrue(!outlier.isEmpty());
        assertEquals(outlier.size(), 1);

    }


    public Map<String, Object> buildMessage1(int index) {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", NORBERT);
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", getReadings1(index));
        return map;
    }

    public Map<String, Object> buildMessage2(int index) {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", NORBERT);
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", getReadings2(index));
        return map;
    }

    public String getReadings1(int index) {
        return "{" + (1 + index) + "," + (13 + index) + "," + (192 + index) + "," + (7 + index) + "," + (8 + index) + "," + (99 + index) + "," + (1014 + index) + "," + (4 + index) + "," + "}";
    }

    public String getReadings2(int index) {
        return "{" + (1 + index) + "," + (2 + index) + "," + (3 + index) + "," + (4 + index) + "," + (5 + index) + "," + (6 + index) + "," + (8 + index) + "," + (8 + index) + "," + (9 + index) + "," + "}";
    }

}
