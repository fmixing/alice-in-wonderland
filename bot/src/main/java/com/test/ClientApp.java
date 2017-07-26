package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@Configuration
@PropertySources({
        @PropertySource(value = "telegram.properties")
})
public class ClientApp {

    public static void main(String[] args) {

        // вытащи их в отдельные main-ы
//        new TestConcurrent(1).run();

//        new com.test.TestCorrectness();

        SpringApplication.run(ClientApp.class, args);
    }

}
