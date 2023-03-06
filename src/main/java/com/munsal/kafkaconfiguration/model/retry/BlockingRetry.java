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
public class BlockingRetry {
    private Boolean isExponentialRetry;
    private Integer retryCount;
    private Integer backoffIntervalMillis;
    private Long maxInterval;
    private Integer maxAttempts;
    private Double multiplier;
}
