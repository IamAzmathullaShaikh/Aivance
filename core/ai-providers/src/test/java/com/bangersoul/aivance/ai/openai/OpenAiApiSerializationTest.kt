package com.bangersoul.aivance.ai.openai

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiApiSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `chat completion request serializes snake_case fields`() {
        val request = ChatCompletionRequest(
            model = "gpt-4o",
            messages = listOf(
                OpenAiMessage(role = "system", content = "You are helpful"),
                OpenAiMessage(role = "user", content = "Hello")
            ),
            temperature = 0.7f,
            maxTokens = 512,
            stream = true
        )

        val encoded = json.encodeToString(ChatCompletionRequest.serializer(), request)

        assertTrue(encoded.contains("\"max_tokens\":512"))
        assertTrue(encoded.contains("\"stream\":true"))
        assertTrue(encoded.contains("\"temperature\":0.7"))
        assertTrue(encoded.contains("\"role\":\"system\""))
    }

    @Test
    fun `chat completion response decodes usage and finish reason`() {
        val payload = """
            {
              "id": "chatcmpl-1",
              "choices": [
                {
                  "message": { "role": "assistant", "content": "Hi there" },
                  "finish_reason": "stop"
                }
              ],
              "usage": { "prompt_tokens": 10, "completion_tokens": 5, "total_tokens": 15 }
            }
        """.trimIndent()

        val response = json.decodeFromString(ChatCompletionResponse.serializer(), payload)

        assertEquals("Hi there", response.choices.first().message.content)
        assertEquals("stop", response.choices.first().finishReason)
        assertEquals(15, response.usage?.totalTokens)
    }

    @Test
    fun `streaming chunk decodes delta content`() {
        val chunkJson = """
            {
              "id": "chatcmpl-2",
              "choices": [
                { "delta": { "role": "assistant", "content": "Hel" }, "finish_reason": null }
              ]
            }
        """.trimIndent()

        val chunk = json.decodeFromString(ChatCompletionChunk.serializer(), chunkJson)
        assertEquals("Hel", chunk.choices.first().delta.content)
    }

    @Test
    fun `streaming chunk with role-only delta has null content`() {
        val chunkJson = """
            {
              "id": "chatcmpl-3",
              "choices": [ { "delta": { "role": "assistant" }, "finish_reason": null } ]
            }
        """.trimIndent()

        val chunk = json.decodeFromString(ChatCompletionChunk.serializer(), chunkJson)
        assertEquals(null, chunk.choices.first().delta.content)
    }

    @Test
    fun `usage serializes round-trip`() {
        val usage = ChatCompletionResponse.Usage(
            promptTokens = 4,
            completionTokens = 2,
            totalTokens = 6
        )

        val encoded = json.encodeToString(ChatCompletionResponse.Usage.serializer(), usage)
        val decoded = json.decodeFromString(ChatCompletionResponse.Usage.serializer(), encoded)

        assertEquals(4, decoded.promptTokens)
        assertEquals(6, decoded.totalTokens)
    }

    @Test
    fun `ignoreUnknownKeys allows extra server fields`() {
        val payload = """
            {
              "id": "chatcmpl-4",
              "object": "chat.completion.chunk",
              "created": 1234567890,
              "model": "gpt-4o",
              "choices": [
                { "index": 0, "delta": { "content": "x" }, "finish_reason": null }
              ]
            }
        """.trimIndent()

        val chunk = json.decodeFromString(ChatCompletionChunk.serializer(), payload)
        assertEquals("x", chunk.choices.first().delta.content)
    }

    @Test
    fun `stream flag false by default`() {
        val request = ChatCompletionRequest(
            model = "m",
            messages = listOf(OpenAiMessage("user", "hi"))
        )
        assertFalse(request.stream)
    }
}
