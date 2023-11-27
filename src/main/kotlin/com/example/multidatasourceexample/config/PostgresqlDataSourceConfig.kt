package com.example.multidatasourceexample.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
@EntityScan(basePackages = ["com.example.multidatasourceexample.domain.postgresql"])
@EnableJpaRepositories(
    basePackages = ["com.example.multidatasourceexample.domain.postgresql"],
    entityManagerFactoryRef = "postgresqlEntityManagerFactory",
    transactionManagerRef = "postgresqlTransactionManager"
)
class PostgresqlDataSourceConfig {

    @Bean("postgresqlDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.postgresql")
    fun postgresqlDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean("postgresqlDataSource")
    fun postgresqlDataSource(): DataSource {
        return postgresqlDataSourceProperties().initializeDataSourceBuilder().build()
    }

    @Bean("postgresqlEntityManagerFactory")
    fun postgresqlEntityManagerFactory(
        @Qualifier("postgresqlDataSource") dataSource: DataSource,
        builder: EntityManagerFactoryBuilder,
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .packages("com.example.multidatasourceexample.domain.postgresql") // PostgreSQL 엔터티 패키지
            .persistenceUnit("postgresql")
            .properties(
                mapOf(Pair("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"))
            )
            .build()
    }

    @Bean("postgresqlTransactionManager")
    fun postgresqlTransactionManager(
        @Qualifier("postgresqlEntityManagerFactory") entityManagerFactory: LocalContainerEntityManagerFactoryBean,
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory.`object`!!)
    }
}
