package com.alice;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass=true)
@EnableCaching
@EnableScheduling
@PropertySources({
        @PropertySource(value = "producer.properties")
})
public class App
{
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }
}