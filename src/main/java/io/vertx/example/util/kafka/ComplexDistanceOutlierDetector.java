package io.vertx.example.util.kafka;

import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class ComplexDistanceOutlierDetector implements OutlierDetector {
    public static final String COMPLEX = "complex";
    private SamplePersister persister;

    public ComplexDistanceOutlierDetector(SamplePersister persister) {
        this.persister = persister;
    }

    /**
     * return all samples (within the sample size window) that differ more > 2 time sdtDev
     */
    @Override
    public List<SampleData> getOutlier(String publisherId, int sampleSize, Optional<Double> outlierFactor) {
        List<SampleData> fetch = persister.fetch(publisherId, sampleSize);
        List<Double> all = new ArrayList<>();
        fetch.forEach(sampleData1 -> { all.addAll(Doubles.asList(sampleData1.getReadings()));});
        DescriptiveStatistics stats = new DescriptiveStatistics(all.stream().mapToDouble(Double::valueOf).toArray());
        double deviation = stats.getPercentile(outlierFactor.orElse(99d));
        System.out.println("deviation : "+deviation);
        List<SampleData> outliers = fetch.stream().filter(sampleData -> isOutlier(deviation, Optional.<Double>empty(), sampleData, outlierFactor.orElse(2d))).collect(toList());
        return outliers;
    }

    @Override
    public String getName() {
        return COMPLEX;
    }

    public boolean isOutlier(double deviation, Optional<Double> mean, SampleData sampleData, double outlierFactor) {
        double[] readings = sampleData.getReadings();
        for (double reading : readings) {
            if(reading>deviation)
                return true;
        }
        return false;
    }


}
