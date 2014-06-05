package org.calrissian.accumulorecipes.featurestore.support.config;


import com.google.common.base.Function;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.lang.StringUtils;
import org.calrissian.accumulorecipes.commons.support.MetricTimeUnit;
import org.calrissian.accumulorecipes.featurestore.model.Metric;
import org.calrissian.accumulorecipes.featurestore.model.MetricFeature;
import org.calrissian.accumulorecipes.featurestore.support.StatsCombiner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang.StringUtils.splitPreserveAllTokens;
import static org.calrissian.accumulorecipes.featurestore.impl.AccumuloFeatureStore.combine;

public class MetricFeatureConfig implements AccumuloFeatureConfig<MetricFeature> {

    @Override
    public Class<MetricFeature> transforms() {
        return MetricFeature.class;
    }

    @Override
    public Value buildValue(MetricFeature feature) {
        return vectorToValue.apply(feature.getVector());
    }

    @Override
    public MetricFeature buildFeatureFromValue(long timestamp, String group, String type, String name, String visibility, Value value) {
        Metric metricFeatureVector = valueToVector.apply(value);
        return new MetricFeature(timestamp, group, type, name, visibility, metricFeatureVector);
    }

    @Override
    public String featureName() {
        return "metric";
    }

    @Override
    public List<IteratorSetting> buildIterators() {
        List<IteratorSetting.Column> columns = new ArrayList<IteratorSetting.Column>();
        for (MetricTimeUnit timeUnit : MetricTimeUnit.values())
            columns.add(new IteratorSetting.Column(combine(featureName(), timeUnit.toString())));

        IteratorSetting setting = new IteratorSetting(14, "stats", StatsCombiner.class);
        StatsCombiner.setColumns(setting, columns);

        return Collections.singletonList(setting);
    }

    public static final Function<Value, Metric> valueToVector = new Function<Value, Metric>() {
        @Override
        public Metric apply(Value value) {
            String[] vals = splitPreserveAllTokens(new String(value.get()), ",");
            return new Metric(
                parseLong(vals[0]),
                parseLong(vals[1]),
                parseLong(vals[2]),
                parseLong(vals[3]),
                new BigInteger(vals[4])
            );
        }
    };

    public static final Function<Metric, Value> vectorToValue = new Function<Metric, Value>() {
        @Override
        public Value apply(Metric metricFeatureVector) {
            return new Value(StringUtils.join(Arrays.asList(
                metricFeatureVector.getMin(),
                metricFeatureVector.getMax(),
                metricFeatureVector.getSum(),
                metricFeatureVector.getCount(),
                metricFeatureVector.getSumSquare().toString()),
                ",").getBytes());
        }
    };
}
