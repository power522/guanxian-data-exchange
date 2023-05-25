package com.yunli.guanxian.data.exchange.guanxiandataexchange.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yunli.frame.common.jackson.*;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * Json日期格式化
 *
 * @author liuwei
 */
@JsonComponent
public class JsonDateFormatConfig {
    
    private final static String DATE_FORMAT = "yyyy-MM-dd";

    private final static String DATE_TIME_FORMAT_FOR_DES = "yyyy-MM-dd['T'][ ]HH:mm:ss['.'SSS][z]";

    @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String dateTimeFormatToStr;

    /**
     * 格式化LocalDate和LocalDateTime
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.simpleDateFormat(dateTimeFormatToStr);
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormatToStr)),
                    new InstantYunLiSerializer(true, DateTimeFormatter.ofPattern(dateTimeFormatToStr)));
            builder.deserializers(new LocalDateYunLiDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    new LocalDateTimeYunLiDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_FOR_DES)),
                    new InstantYunLiDeserializer(DateTimeFormatter.ofPattern(dateTimeFormatToStr)));
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.serializerByType(ScriptObjectMirror.class, new ScriptObjectMirrorToArraySerialize());
        };
    }
}
