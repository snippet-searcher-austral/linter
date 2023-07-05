package com.linter.producer

import kotlinx.coroutines.reactor.awaitSingle
import org.austral.ingsis.`class`.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class LintingRulesUpdateProducer @Autowired constructor(
    @Value("\${stream.updated-key}") streamKey: String,
    redis: ReactiveRedisTemplate<String, String>
) : RedisStreamProducer(streamKey, redis) {

    suspend fun publishEvent(userId: String) {
        println("Publishing on stream: $streamKey")
        emit(userId).awaitSingle()
    }
}