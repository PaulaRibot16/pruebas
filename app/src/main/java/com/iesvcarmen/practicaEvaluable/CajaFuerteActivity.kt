package com.iesvcarmen.practicaEvaluable

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.iesvcarmen.practicaEvaluable.databinding.ActivityCajaFuerteBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class CajaFuerteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCajaFuerteBinding
    private var sum: Int = 0
    private var numeroUsuario: Int = 0
    private var animacionMano: ObjectAnimator? = null
    private var tiempoEntreTiradas: Long = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCajaFuerteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperamos el tiempo elegido en el Spinner de Configuración (1000L, 2000L o 3000L)
        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        tiempoEntreTiradas = prefs.getLong("TIEMPO_TIRADAS_MS", 1000L)

        initEvent()
    }

    private fun initEvent() {
        binding.txtResultado.visibility = View.INVISIBLE

        binding.imageButtonMano.setOnClickListener {
            val textoNumero = binding.editTextNumero.text.toString()
            val numeroObjetivo = textoNumero.toIntOrNull()
            if (numeroObjetivo == null || numeroObjetivo !in 3..18) {
                Toast.makeText(this, "Introduce un número entre 3 y 18", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            numeroUsuario = numeroObjetivo

            binding.imageButtonMano.isEnabled = false
            binding.editTextNumero.isEnabled = false
            binding.txtResultado.visibility = View.VISIBLE

            game(numeroObjetivo)
        }

        binding.btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun game(numeroObjetivo: Int) {
        sheduleRun(numeroObjetivo)
    }

    private fun sheduleRun(numeroObjetivo: Int) {
        iniciarAnimacionMano()

        lifecycleScope.launch {
            do {
                throwEngranajeInTime()
                viewResult()

                // Usamos el tiempo seleccionado en el Spinner
                if (sum != numeroUsuario) delay(tiempoEntreTiradas)
            } while (sum != numeroUsuario)

            detenerAnimacionMano()

            binding.imageButtonMano.isEnabled = true
            binding.editTextNumero.isEnabled = true

            mostrarDialogoGanador()
        }
    }

    private fun throwEngranajeInTime() {
        val numero = Array(3) { Random.nextInt(1, 7) }

        val imagView: Array<ImageView> = arrayOf(
            binding.imageViewRueda1,
            binding.imageViewRueda2,
            binding.imageViewRueda3
        )

        sum = numero.sum()

        for (i in 0..2) {
            selectView(imagView[i], numero[i])
        }
    }

    private fun selectView(imgV: ImageView, v: Int) {
        when (v) {
            1 -> imgV.setImageResource(R.drawable.rueda1)
            2 -> imgV.setImageResource(R.drawable.rueda2)
            3 -> imgV.setImageResource(R.drawable.rueda3)
            4 -> imgV.setImageResource(R.drawable.rueda4)
            5 -> imgV.setImageResource(R.drawable.rueda5)
            6 -> imgV.setImageResource(R.drawable.rueda6)
        }
    }

    private fun viewResult() {
        binding.txtResultado.text = sum.toString()
    }

    private fun mostrarDialogoGanador() {
        AlertDialog.Builder(this)
            .setTitle("¡Caja Fuerte Abierta!")
            .setMessage("La combinación ha coincidido con el número $numeroUsuario.")
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun iniciarAnimacionMano() {
        animacionMano = ObjectAnimator.ofFloat(binding.imageButtonMano, "translationX", -40f, 40f).apply {
            duration = 250L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun detenerAnimacionMano() {
        animacionMano?.cancel()
        binding.imageButtonMano.translationX = 0f
    }
}