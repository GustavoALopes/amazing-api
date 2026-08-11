package com.gustavo.dev.observation;

import com.gustavo.dev.observation.interfaces.IMetricsPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;


public final class MicrometerMetricPublisher implements IMetricsPublisher {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<MetricKey, AtomicReference<Double>> gauges = new ConcurrentHashMap<>();

    public MicrometerMetricPublisher(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void set(final String name, final double value, final Map<String, String> tags) {
        MetricKey metricKey = new MetricKey(name, tags);
        AtomicReference<Double> gaugeValue = gauges.computeIfAbsent(
            metricKey,
            key -> registerGauge(key, value)
        );

        gaugeValue.set(value);
    }

    @Override
    public void increment(final String name, final double value, final Map<String, String> tags) {
        Counter.builder(name)
            .tags(toMicrometerTags(tags))
            .register(meterRegistry)
            .increment(value);
    }

    private AtomicReference<Double> registerGauge(final MetricKey metricKey, final double initialValue) {
        AtomicReference<Double> value = new AtomicReference<>(initialValue);

        Gauge.builder(metricKey.name(), value, AtomicReference::get)
            .tags(toMicrometerTags(metricKey.tags()))
            .register(meterRegistry);

        return value;
    }

    private Tags toMicrometerTags(final Map<String, String> tags) {
        return Tags.of(
            tags.entrySet().stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .toList()
        );
    }

    private record MetricKey(String name, Map<String, String> tags) {

        private MetricKey {
            tags = Map.copyOf(new TreeMap<>(tags));
        }
    }
}
