package com.alice;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@SpringBootApplication
@PropertySources({
        @PropertySource(value = "telegram.properties")
})
public class App
{
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }
}