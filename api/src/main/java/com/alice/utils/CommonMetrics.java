package com.alice.utils;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;

public abstract class CommonMetrics {

    private static final String METRIC_NAME = "Metrics";

    static {
        JmxReporter.forRegistry(getMetricRegistry()).build().start();
    }

    public static <T> Timer.Context getTimerContext(Class<T> clazz, String name)
    {
        MetricRegistry metricRegistry = getMetricRegistry();
        Timer timer = metricRegistry.timer(MetricRegistry.name(clazz, name));
        return timer.time();
    }

    public static MetricRegistry getMetricRegistry()
    {
        return SharedMetricRegistries.getOrCreate(METRIC_NAME);
    }

}
