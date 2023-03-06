package com.munsal.kafkaconfiguration.model.retry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NonBlockingRetry {
    private Boolean isExponentialRetry;
    private Integer backoffIntervalMillis;
    private Long maxInterval;
    private Integer maxAttempts;
    private Integer multiplier;
    private List<String> includeTopics;
    private Class<? extends Throwable> retryOnException;
    private Class<? extends Throwable> notRetryOnException;
}
