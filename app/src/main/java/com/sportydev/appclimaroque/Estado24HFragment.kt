package com.sportydev.appclimaroque

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment

/**
 * Data class para el Fragment "Estado 24 Horas".
 * Coincide con las columnas "Estado 24 horas anteriores" de la BD.
 */
data class Estado24hData(
    val estadoTiempo24hr: String?,        // Cielo (Despejado, Nublado...)
    val estadoTemperatura24hr: String?,   // Temperatura (Frio, Fresco...)
    val vientoDireccion24hr: String?,     // N, S, E, W
    val visibilidadPorcentaje24hr: Int?,  // Entero
    val fenomenosDiversos24hr: String?    // Descripción
)

class Estado24HFragment : Fragment() {

    // 1. CheckBoxes Cielo
    private lateinit var cbDespejado: CheckBox
    private lateinit var cbMedioNublado: CheckBox
    private lateinit var cbNublado: CheckBox

    // 2. CheckBoxes Temperatura
    private lateinit var cbFrio: CheckBox
    private lateinit var cbFresco: CheckBox
    private lateinit var cbTemplado: CheckBox
    private lateinit var cbCaluroso: CheckBox

    // 3. Viento
    private lateinit var etDireccionViento: EditText
    private lateinit var etIntensidadViento: EditText

    // 4. Visibilidad y Fenómenos
    private lateinit var etVisibilidad: EditText
    private lateinit var etFenomenos: EditText

    private var datosCargados = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_estado_24h, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Cielo
        cbDespejado = view.findViewById(R.id.cbDespejado24h)
        cbMedioNublado = view.findViewById(R.id.cbMedioNublado24h)
        cbNublado = view.findViewById(R.id.cbNublado24h)

        // Temperatura
        cbFrio = view.findViewById(R.id.cbFrio24h)
        cbFresco = view.findViewById(R.id.cbFresco24h)
        cbTemplado = view.findViewById(R.id.cbTemplado24h)
        cbCaluroso = view.findViewById(R.id.cbCaluroso24h)

        // Viento
        etDireccionViento = view.findViewById(R.id.etDireccionViento24h)
        etIntensidadViento = view.findViewById(R.id.etIntensidadViento24h)

        // Visibilidad y Fenómenos
        etVisibilidad = view.findViewById(R.id.etVisibilidad24h)
        etFenomenos = view.findViewById(R.id.etFenomenos24h)
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as? RegistroClimaActivity
        activity?.registroEdicion?.let { datos ->
            cargarDatos(datos)
        }
    }

    private fun String.toIntOrNullSafe(): Int? {
        return this.trim().toDoubleOrNull()?.toInt()
    }


    fun collectData(): Estado24hData {

        // A. Lógica para el Cielo (Juntar opciones seleccionadas)
        val cieloList = mutableListOf<String>()
        if (cbDespejado.isChecked) cieloList.add("Despejado")
        if (cbMedioNublado.isChecked) cieloList.add("Medio Nublado")
        if (cbNublado.isChecked) cieloList.add("Nublado")
        val cieloString = cieloList.joinToString(", ")

        // B. Lógica para la Temperatura
        val tempList = mutableListOf<String>()
        if (cbFrio.isChecked) tempList.add("Frío")
        if (cbFresco.isChecked) tempList.add("Fresco")
        if (cbTemplado.isChecked) tempList.add("Templado")
        if (cbCaluroso.isChecked) tempList.add("Caluroso")
        val tempString = tempList.joinToString(", ")

        return Estado24hData(
            estadoTiempo24hr = cieloString,
            estadoTemperatura24hr = tempString,
            vientoDireccion24hr = etDireccionViento.text.toString(),

            visibilidadPorcentaje24hr = etVisibilidad.text.toString().toIntOrNullSafe(),
            fenomenosDiversos24hr = etFenomenos.text.toString()
        )
    }

    fun cargarDatos(registro: RegistroClimatico) {
        // 2. VALIDACIÓN
        if (datosCargados) return

        // Cargar Cielo
        val cielo = registro.estadoTiempo24hr ?: ""
        cbDespejado.isChecked = cielo.contains("Despejado")
        cbMedioNublado.isChecked = cielo.contains("Medio Nublado")
        cbNublado.isChecked = cielo.contains("Nublado")

        // Cargar Temperatura
        val temp = registro.estadoTemperatura24hr ?: ""
        cbFrio.isChecked = temp.contains("Frío")
        cbFresco.isChecked = temp.contains("Fresco")
        cbTemplado.isChecked = temp.contains("Templado")
        cbCaluroso.isChecked = temp.contains("Caluroso")

        etDireccionViento.setText(registro.vientoDireccion24hr ?: "")
        etVisibilidad.setText(registro.visibilidadPorcentaje24hr?.toString() ?: "")
        etFenomenos.setText(registro.fenomenosDiversos24hr ?: "")

        // 3. BLOQUEAR
        datosCargados = true
    }
}