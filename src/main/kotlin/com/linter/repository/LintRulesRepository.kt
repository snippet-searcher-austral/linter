package com.linter.repository

import com.linter.entity.LintRules

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LintRulesRepository : JpaRepository<LintRules, UUID> {
    fun findByUserId(userId: String): LintRules?
}