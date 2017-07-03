package com.alice;


import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class AppConfig
{
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource()
    {
        return DataSourceBuilder.create().build();
    }


    @Bean
    public LiquibaseProperties liquibaseProperties()
    {
        return new LiquibaseProperties();
    }


    @Bean
    public SpringLiquibase liquibase()
    {
        SpringLiquibase liquibase = new SpringLiquibase();
        LiquibaseProperties properties = liquibaseProperties();
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDataSource(dataSource());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setLabels(properties.getLabels());
        liquibase.setChangeLogParameters(properties.getParameters());
        return liquibase;
    }
}