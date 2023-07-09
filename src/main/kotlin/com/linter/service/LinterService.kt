package com.linter.service

import com.linter.entity.LintRules
import com.linter.entity.Rule
import com.linter.repository.LintRulesRepository
import org.springframework.stereotype.Service
import prinscript.language.linter.LinterImpl
import prinscript.language.rule.CamelCaseRule
import prinscript.language.rule.NoExpressionsOnPrintRule
import prinscript.language.rule.NoExpressionsOnReadRule
import prinscript.language.rule.SnakeCaseRule
import printscript.language.lexer.LexerFactory
import printscript.language.lexer.TokenListIterator
import printscript.language.parser.ASTIterator
import printscript.language.parser.ParserFactory
import java.util.UUID

data class Snippet(val content: String, val userId: String)

@Service
class LinterService(private val lintRulesRepository: LintRulesRepository, private val snippetManagerHTTPService: SnippetManagerHTTPService) {
    fun createLintingRules(userId: String, rules: List<Rule>): LintRules {
        return lintRulesRepository.save(
            LintRules(
                userId = userId,
                rules = rules,
            )
        )
    }

    fun updateLintingRules(id: UUID, rules: List<Rule>): LintRules {
        val lintRules = lintRulesRepository.findById(id).orElseThrow {
            throw Exception("Linting rules not found")
        }
        println(lintRules)
        val newLintRules = lintRules.copy(rules = rules)
        return lintRulesRepository.save(newLintRules)
    }

    suspend fun lint(snippetId: String) {
        val snippet = snippetManagerHTTPService.getSnippet(snippetId)
        val lintRules = lintRulesRepository.findByUserId(snippet.userId)
        if (lintRules == null || lintRules.rules.isEmpty()) {
            println("No lint rules found for user ${snippet.userId}")
            snippetManagerHTTPService.updateSnippet(snippetId, "FAILED")
            return
        }
        val rules = lintRules.rules
        val astIterator = getAstIterator(snippet.content, "1.1")
        val linter = LinterImpl(
            rules.map { it.toRule() },
        )
        val lintResults = mutableListOf<String>()
        try {
            while (astIterator.hasNext()) {
                val result = linter.lint(astIterator.next() ?: return)
                lintResults.addAll(result)
            }
        } catch (e: Exception) {
            println("Error while linting: ${e.message}")
            snippetManagerHTTPService.updateSnippet(snippetId, "NOT_COMPLIANT")
        }
        val complianceValue = if (lintResults.isEmpty()) "COMPLIANT" else "NOT_COMPLIANT"
        try {
            snippetManagerHTTPService.updateSnippet(snippetId, complianceValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAstIterator(code: String, version: String): ASTIterator {
        val inputStream = code.byteInputStream(Charsets.UTF_8)
        val lexer = LexerFactory().createLexer(version, inputStream)
        val tokenListIterator = TokenListIterator(lexer)
        val parser = ParserFactory().createParser(version)
        return ASTIterator(parser, tokenListIterator)
    }

    private fun Rule.toRule(): prinscript.language.rule.Rule {
        return when (this) {
            Rule.CAMEL_CASE-> CamelCaseRule
            Rule.NO_EXPRESSIONS_ON_PRINT -> NoExpressionsOnPrintRule
            Rule.NO_EXPRESSIONS_ON_READ -> NoExpressionsOnReadRule
            Rule.SNAKE_CASE -> SnakeCaseRule
        }
    }
}