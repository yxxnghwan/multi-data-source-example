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
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
@EntityScan(basePackages = ["com.example.multidatasourceexample.domain.mysql"])
@EnableJpaRepositories(
    basePackages = ["com.example.multidatasourceexample.domain.mysql"],
    entityManagerFactoryRef = "mysqlEntityManagerFactory",
    transactionManagerRef = "mysqlTransactionManager"
)
class MysqlDataSourceConfig {

    @Primary
    @Bean("mysqlDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    fun mysqlDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Primary
    @Bean("mysqlDataSource")
    fun dataSource(): DataSource {
        return mysqlDataSourceProperties().initializeDataSourceBuilder().build()
    }

    @Primary
    @Bean("mysqlEntityManagerFactory")
    fun entityManagerFactory(
        @Qualifier("mysqlDataSource") dataSource: DataSource,
        builder: EntityManagerFactoryBuilder,
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .packages("com.example.multidatasourceexample.domain.mysql") // MySQL 엔터티 패키지
            .persistenceUnit("mysql")
            .properties(
                mapOf(Pair("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect"))
            )
            .build()
    }

    @Primary
    @Bean("mysqlTransactionManager")
    fun transactionManager(
        @Qualifier("mysqlEntityManagerFactory") entityManagerFactory: LocalContainerEntityManagerFactoryBean,
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory.`object`!!)
    }
}
