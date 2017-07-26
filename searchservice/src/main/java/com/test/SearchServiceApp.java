package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@SpringBootApplication
@Configuration
@PropertySources({
        @PropertySource(value = "consumer.properties"),
})
public class SearchServiceApp
{
    public static void main(String[] args)
    {
        SpringApplication.run(SearchServiceApp.class, args);
    }
}