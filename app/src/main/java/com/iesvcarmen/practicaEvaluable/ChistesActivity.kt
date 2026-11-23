package com.iesvcarmen.practicaEvaluable

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iesvcarmen.practicaEvaluable.databinding.ActivityChistesBinding
import java.util.Locale
import kotlin.random.Random

// Objeto con una lista de 10 chistes
object RepositorioChistes {
    val listaChistes = listOf(
        "¿Qué le dice un bit a otro? Nos vemos en el bus.",
        "¿Cuál es el colmo de un programador? Tener problemas de memoria y no poder hacer un reset.",
        "Van dos programadores por la calle y uno le dice al otro: ¿Qué hora es? Y responde: Un entero.",
        "¿Por qué los programadores prefieren el modo oscuro? Porque la luz atrae a los bugs.",
        "¿Qué hace una abeja en el gimnasio? Zum-ba.",
        "¿Cómo se dice pañuelo en japonés? Saka-moko.",
        "¿Qué le dice una impresora a otra? ¿Esa hoja es tuya o es impresión mía?",
        "¿Cuál es el café más peligroso del mundo? El ex-preso.",
        "¿Qué le dice un techo a otro? Techo de menos.",
        "¿Por qué las focas del circo miran siempre hacia arriba? Porque es donde están los focos."
    )
}

class ChistesActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityChistesBinding
    private var tts: TextToSpeech? = null
    private var chisteActual: String = ""
    private var ttsListo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChistesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar sintetizador de voz
        tts = TextToSpeech(this, this)

        // Recuperar configuración guardada
        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        val nombreUsuario = prefs.getString("NOMBRE_GUARDADO", "Usuario")
        val mostrarSubtitulos = prefs.getBoolean("MOSTRAR_SUBTITULOS", true)
        val tipoVoz = prefs.getString("TIPO_VOZ", "NORMAL") ?: "NORMAL"

        binding.tvTituloChistes.text = "Chistes para $nombreUsuario"


        if (!mostrarSubtitulos) {
            binding.tvTextoChiste.visibility = View.INVISIBLE
        }

        // 1. Contar un chiste aleatorio de los 10
        binding.btnEscucharChiste.setOnClickListener {
            val indice = Random.nextInt(RepositorioChistes.listaChistes.size)
            chisteActual = RepositorioChistes.listaChistes[indice]
            binding.tvTextoChiste.text = chisteActual
            hablar(chisteActual, tipoVoz)
        }

        // 2. Repetir el último chiste
        binding.btnRepetirChiste.setOnClickListener {
            if (chisteActual.isNotEmpty()) {
                hablar(chisteActual, tipoVoz)
            } else {
                Toast.makeText(this, "Pulsa primero en Contar Chiste Aleatorio", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Parar la voz
        binding.btnPararVoz.setOnClickListener {
            tts?.stop()
        }

        // 4. Volver al menú principal
        binding.btnVolverChistes.setOnClickListener {
            finish()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val resultado = tts?.setLanguage(Locale("es", "ES"))
            if (resultado != TextToSpeech.LANG_MISSING_DATA && resultado != TextToSpeech.LANG_NOT_SUPPORTED) {
                // Intentamos asignar una voz femenina en español si hay varias instaladas
                val vocesFemeninas = tts?.voices?.filter {
                    it.locale.language == "es" && (it.name.contains("female", ignoreCase = true) || it.name.contains("ana", ignoreCase = true) || it.name.contains("eed", ignoreCase = true))
                }
                if (!vocesFemeninas.isNullOrEmpty()) {
                    tts?.voice = vocesFemeninas.first()
                }
                ttsListo = true
            }
        }
    }

    private fun hablar(texto: String, tipoVoz: String) {
        if (!ttsListo) {
            Toast.makeText(this, "Preparando voz, inténtalo de nuevo en un segundo", Toast.LENGTH_SHORT).show()
            return
        }

        // Ajustamos tono y velocidad según el RadioButton elegido en Configuración
        when (tipoVoz) {
            "ARDILLA" -> {
                tts?.setPitch(1.6f)
                tts?.setSpeechRate(1.3f)
            }
            "GRAVE" -> {
                tts?.setPitch(0.6f)
                tts?.setSpeechRate(0.9f)
            }
            else -> {
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)
            }
        }

        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "CHISTE_ID")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}