package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@SpringBootApplication
// всё равно конфиг пустой
@Configuration
@PropertySources({
        @PropertySource(value = "consumer.properties"),
})
// дай нормальные названия аппликейшнам
public class App
{
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }
}