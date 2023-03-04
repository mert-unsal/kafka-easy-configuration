package com.munsal.kafkaconfiguration.annotation;


import org.springframework.context.annotation.DependsOn;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DependsOn({"kafkaConfiguration","kafkaListenerContainerFactoryMap","kafkaProducerTemplateMap"})
public @interface DependsOnKafkaConfiguration {
}
