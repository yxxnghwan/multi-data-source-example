package com.example.multidatasourceexample.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
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
    @Bean("mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    fun dataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Primary
    @Bean("mysqlEntityManagerFactory")
    fun entityManagerFactory(
        @Qualifier("mysqlDataSource") dataSource: DataSource,
    ): EntityManagerFactory {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("com.example.multidatasourceexample.domain.mysql")
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        return em.`object`!!
    }

    @Primary
    @Bean("mysqlTransactionManager")
    fun transactionManager(
        @Qualifier("mysqlEntityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
