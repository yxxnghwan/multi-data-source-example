package com.example.multidatasourceexample.domain.postgresql

import org.springframework.data.jpa.repository.JpaRepository

interface ExamplePGRepository: JpaRepository<ExamplePG, Long> {
}