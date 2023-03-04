package com.munsal.kafkaconfiguration.config;

import com.munsal.kafkaconfiguration.model.Consumer;
import com.munsal.kafkaconfiguration.model.Producer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "kafka-configuration")
@Component(value = "kafkaConfiguration")
public class KafkaConfiguration {
    private Map<String, Consumer> consumers;
    private Map<String, Producer> producers;
    private Map<String,String> topics;


}
