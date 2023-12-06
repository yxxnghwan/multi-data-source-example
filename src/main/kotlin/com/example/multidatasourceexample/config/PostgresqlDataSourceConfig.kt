package com.example.multidatasourceexample.config

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.orm.hibernate5.SpringBeanContainer
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource


@Configuration
@EntityScan(basePackages = ["com.example.multidatasourceexample.domain.postgresql"])
@EnableJpaRepositories(
    basePackages = ["com.example.multidatasourceexample.domain.postgresql"],
    entityManagerFactoryRef = PostgresqlDataSourceConfig.DATABASE_NAME + "EntityManagerFactory",
    transactionManagerRef = PostgresqlDataSourceConfig.DATABASE_NAME + "TransactionManager"
)
class PostgresqlDataSourceConfig(
    private val jpaProperties: JpaProperties,
    private val hibernateProperties: HibernateProperties,
) {

    companion object {
        const val DATABASE_NAME = "postgresql"
    }

    @Value("\${spring.datasource.postgresql.dialect}")
    private val dialect: String? = null

    @DependsOn(DATABASE_NAME + "WriteDataSource", DATABASE_NAME + "ReadonlyDataSource")
    @Bean(name = [DATABASE_NAME + "DataSource"])
    fun dataSource(
        @Qualifier(DATABASE_NAME + "WriteDataSource") writeDataSource: DataSource,
        @Qualifier(DATABASE_NAME + "ReadonlyDataSource") readonlyDataSource: DataSource
    ): DataSource {
        val routingDataSource = ReplicationRoutingDataSource()
        val dataSourceMap: MutableMap<Any, Any> = HashMap()
        dataSourceMap["write"] = writeDataSource
        dataSourceMap["read"] = readonlyDataSource
        routingDataSource.setTargetDataSources(dataSourceMap)
        routingDataSource.setDefaultTargetDataSource(dataSourceMap["write"]!!)
        routingDataSource.afterPropertiesSet()
        return LazyConnectionDataSourceProxy(routingDataSource)
    }

    private class ReplicationRoutingDataSource : AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): Any {
            return if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) "read" else "write"
        }
    }

    @Bean(name = [DATABASE_NAME + "WriteDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.postgresql.main")
    fun writeDataSource(): DataSource {
        return HikariDataSource()
    }

    @Bean(name = [DATABASE_NAME + "ReadonlyDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.postgresql.readonly")
    fun readonlyDataSource(): DataSource {
        return HikariDataSource()
    }

    @Bean(name = [DATABASE_NAME + "JdbcTemplate"])
    fun jdbcTemplate(@Qualifier(DATABASE_NAME + "WriteDataSource") dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }

    @Bean(name = [DATABASE_NAME + "EntityManagerFactory"])
    fun entityManagerFactory(
        @Qualifier(DATABASE_NAME + "DataSource") dataSource: DataSource,
        builder: EntityManagerFactoryBuilder,
        beanFactory: ConfigurableListableBeanFactory
    ): LocalContainerEntityManagerFactoryBean {
        val emfb = builder.dataSource(dataSource)
            .persistenceUnit(DATABASE_NAME)
            .properties(jpaProperties())
            .packages("com.example.multidatasourceexample.domain.postgresql")
            .build()
        emfb.jpaPropertyMap[AvailableSettings.BEAN_CONTAINER] = SpringBeanContainer(beanFactory)
        return emfb
    }

    @Bean(name = [DATABASE_NAME + "TransactionManager"])
    fun transactionManager(
        @Qualifier(DATABASE_NAME + "EntityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory
        return transactionManager
    }

    private fun jpaProperties(): Map<String, Any?>? {
        val properties = hibernateProperties.determineHibernateProperties(
            jpaProperties.properties, HibernateSettings()
        )
        properties["hibernate.dialect"] = dialect
        properties["javax.persistence.lock.timeout"] = 0
        return properties
    }
}
