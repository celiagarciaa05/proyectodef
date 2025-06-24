package com.example.proyectodef.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectodef.data.repository.ChatService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    var chatHistory by mutableStateOf(
        listOf("" to "Hola, soy Budget Buddy. ¿En qué puedo ayudarte hoy?")
    )
        private set

    var cargando by mutableStateOf(false)
        private set

    fun enviarPregunta(pregunta: String, contexto: String = "", onRespuesta: (String) -> Unit) {
        chatHistory = chatHistory + (pregunta to "...")
        viewModelScope.launch {
            cargando = true

            val fullHistory = chatHistory.drop(1)
            val respuesta = ChatService.enviarPrompt(fullHistory, contexto).ifBlank {
                "Lo siento, no pude responder. ¿Podrías reformular tu pregunta?"
            }

            chatHistory = chatHistory.dropLast(1) + (pregunta to respuesta)
            cargando = false
            onRespuesta(respuesta)
        }
    }

    fun enviarPreguntaConContextoTotal(pregunta: String, userId: String, onRespuesta: (String) -> Unit) {
        cargando = true
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("usuarios").document(userId)

        userDocRef.get().addOnSuccessListener { userDoc ->
            val userData = userDoc.data ?: emptyMap()
            val metasRef = userDocRef.collection("metas")
            val transRef = userDocRef.collection("transacciones")
            val catRef = userDocRef.collection("categorias")

            metasRef.get().addOnSuccessListener { metasSnap ->
                transRef.get().addOnSuccessListener { transSnap ->
                    catRef.get().addOnSuccessListener { catSnap ->
                        val contexto = construirContextoFinanciero(
                            userData,
                            metasSnap.documents,
                            transSnap.documents,
                            catSnap.documents
                        )
                        enviarPregunta(pregunta, contexto, onRespuesta)
                    }
                }
            }
        }.addOnFailureListener {
            enviarPregunta(pregunta, "", onRespuesta)
        }
    }

    private fun construirContextoFinanciero(
        userData: Map<String, Any>,
        metas: List<com.google.firebase.firestore.DocumentSnapshot>,
        transacciones: List<com.google.firebase.firestore.DocumentSnapshot>,
        categorias: List<com.google.firebase.firestore.DocumentSnapshot>
    ): String {
        val nombre = userData["nombreCompleto"] as? String ?: "desconocido"
        val dineroTotal = userData["dineroTotal"]?.toString() ?: "0"
        val sb = StringBuilder()

        sb.append("Nombre: $nombre\n")
        sb.append("Dinero total: $dineroTotal€\n\n")

        sb.append("Metas:\n")
        if (metas.isEmpty()) sb.append("- No hay metas.\n")
        else metas.forEach {
            val tipo = it["tipo"] ?: "N/A"
            val cantidad = it["cantidad"] ?: "N/A"
            val categoria = it["categoria"] ?: "N/A"
            val estado = it["estado"] ?: "N/A"
            sb.append("- $tipo $cantidad€ para $categoria ($estado)\n")
        }

        sb.append("\nTransacciones:\n")
        if (transacciones.isEmpty()) sb.append("- Ninguna registrada.\n")
        else transacciones.forEach {
            val tipo = it["tipo"] ?: "N/A"
            val cantidad = (it["cantidad"] as? Number)?.toDouble()?.let { "%.2f".format(it) } ?: "N/A"
            val categoria = it["categoria"] ?: "N/A"
            val descripcion = it["descripcion"] ?: ""
            val fechaMs = (it["fecha"] as? Number)?.toLong() ?: 0L
            val fechaStr = try {
                java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(fechaMs))
            } catch (e: Exception) { "Fecha inválida" }
            sb.append("- $cantidad€ en $categoria el $fechaStr ($tipo: \"$descripcion\")\n")
        }

        sb.append("\nCategorías:\n")
        if (categorias.isEmpty()) sb.append("- Sin categorías.\n")
        else categorias.forEach {
            val nombre = it["nombre"] ?: "Sin nombre"
            sb.append("- $nombre\n")
        }

        return sb.toString().trim()
    }
}