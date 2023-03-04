package com.munsal.kafkaconfiguration.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.List;

public interface JsonUtil {
    default String getFirstLevelAttribute(ObjectMapper objectMapper, Object in, String fieldName) {
        JsonNode node = objectMapper.valueToTree(in);
        return node.get(fieldName).textValue();
    }

    default <T> String asJson(ObjectMapper objectMapper, T in) {
        try {
            return objectMapper.writeValueAsString(in);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default <T> String asJsonWithWriter(ObjectWriter objectWriter, T in) {
        try {
            return objectWriter.writeValueAsString(in);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default <T> T fromJson(ObjectMapper objectMapper, Class<T> clazz, String in) {
        try {
            return objectMapper.readValue(in, TypeFactory.defaultInstance().constructType(clazz));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }


    default <T> T fromJson(ObjectMapper objectMapper, Class<T> clazz, Object in) {
        try {
            return objectMapper.convertValue(in, TypeFactory.defaultInstance().constructType(clazz));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    default <T> T fromJsonAsList(ObjectMapper objectMapper, Class<T> clazz, String in) {
        try {
            return objectMapper.readValue(in, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default <T> T deserialize(ObjectMapper objectMapper, String in, Class<T> clazz, boolean isList) {
        if (isList) {
            return fromJsonAsList(objectMapper, clazz, in);
        } else {
            return fromJson(objectMapper, clazz, in);
        }
    }

}
