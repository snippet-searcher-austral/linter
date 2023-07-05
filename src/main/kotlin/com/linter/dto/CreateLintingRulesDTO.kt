package com.linter.dto

import com.linter.entity.Rule

data class CreateLintingRulesDTO(
    val userId: String,
    val rules: List<Rule>
)