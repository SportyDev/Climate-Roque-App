package com.sportydev.appclimaroque

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment

/**
 * Data class para recolectar los datos de este Fragmento.
 * Se ajusta a los campos disponibles en tu XML.
 */
data class EstadoHoraData(
    val estadoTiempoObs: String,      // Cielo (Despejado, etc.)
    val estadoTemperaturaObs: String, // Temperatura (Frio, etc.)
    val vientoDireccion: String,      // N, S, E, W
    val vientoIntensidad: Double?,    // km/h
    val vientoCalma: Boolean,         // Checkbox Calma
    val visibilidad: Double?,         // km
    val fenomenos: String             // Descripción
)

class EstadoHoraFragment : Fragment() {

    // 1. Variables para el Cielo (CheckBox)
    private lateinit var cbDespejado: CheckBox
    private lateinit var cbMedioNublado: CheckBox
    private lateinit var cbNublado: CheckBox

    // 2. Variables para Temperatura (CheckBox)
    private lateinit var cbFrio: CheckBox
    private lateinit var cbFresco: CheckBox
    private lateinit var cbTemplado: CheckBox
    private lateinit var cbCaluroso: CheckBox

    // 3. Variables para Viento
    private lateinit var etDireccionViento: EditText
    private lateinit var etIntensidadViento: EditText
    private lateinit var cbCalma: CheckBox

    // 4. Variables para Visibilidad y Fenómenos
    private lateinit var etVisibilidad: EditText
    private lateinit var etFenomenos: EditText

    private var datosCargados = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout correcto
        return inflater.inflate(R.layout.fragment_estado_hora, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Inicializar Vistas (Binding) ---

        // Cielo
        cbDespejado = view.findViewById(R.id.cbDespejadoHora)
        cbMedioNublado = view.findViewById(R.id.cbMedioNubladoHora)
        cbNublado = view.findViewById(R.id.cbNubladoHora)

        // Temperatura
        cbFrio = view.findViewById(R.id.cbFrioHora)
        cbFresco = view.findViewById(R.id.cbFrescoHora)
        cbTemplado = view.findViewById(R.id.cbTempladoHora)
        cbCaluroso = view.findViewById(R.id.cbCalurosoHora)

        // Viento
        etDireccionViento = view.findViewById(R.id.etDireccionVientoHora)
        etIntensidadViento = view.findViewById(R.id.etIntensidadVientoHora)
        cbCalma = view.findViewById(R.id.cbCalmaHora)

        // Visibilidad y Fenómenos
        etVisibilidad = view.findViewById(R.id.etVisibilidadHora)
        etFenomenos = view.findViewById(R.id.etFenomenosHora)
    }

    // Agrega esto en TemperaturasFragment.kt
    override fun onResume() {
        super.onResume()
        // Buscar si la actividad tiene un registro para editar
        val activity = requireActivity() as? RegistroClimaActivity
        activity?.registroEdicion?.let { datos ->
            cargarDatos(datos) // Llamamos a la función que creamos en el Paso 1
        }
    }

    // Ayuda a convertir texto a Double sin errores
    private fun String.toDoubleOrNullSafe(): Double? {
        return this.trim().toDoubleOrNull()
    }

    /**
     * Recolecta los datos de las vistas para enviarlos a la Activity/Base de Datos.
     */
    fun collectData(): EstadoHoraData {

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

        // C. Lógica para Viento
        // Si "Calma" está marcado, ignoramos la dirección escrita y ponemos "CALMA" (opcional)
        // O simplemente guardamos el booleano. Aquí guardamos lo que está escrito.
        val direccion = if (cbCalma.isChecked) "CALMA" else etDireccionViento.text.toString()

        return EstadoHoraData(
            estadoTiempoObs = cieloString,
            estadoTemperaturaObs = tempString,
            vientoDireccion = direccion,
            vientoIntensidad = etIntensidadViento.text.toString().toDoubleOrNullSafe(),
            vientoCalma = cbCalma.isChecked,
            visibilidad = etVisibilidad.text.toString().toDoubleOrNullSafe(),
            fenomenos = etFenomenos.text.toString()
        )
    }
    // Agrega esto dentro de EstadoHoraFragment

    fun cargarDatos(registro: RegistroClimatico) {
        // 2. VALIDACIÓN PARA NO SOBRESCRIBIR
        if (datosCargados) return

        // Cargar Cielo
        val cielo = registro.estadoTiempoObs ?: ""
        cbDespejado.isChecked = cielo.contains("Despejado")
        cbMedioNublado.isChecked = cielo.contains("Medio Nublado")
        cbNublado.isChecked = cielo.contains("Nublado")

        // Cargar Temperatura
        val temp = registro.estadoTemperaturaObs ?: ""
        cbFrio.isChecked = temp.contains("Frío")
        cbFresco.isChecked = temp.contains("Fresco")
        cbTemplado.isChecked = temp.contains("Templado")
        cbCaluroso.isChecked = temp.contains("Caluroso")

        // Cargar Viento
        if (registro.vientoDireccionObs == "CALMA") {
            cbCalma.isChecked = true
            etDireccionViento.setText("")
        } else {
            cbCalma.isChecked = false
            etDireccionViento.setText(registro.vientoDireccionObs ?: "")
        }

        etVisibilidad.setText(registro.visibilidadPorcentajeObs?.toString() ?: "")
        etFenomenos.setText(registro.fenomenosDiversos1hr ?: "")

        // 3. BLOQUEAR FUTURAS CARGAS
        datosCargados = true
    }
}