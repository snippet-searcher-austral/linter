package com.linter

import com.linter.dto.CreateLintingRulesDTO
import com.linter.entity.LintRules
import com.linter.producer.LintingRulesUpdateProducer
import com.linter.service.LinterService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


fun main(args: Array<String>) {
    runApplication<LinterController>(*args)
}

@RestController
@SpringBootApplication
class LinterController(private val linterService: LinterService, private val producer: LintingRulesUpdateProducer) {
    @GetMapping("/")
    fun index(@RequestParam("name") name: String): String = "Hello, $name!. This is a health test!"

    @PostMapping("/linting-rules")
    fun createLintingRules(@RequestBody rulesBody: CreateLintingRulesDTO): LintRules {
        return linterService.createLintingRules(rulesBody.userId, rulesBody.rules)
    }

    @PutMapping("/linting-rules/{id}")
    suspend fun updateLintingRules(@PathVariable("id") id: UUID, @RequestBody rulesBody: CreateLintingRulesDTO): LintRules {
        val updatedRules = linterService.updateLintingRules(id, rulesBody.rules)
        producer.publishEvent(rulesBody.userId)
        return updatedRules;
    }
}