package com.iesvcarmen.practicaEvaluable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        // Referencias a las vistas
        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val etTelefono = findViewById<EditText>(R.id.et_telefono)
        val etUrl = findViewById<EditText>(R.id.et_url)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val spTiempoDados = findViewById<Spinner>(R.id.sp_tiempo_dados)
        val rgTipoVoz = findViewById<RadioGroup>(R.id.rg_tipo_voz)
        val rbVozNormal = findViewById<RadioButton>(R.id.rb_voz_normal)
        val rbVozGrave = findViewById<RadioButton>(R.id.rb_voz_grave)
        val cbSubtitulos = findViewById<CheckBox>(R.id.cb_mostrar_subtitulos)
        val dpFechaNacimiento = findViewById<DatePicker>(R.id.dp_fecha_nacimiento)
        val pbCargando = findViewById<ProgressBar>(R.id.pb_cargando)
        val tvEstadoCarga = findViewById<TextView>(R.id.tv_estado_carga)
        val btnGuardar = findViewById<Button>(R.id.btn_guardar_entrar)

        // 1. Configurar el Spinner con ArrayAdapter
        val opcionesTiempo = arrayOf("1 segundo", "2 segundos", "3 segundos")
        val adapter = ArrayAdapter(this, R.layout.item_negro, opcionesTiempo)
        adapter.setDropDownViewResource(R.layout.item_negro)
        spTiempoDados.adapter = adapter

        // Limitar fecha máxima del DatePicker a hoy
        dpFechaNacimiento.maxDate = System.currentTimeMillis()

        // 2. Precargar datos si el usuario ya había guardado configuración previamente
        val sharedPreferences = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        etNombre.setText(sharedPreferences.getString("NOMBRE_GUARDADO", ""))
        etTelefono.setText(sharedPreferences.getString("TELEFONO_GUARDADO", ""))
        etUrl.setText(sharedPreferences.getString("URL_GUARDADA", ""))
        etEmail.setText(sharedPreferences.getString("EMAIL_GUARDADO", ""))

        val tiempoPrevio = sharedPreferences.getLong("TIEMPO_TIRADAS_MS", 1000L)
        val posicionSpinner = when (tiempoPrevio) {
            2000L -> 1
            3000L -> 2
            else -> 0
        }
        spTiempoDados.setSelection(posicionSpinner)

        val vozPrevia = sharedPreferences.getString("TIPO_VOZ", "NORMAL")
        if (vozPrevia == "GRAVE") {
            rbVozGrave.isChecked = true
        } else {
            rbVozNormal.isChecked = true
        }

        cbSubtitulos.isChecked = sharedPreferences.getBoolean("MOSTRAR_SUBTITULOS", true)

        val fechaGuardada = sharedPreferences.getString("FECHA_NACIMIENTO", "") ?: ""
        if (fechaGuardada.contains("/")) {
            val partes = fechaGuardada.split("/")
            if (partes.size == 3) {
                dpFechaNacimiento.updateDate(
                    partes[2].toInt(),
                    partes[1].toInt() - 1,
                    partes[0].toInt()
                )
            }
        }

        // 3. Acción al pulsar Guardar
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val url = etUrl.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (nombre.isEmpty() || telefono.isEmpty() || url.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (telefono.length != 9 || !telefono.all { it.isDigit() }) {
                Toast.makeText(this, "El teléfono debe tener 9 números válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener valor del Spinner en milisegundos para CajaFuerteActivity
            val tiempoMilisegundos = when (spTiempoDados.selectedItemPosition) {
                0 -> 1000L
                1 -> 2000L
                2 -> 3000L
                else -> 1000L
            }

            // Obtener valor del RadioGroup para ChistesActivity
            val tipoVozSeleccionada = if (rgTipoVoz.checkedRadioButtonId == R.id.rb_voz_grave) {
                "GRAVE"
            } else {
                "NORMAL"
            }

            // Obtener estado del CheckBox para ChistesActivity
            val mostrarSubtitulos = cbSubtitulos.isChecked

            // Obtener fecha del DatePicker y calcular edad
            val dia = dpFechaNacimiento.dayOfMonth
            val mes = dpFechaNacimiento.month + 1
            val anio = dpFechaNacimiento.year
            val fechaFormateada = String.format("%02d/%02d/%04d", dia, mes, anio)
            val esMayorEdad = calcularEdad(anio, dpFechaNacimiento.month, dia) >= 18

            // Guardar todas las preferencias
            val editor = sharedPreferences.edit()
            editor.putString("NOMBRE_GUARDADO", nombre)
            editor.putString("TELEFONO_GUARDADO", telefono)
            editor.putString("URL_GUARDADA", url)
            editor.putString("EMAIL_GUARDADO", email)
            editor.putLong("TIEMPO_TIRADAS_MS", tiempoMilisegundos)
            editor.putString("TIPO_VOZ", tipoVozSeleccionada)
            editor.putBoolean("MOSTRAR_SUBTITULOS", mostrarSubtitulos)
            editor.putString("FECHA_NACIMIENTO", fechaFormateada)
            editor.putBoolean("ES_MAYOR_EDAD", esMayorEdad)
            editor.apply()

            // 4. ProgressBar en segundo plano antes de navegar a MainActivity
            btnGuardar.isEnabled = false
            pbCargando.visibility = View.VISIBLE
            tvEstadoCarga.visibility = View.VISIBLE

            lifecycleScope.launch {
                for (progreso in 0..100 step 20) {
                    pbCargando.progress = progreso
                    tvEstadoCarga.text = "Aplicando preferencias... $progreso%"
                    delay(200L)
                }

                val intent = Intent(this@ConfiguracionActivity, MainActivity::class.java)
                intent.putExtra("FECHA_NACIMIENTO", fechaFormateada)
                intent.putExtra("ES_MAYOR_EDAD", esMayorEdad)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun calcularEdad(anio: Int, mes: Int, dia: Int): Int {
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - anio
        if (hoy.get(Calendar.MONTH) < mes ||
            (hoy.get(Calendar.MONTH) == mes && hoy.get(Calendar.DAY_OF_MONTH) < dia)
        ) {
            edad--
        }
        return edad
    }
}