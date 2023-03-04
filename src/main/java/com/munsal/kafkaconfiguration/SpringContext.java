package com.munsal.kafkaconfiguration;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public <T> T getBean(String beanName,Class<T> beanClass) {
        return this.applicationContext.getBean(beanName,beanClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
