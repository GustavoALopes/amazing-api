package com.gustavo.dev.observation.interfaces;

import java.util.Map;

public interface IMetricsPublisher {

    void set(final String name, final double value, final Map<String, String> tags);

    void increment(final String name, final double value, final Map<String, String> tags);
}
