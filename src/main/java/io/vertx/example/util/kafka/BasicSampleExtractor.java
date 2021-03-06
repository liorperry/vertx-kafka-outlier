package io.vertx.example.util.kafka;

import io.vertx.core.json.JsonObject;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.vertx.example.util.kafka.SampleData.*;

public class BasicSampleExtractor implements SampleExtractor{

    public static JsonObject toJson(SampleData data) {
        Map<String,Object> map = new HashMap<>();
        map.put(PUBLISHER,data.getPublishId());
        map.put(TIME,data.getTime());
        map.put(MEDIAN,data.getMedian());
        return new JsonObject(map);
    }

    @Override
    public Optional<SampleData> extractSample(JsonObject result) {
            if (!result.isEmpty()) {
                double[] values = new double[0];
                String publisher = result.getString(PUBLISHER);
                String time = result.getString(TIME);
                String samples = result.getString(READINGS);
                double median = result.getDouble(MEDIAN,0.0);
                if (samples != null && samples.length()>0) {
                    values = extractReadings(samples);
                }
                if (publisher != null && time != null && values.length > 0) {

                    DescriptiveStatistics stats = new DescriptiveStatistics(values);
                    median = stats.getPercentile(50);
                    //persist sample data
                }
                return Optional.of(new SampleData(publisher,time,median,values ));
            }
        return Optional.empty();
    }

    public double[] extractReadings(String samples) {
        return Arrays.asList(samples.replace("{", "").replace("}", "").split("\\,")).stream().mapToDouble(Double::valueOf).toArray();
    }
}
