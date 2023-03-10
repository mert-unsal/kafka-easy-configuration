package com.munsal.kafkaconfiguration.model;

import com.munsal.kafkaconfiguration.model.retry.BlockingRetry;
import com.munsal.kafkaconfiguration.model.retry.NonBlockingRetry;
import com.munsal.kafkaconfiguration.model.retry.RetryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Consumer {
    private ContainerProperties.AckMode ackMode;
    private Boolean asyncAcks;
    private Long ackTime;
    private String topic;
    private String errorProducerBeanName;
    private String errorTopic;
    private Integer ackCount;
    private Integer concurrency;
    private Long shutdownTimeout;
    private Long idleEventInterval;
    private Long idlePartitionEventInterval;
    private Double idleBeforeDataMultiplier;
    private Boolean logContainerConfig;
    private Boolean missingTopicsFatal;
    private Long idleBetweenPolls;
    private Boolean micrometerEnabled;
    private Boolean deliveryAttemptHeader;
    private Boolean checkDeserExWhenKeyNull;
    private Boolean checkDeserExWhenValueNull;
    private Integer monitorInterval;
    private Float noPollThreshold;
    private Integer commitRetries;
    private Boolean subBatchPerPartition;
    private Boolean stopContainerWhenFenced;
    private Boolean stopImmediate;
    private Integer phase;
    private String clientId;
    private Integer syncCommitTimeoutSecond;
    private Boolean syncCommit;
    private Boolean fixTxOffsets;
    private Long pollTimeout;
    private Boolean missingTopicAlertEnable;
    private String failoverHandlerBeanName;
    private String dataClass;
    private Boolean autoStartup;
    private Boolean batchListener;
    private Boolean ackDiscarded;
    private RetryType retryType;
    private NonBlockingRetry nonBlockingRetry;
    private BlockingRetry blockingRetry;
    private Map<String, Object> props;
}


