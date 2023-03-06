package com.munsal.kafkaconfiguration.config;


import com.munsal.kafkaconfiguration.SpringContext;
import com.munsal.kafkaconfiguration.model.Consumer;
import com.munsal.kafkaconfiguration.model.retry.RetryType;
import com.munsal.kafkaconfiguration.util.KafkaConsumerUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

import java.util.*;

@Configuration
@RequiredArgsConstructor
@DependsOn(value = {"kafkaTemplateMap"})
public class KafkaConsumerConfiguration {
    private final KafkaConsumerUtil kafkaConsumerUtil;
    private final KafkaConfiguration kafkaConfiguration;
    private final SpringContext springContext;

    @Bean("kafkaListenerContainerFactoryMap")
    public Map<String, ConcurrentKafkaListenerContainerFactory<String, ?>> kafkaListenerContainerFactoryMap(Map<Integer, KafkaTemplate<String, Object>> kafkaTemplateMap) {
        Map<String,ConcurrentKafkaListenerContainerFactory<String,?>> kafkaListenerContainerFactoryMap = new HashMap<>();
        Optional.ofNullable(kafkaConfiguration.getConsumers()).ifPresent(consumerMap -> consumerMap.forEach((key, value) -> {
            Class<?> consumerClass = kafkaConsumerUtil.getDataClass(value);
            ConsumerFactory<String,?> consumerFactory = kafkaConsumerUtil.createConsumerFactory(value,consumerClass);
            KafkaTemplate<String, Object> kafkaTemplate = findSuitableKafkaTemplate(value, kafkaTemplateMap);
            if(Objects.equals(value.getRetryType(), RetryType.NONBLOCKING_RETRY)) {
                Optional.ofNullable(value.getNonBlockingRetry()).ifPresent(nonBlockingRetry -> {
                    RetryTopicConfigurationBuilder retryTopicConfigurationBuilder = RetryTopicConfigurationBuilder
                            .newInstance();
                    if(Objects.equals(nonBlockingRetry.getIsExponentialRetry(),true)) {
                        retryTopicConfigurationBuilder.exponentialBackoff(
                                Optional.ofNullable(nonBlockingRetry.getBackoffIntervalMillis()).orElse(250),
                                Optional.ofNullable(nonBlockingRetry.getMultiplier()).orElse(2),
                                Optional.ofNullable(nonBlockingRetry.getMaxInterval()).orElse(15000L)
                        );
                    } else {
                        retryTopicConfigurationBuilder.fixedBackOff(Optional.ofNullable(nonBlockingRetry.getBackoffIntervalMillis()).orElse(250));
                    }
                    retryTopicConfigurationBuilder
                            .maxAttempts(Optional.ofNullable(nonBlockingRetry.getMaxAttempts()).orElse(5))
                            .includeTopics(ObjectUtils.isNotEmpty(nonBlockingRetry.getIncludeTopics()) ? nonBlockingRetry.getIncludeTopics() : List.of(value.getTopic()));
                    if(Objects.nonNull(nonBlockingRetry.getRetryOnException()) && Objects.nonNull(nonBlockingRetry.getNotRetryOnException())) {
                        throw new IllegalArgumentException("Please use supply only retry-on filed or only not-retry-on field on the your kafka yml");
                    }
                    if(Objects.nonNull(nonBlockingRetry.getRetryOnException())) {
                        retryTopicConfigurationBuilder.retryOn(nonBlockingRetry.getRetryOnException());
                    }
                    if(Objects.nonNull(nonBlockingRetry.getNotRetryOnException())) {
                        retryTopicConfigurationBuilder.notRetryOn(nonBlockingRetry.getNotRetryOnException());
                    }
                    springContext.addBean(new StringBuilder(key).append("-retry-configuration").toString(), retryTopicConfigurationBuilder.create(kafkaTemplate));
                });
            }
            kafkaListenerContainerFactoryMap.put(key,kafkaConsumerUtil.createListenerFactory(kafkaTemplate,value,consumerFactory));
        }));
        return kafkaListenerContainerFactoryMap;
    }



    private KafkaTemplate<String,Object> findSuitableKafkaTemplate(Consumer consumer, Map<Integer,KafkaTemplate<String,Object>> kafkaProducerTemplateMap) {
        return Optional.ofNullable(consumer.getErrorProducerBeanName()).map(beanName -> kafkaProducerTemplateMap.get(beanName.hashCode()))
                .orElseGet(() -> {
                    Optional<KafkaTemplate<String,Object>> optionalKafkaTemplate = kafkaProducerTemplateMap
                            .values()
                            .stream()
                            .filter(kafkaTemplate ->
                                    ((List<String>)kafkaTemplate.getProducerFactory().getConfigurationProperties().get("bootstrap.servers"))
                                            .stream()
                                            .anyMatch(s -> s.equals(consumer.getProps().get("bootstrap.servers"))))
                            .findFirst();
                    return optionalKafkaTemplate.orElse(null);
                });
    }
}
