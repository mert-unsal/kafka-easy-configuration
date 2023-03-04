package com.munsal.kafkaconfiguration.annotation;

import com.munsal.kafkaconfiguration.SpringContext;
import com.munsal.kafkaconfiguration.config.KafkaConfiguration;
import com.munsal.kafkaconfiguration.config.KafkaConsumerConfiguration;
import com.munsal.kafkaconfiguration.config.KafkaProducerConfiguration;
import com.munsal.kafkaconfiguration.kafka.KafkaSender;
import com.munsal.kafkaconfiguration.util.KafkaConsumerUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({SpringContext.class, KafkaConsumerConfiguration.class, KafkaProducerConfiguration.class, KafkaConfiguration.class, KafkaConsumerUtil.class, KafkaSender.class})
@Configuration
public @interface EnableKafka {
}
