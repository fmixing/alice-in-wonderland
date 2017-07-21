package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@SpringBootApplication
@PropertySources({
        @PropertySource(value = "consumer.properties"),
})
public class App
{
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }
}