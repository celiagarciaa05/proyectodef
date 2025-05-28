package com.example.proyectodef.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object ChatService {

    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private const val API_KEY = "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"
    private const val ORG_ID = "org-nLl0BjsxMmcGZ3XaxL5em6af"

    private val client = OkHttpClient.Builder()
        .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun enviarPrompt(
        messages: List<Pair<String, String>>,
        userContext: String
    ): String = withContext(Dispatchers.IO) {
        try {
            println("GUAPA: 🔧 Preparando mensaje para OpenAI...")

            val mensajesArray = JSONArray()

            // Mensaje system
            val systemMessage = """
    Eres Budget Buddy, un asistente financiero. Ya tienes el contexto completo del usuario (datos, transacciones, metas, etc.). 
    Siempre responde desde la primera interacción. No pidas más contexto. Si el usuario escribe cualquier mensaje, debes interpretar la intención y actuar con precisión. 
    No uses emojis, ni saludos, ni repitas. Sé útil, claro y directo.
""".trimIndent()


            mensajesArray.put(JSONObject().put("role", "system").put("content", systemMessage))

            // ✅ Incluir siempre el contexto si está disponible
            if (userContext.isNotBlank()) {
                mensajesArray.put(JSONObject().put("role", "user").put("content", "📊 Contexto financiero del usuario:\n$userContext"))
                println("GUAPA: ✅ Contexto añadido al mensaje")
            }

            // Conversación previa
            messages.takeLast(6).forEachIndexed { i, (user, bot) ->
                mensajesArray.put(JSONObject().put("role", "user").put("content", user))
                mensajesArray.put(JSONObject().put("role", "assistant").put("content", bot))
                println("GUAPA: 🗣️ Historial [$i] user: $user\nbot: $bot")
            }

            // Crear cuerpo JSON
            val requestJson = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", mensajesArray)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("OpenAI-Organization", ORG_ID)
                .post(requestBody)
                .build()

            println("GUAPA: 🚀 Enviando solicitud a OpenAI...")
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            println("GUAPA: ✅ Respuesta HTTP ${response.code}\n$responseBody")

            return@withContext JSONObject(responseBody)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()
                ?: "No se encontró contenido válido en la respuesta."

        } catch (e: Exception) {
            println("GUAPA: ❌ Error en enviarPrompt: ${e.message}")
            return@withContext "Ocurrió un error al enviar tu mensaje: ${e.message}"
        }
    }
}
