package com.linter.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

enum class Rule {
    CAMEL_CASE,
    NO_EXPRESSIONS_ON_PRINT,
    NO_EXPRESSIONS_ON_READ,
    SNAKE_CASE,
}

@Entity
@EntityListeners(AuditingEntityListener::class)
data class LintRules(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val userId: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    val rules: List<Rule> = listOf(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    val updatedAt: LocalDateTime? = null
)