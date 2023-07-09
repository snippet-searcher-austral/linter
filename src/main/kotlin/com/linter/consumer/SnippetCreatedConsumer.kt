package com.linter.consumer

import org.austral.ingsis.`class`.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration
import com.linter.service.LinterService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Component
class SnippetCreatedConsumer @Autowired constructor(
    redis: ReactiveRedisTemplate<String, String>,
    @Value("\${stream.created-key}") streamKey: String,
    @Value("\${groups.product}") groupId: String,
    private val linterService: LinterService
) : RedisStreamConsumer<String>(streamKey, groupId, redis) {

    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
        return StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(Duration.ofMillis(10000)) // Set poll rate
            .targetType(String::class.java) // Set type to de-serialize record
            .build();
    }

    override fun onMessage(record: ObjectRecord<String, String>) {
        // What we want to do with the stream
        println("Id: ${record.id}, Value: ${record.value}, Stream: ${record.stream}, Group: ${groupId}")
        GlobalScope.launch {
            linterService.lint(record.value)
        }
    }
}