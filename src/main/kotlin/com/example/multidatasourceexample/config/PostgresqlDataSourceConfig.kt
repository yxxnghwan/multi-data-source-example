package com.example.multidatasourceexample.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
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

    @Bean("postgresqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.postgresql")
    fun dataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean("postgresqlEntityManagerFactory")
    fun entityManagerFactory(
        @Qualifier("postgresqlDataSource") dataSource: DataSource,
    ): EntityManagerFactory {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("com.example.multidatasourceexample.domain.postgresql")
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        return em.`object`!!
    }

    @Bean("postgresqlTransactionManager")
    fun transactionManager(
        @Qualifier("postgresqlEntityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
