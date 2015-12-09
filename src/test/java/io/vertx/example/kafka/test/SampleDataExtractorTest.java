package io.vertx.example.kafka.test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.BasicSampleExtractor;
import io.vertx.example.util.kafka.SampleData;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static io.vertx.example.util.kafka.launcher.KafkaTestUtils.*;
import static io.vertx.example.util.kafka.SampleData.READINGS;
import static junit.framework.Assert.*;

@RunWith(VertxUnitRunner.class)
public class SampleDataExtractorTest {


    public static final String NOW = GregorianCalendar.getInstance().getTime().toLocaleString();
    public static final String SAMPLE_READINGS = "{1,13,192,7,8,99,1014,4}";

    @Test
    public void testDataReadingsExtractor() throws Exception {
        BasicSampleExtractor extractor = new BasicSampleExtractor();
        double[] readings = extractor.extractReadings(SAMPLE_READINGS);
        assertEquals(readings.length,8);
    }

    @Test
    public void testDataConsumption() throws Exception {
        List<SampleData> ginzburg = create("ginzburg", 10, 10);
        assertEquals(ginzburg.size(), 10);
    }

    @Test
    public void testDataJsonConsumption() throws Exception {
        JsonArray ginzburg = createJson("ginzburg", 1, 10);
        assertEquals(ginzburg.size(), 1);

        BasicSampleExtractor extractor = new BasicSampleExtractor();
        JsonObject jsonObject = ginzburg.getJsonObject(0);

        Optional<SampleData> sampleData = extractor.extractSample(jsonObject);
        assertTrue(sampleData.isPresent());
        assertEquals(sampleData.get().getPublishId(), "ginzburg");
        assertFalse(sampleData.get().getTime().isEmpty());

        double[] readings = extractor.extractReadings(jsonObject.getString(READINGS));
        assertEquals(readings.length, 10);


        DescriptiveStatistics stats = new DescriptiveStatistics(readings);
        double median = stats.getPercentile(50);
        assertEquals(sampleData.get().getMedian() , median);
    }

    public void testDataExtractor() throws Exception {
        BasicSampleExtractor extractor = new BasicSampleExtractor();
        Optional<SampleData> sampleData = extractor.extractSample(new JsonObject(buildMessage()));
        assertTrue(sampleData.isPresent());
        assertEquals(sampleData.get().getPublishId(), "norbert");
        assertEquals(sampleData.get().getTime(), NOW);
        assertEquals(sampleData.get().getMedian(), 10.5);
    }

    public Map<String, Object> buildMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", "norbert");
        map.put("time", NOW);
        map.put("readings", SAMPLE_READINGS);
        return map;
    }

}
