package com.example.multidatasourceexample.domain.mysql

import org.springframework.data.jpa.repository.JpaRepository

interface ExampleMYRepository: JpaRepository<ExampleMY, Long> {
}