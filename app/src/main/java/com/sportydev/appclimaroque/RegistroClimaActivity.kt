package com.sportydev.appclimaroque

// Asegúrate de tener estos imports
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroClimaActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var submitButton: Button
    private lateinit var btnClose: ImageButton
    private lateinit var tvFechaObservacion: TextView

    private lateinit var dbHelper: AdminBdClima
    private lateinit var fechaActualParaBD: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_clima)

        dbHelper = AdminBdClima(this)

        // Configuración de fecha
        val fecha = Date()
        fechaActualParaBD = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)
        val formatoDisplay = SimpleDateFormat("dd-MMMM 'DEL' yyyy", Locale("es", "ES"))
        val fechaDisplay = formatoDisplay.format(fecha).uppercase()

        initViews()
        tvFechaObservacion.text = "HOJA DIARIA DE OBSERVACIONES... DÍA $fechaDisplay"

        setupViewPager()
        setupTabLayout()
        setupClickListeners()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        submitButton = findViewById(R.id.submitButton)
        btnClose = findViewById(R.id.btnClose)
        tvFechaObservacion = findViewById(R.id.tvFechaObservacion)
    }

    private fun setupViewPager() {
        val adapter = ClimateFormPagerAdapter(this)
        viewPager.adapter = adapter
        // Importante: Mantiene los fragments en memoria para no perder datos al cambiar de tab
        viewPager.offscreenPageLimit = 3
    }

    private fun setupTabLayout() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Observación 8H"
                1 -> tab.text = "Estado Actual"
                2 -> tab.text = "Estado 24H"
            }
        }.attach()
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener { finish() }
        submitButton.setOnClickListener { collectAndSaveFormData() }
    }

    // =========================================================================
    //  AQUÍ ESTÁ LA CORRECCIÓN PRINCIPAL
    // =========================================================================
    private fun collectAndSaveFormData() {
        val adapter = viewPager.adapter as? ClimateFormPagerAdapter
        if (adapter == null) return

        // --- 1. Datos Fragment 1 (Temperaturas) ---
        val tempFragment = adapter.getTemperaturasFragment()
        val dataFrag1 = tempFragment?.collectData()

        if (dataFrag1 == null) {
            Toast.makeText(this, "Faltan datos en Temperaturas", Toast.LENGTH_SHORT).show()
            return
        }

        // --- 2. Datos Fragment 2 (Estado Hora) ---
        val estadoFragment = adapter.getEstadoHoraFragment()
        val dataFrag2 = estadoFragment?.collectData()

        if (dataFrag2 == null) {
            Toast.makeText(this, "Faltan datos en Estado Actual", Toast.LENGTH_SHORT).show()
            return
        }

        // --- 3. Datos Fragment 3 (Estado 24h) ---
        val estado24hFragment = adapter.getEstado24HFragment()
        val dataFrag3 = estado24hFragment?.collectData()

        if (dataFrag3 == null) {
            Toast.makeText(this, "Faltan datos en Estado 24H", Toast.LENGTH_SHORT).show()
            return
        }

        // --- 4. Crear objeto RegistroClimatico ---
        // Aquí hacemos el "mapeo" correcto de nombres
        val registroCompleto = RegistroClimatico(
            fecha = fechaActualParaBD,

            // DATOS FRAG 1 (Nombres coinciden con TemperaturaData)
            tempAmbiente = dataFrag1.tempAmbiente,
            tempMax = dataFrag1.tempMax,
            tempMin = dataFrag1.tempMin,
            precipitacionMm = dataFrag1.precipCantidad, // dataFrag1.precipCantidad -> BD.precipitacionMm
            evapLecturaMicrometro = dataFrag1.evapLectura,
            evapMm = dataFrag1.evapMm,
            evap24hr = dataFrag1.evap24h,
            helada = dataFrag1.helada,

            // DATOS FRAG 2 (CORREGIDOS los errores de referencia)
            estadoTiempoObs = dataFrag2.estadoTiempoObs,
            estadoTemperaturaObs = dataFrag2.estadoTemperaturaObs,

            // CORRECCIÓN: En el fragment se llama 'vientoDireccion', en la BD 'vientoDireccionObs'
            vientoDireccionObs = dataFrag2.vientoDireccion,

            // CORRECCIÓN: Convertir Double a Int?
            visibilidadPorcentajeObs = dataFrag2.visibilidad?.toInt(),

            // CORRECCIÓN: En el fragment se llama 'fenomenos', en la BD 'fenomenosDiversos1hr'
            fenomenosDiversos1hr = dataFrag2.fenomenos,

            // DATOS FRAG 3
            estadoTiempo24hr = dataFrag3.estadoTiempo24hr,
            estadoTemperatura24hr = dataFrag3.estadoTemperatura24hr,
            vientoDireccion24hr = dataFrag3.vientoDireccion24hr,
            visibilidadPorcentaje24hr = dataFrag3.visibilidadPorcentaje24hr,

            // CORRECCIÓN: Eliminamos dataFrag1.precipTipo porque ya no existe ahí.
            // Usamos directamente lo del Frag 3.
            fenomenosDiversos24hr = dataFrag3.fenomenosDiversos24hr
        )

        // --- 5. Guardar en BD ---
        try {
            val id = dbHelper.addRegistro(registroCompleto)
            if (id > -1) {
                Toast.makeText(this, "¡Guardado Exitoso!", Toast.LENGTH_LONG).show()
                //finish()
            } else {
                Toast.makeText(
                    this,
                    "Error: Ya existe un registro para hoy ($fechaActualParaBD)",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error guardando", e)
            Toast.makeText(this, "Error técnico: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Clase Adapter (Sin cambios mayores, solo para asegurar que funcione)
    private class ClimateFormPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        // Cacheamos los fragmentos para poder pedirles los datos luego
        private var temperaturasFragment: TemperaturasFragment? = null
        private var estadoHoraFragment: EstadoHoraFragment? = null
        private var estado24HFragment: Estado24HFragment? = null

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    if (temperaturasFragment == null) temperaturasFragment = TemperaturasFragment()
                    temperaturasFragment!!
                }

                1 -> {
                    if (estadoHoraFragment == null) estadoHoraFragment = EstadoHoraFragment()
                    estadoHoraFragment!!
                }

                2 -> {
                    if (estado24HFragment == null) estado24HFragment = Estado24HFragment()
                    estado24HFragment!!
                }

                else -> TemperaturasFragment()
            }
        }

        // Getters seguros
        fun getTemperaturasFragment() = temperaturasFragment
        fun getEstadoHoraFragment() = estadoHoraFragment
        fun getEstado24HFragment() = estado24HFragment
    }
}