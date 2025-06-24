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

    private const val API_URL = "https://api.together.xyz/v1/chat/completions"
    private const val API_KEY = "tgp_v1_UXDxmYWanILe3eLEQ-HYejl9jgF_75ZHjea-fjwZrWs"
    private const val MODEL = "meta-llama/Meta-Llama-3-8B-Instruct-Lite"

    private val SYSTEM_PROMPT = """
    Eres Buddy, un asistente financiero. Ya tienes el contexto completo del usuario (datos, transacciones, metas, etc.). 
    Debes contestar únicamente a lo que pida o diga el usuario, no a más nada, siempre y cuando el tema no se salga de las finanzas. 
    No uses carácteres especiales, ni repitas los mensajes. Sé útil, y amigable, habla de tú a tú. Respone en solo un parráfo no muy largo.
    """.trimIndent()

    private val client = OkHttpClient.Builder().callTimeout(30, java.util.concurrent.TimeUnit.SECONDS).connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS).readTimeout(30, java.util.concurrent.TimeUnit.SECONDS).writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build()

    suspend fun enviarPrompt(messages: List<Pair<String, String>>, userContext: String = ""): String = withContext(Dispatchers.IO) {
        try {
            val mensajesArray = JSONArray()
            mensajesArray.put(JSONObject().put("role", "system").put("content", SYSTEM_PROMPT))
            if (userContext.isNotBlank()) {
                mensajesArray.put(JSONObject().put("role", "user").put("content", "Contexto adicional:\n$userContext"))
            }

            messages.takeLast(6).forEachIndexed { i, (user, bot) ->
                mensajesArray.put(JSONObject().put("role", "user").put("content", user))
                mensajesArray.put(JSONObject().put("role", "assistant").put("content", bot))
            }

            val requestJson = JSONObject().apply {
                put("model", MODEL)
                put("messages", mensajesArray)
                put("temperature", 0.7)
                put("top_p", 0.7)
                put("top_k", 50)
                put("repetition_penalty", 1.0)
                put("stream", false)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(API_URL).addHeader("Authorization", "Bearer $API_KEY").addHeader("Content-Type", "application/json").post(requestBody).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            val jsonResponse = JSONObject(responseBody)
            if (jsonResponse.has("error")) {
                val errorMsg = jsonResponse.getJSONObject("error").optString("message", "Error desconocido")
                return@withContext "TogetherAI dio error: $errorMsg"
            }

            return@withContext jsonResponse.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")?.trim() ?: "No se encontró contenido válido en la respuesta."

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Ocurrió un error al enviar tu mensaje: ${e.message}"
        }
    }
}
