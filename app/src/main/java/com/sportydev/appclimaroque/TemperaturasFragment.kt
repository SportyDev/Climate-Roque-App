package com.sportydev.appclimaroque

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment

/**
 * Data class ajustada para la tabla 'RegistrosClimaticos'.
 * Solo contiene los datos que REALMENTE existen en este Fragmento.
 */
data class TemperaturaData(
    val tempAmbiente: Double?,      // -> temp_ambiente
    val tempMax: Double?,           // -> temp_max
    val tempMin: Double?,           // -> temp_min
    val precipCantidad: Double?,    // -> precipitacion_mm
    val evapLectura: Double?,       // -> evap_lectura_micrometro
    val evapMm: Double?,            // -> evap_mm
    val evap24h: Double?,           // -> evap_24hr
    val helada: Boolean             // -> helada
)

class TemperaturasFragment : Fragment() {

    // Referencias a las Vistas
    private lateinit var etTempAmbiente: EditText
    private lateinit var etTempMaxima: EditText
    private lateinit var etTempMinima: EditText

    // Precipitación (Solo cantidad, los tipos están en EstadoTiempoFragment)
    private lateinit var etCantidadLluvia: EditText

    // Evaporación
    private lateinit var etLecturaMicrometro: EditText
    private lateinit var etEvaporacionMm: EditText
    private lateinit var etEvaporacion24hrs: EditText

    // Helada
    private lateinit var cbHelada: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout
        return inflater.inflate(R.layout.fragment_temperaturas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar Temperaturas
        etTempAmbiente = view.findViewById(R.id.etTempAmbiente)
        etTempMaxima = view.findViewById(R.id.etTempMaxima)
        etTempMinima = view.findViewById(R.id.etTempMinima)

        // 2. Inicializar Precipitación (Cantidad)
        etCantidadLluvia = view.findViewById(R.id.etCantidadLluvia)

        // 3. Inicializar Evaporación
        etLecturaMicrometro = view.findViewById(R.id.etLecturaMicrometro)
        etEvaporacionMm = view.findViewById(R.id.etEvaporacionMm) // ¡Ya no está comentado!
        etEvaporacion24hrs = view.findViewById(R.id.etEvaporacion24hrs)

        // 4. Inicializar Helada
        cbHelada = view.findViewById(R.id.cbHelada)
    }

    // Función auxiliar para convertir texto a Double de forma segura
    private fun String.toDoubleOrNullSafe(): Double? {
        return this.trim().toDoubleOrNull()
    }

    /**
     * Recolecta todos los datos para enviar a la Base de Datos.
     */
    fun collectData(): TemperaturaData {

        return TemperaturaData(
            // Temperaturas
            tempAmbiente = etTempAmbiente.text.toString().toDoubleOrNullSafe(),
            tempMax = etTempMaxima.text.toString().toDoubleOrNullSafe(),
            tempMin = etTempMinima.text.toString().toDoubleOrNullSafe(),

            // Precipitación
            precipCantidad = etCantidadLluvia.text.toString().toDoubleOrNullSafe(),

            // Evaporación
            evapLectura = etLecturaMicrometro.text.toString().toDoubleOrNullSafe(),
            evapMm = etEvaporacionMm.text.toString().toDoubleOrNullSafe(),
            evap24h = etEvaporacion24hrs.text.toString().toDoubleOrNullSafe(),

            // Helada
            helada = cbHelada.isChecked
        )
    }
}