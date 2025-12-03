package com.sportydev.appclimaroque

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

    // VARIABLE NUEVA: Almacena el registro si estamos editando
    var registroEdicion: RegistroClimatico? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_clima)

        dbHelper = AdminBdClima(this)

        // --- MANEJO DE FECHAS ---
        val fechaRecibida = intent.getStringExtra("EXTRA_FECHA")
        val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd-MMMM 'DEL' yyyy", Locale("es", "ES"))

        if (fechaRecibida != null) {
            fechaActualParaBD = fechaRecibida
            try {
                val dateObj = dbFormat.parse(fechaRecibida)
                val fechaDisplay = displayFormat.format(dateObj!!).uppercase()
                initViews()
                tvFechaObservacion.text = "HOJA DIARIA DE OBSERVACIONES... DÍA $fechaDisplay"
            } catch (e: Exception) {
                initViews()
                tvFechaObservacion.text = "HOJA DIARIA... $fechaRecibida"
            }
        } else {
            val hoy = Date()
            fechaActualParaBD = dbFormat.format(hoy)
            val fechaDisplay = displayFormat.format(hoy).uppercase()
            initViews()
            tvFechaObservacion.text = "HOJA DIARIA DE OBSERVACIONES... DÍA $fechaDisplay"
        }

        // --- BUSCAR DATOS EXISTENTES ---
        registroEdicion = dbHelper.getRegistroByFecha(fechaActualParaBD)

        if (registroEdicion != null) {
            submitButton.text = "ACTUALIZAR REGISTRO"
            Toast.makeText(this, "Cargando datos existentes...", Toast.LENGTH_SHORT).show()
        }

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
        // Mantiene los 3 fragments vivos para poder cargarles datos sin que se borren
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

    private fun collectAndSaveFormData() {
        val adapter = viewPager.adapter as? ClimateFormPagerAdapter
        if (adapter == null) return

        // 1. Obtener datos de fragments
        val tempFragment = adapter.getTemperaturasFragment()
        val estadoFragment = adapter.getEstadoHoraFragment()
        val estado24hFragment = adapter.getEstado24HFragment()

        val dataFrag1 = tempFragment?.collectData()
        val dataFrag2 = estadoFragment?.collectData()
        val dataFrag3 = estado24hFragment?.collectData()

        if (dataFrag1 == null || dataFrag2 == null || dataFrag3 == null) {
            Toast.makeText(this, "Faltan datos en algun formulario", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Crear objeto (Usando ID existente si es edición)
        val nuevoRegistro = RegistroClimatico(
            id = registroEdicion?.id ?: 0,
            fecha = fechaActualParaBD,

            // Frag 1
            tempAmbiente = dataFrag1.tempAmbiente,
            tempMax = dataFrag1.tempMax,
            tempMin = dataFrag1.tempMin,
            precipitacionMm = dataFrag1.precipCantidad,
            evapLecturaMicrometro = dataFrag1.evapLectura,
            evapMm = dataFrag1.evapMm,
            evap24hr = dataFrag1.evap24h,
            helada = dataFrag1.helada,

            // Frag 2
            estadoTiempoObs = dataFrag2.estadoTiempoObs,
            estadoTemperaturaObs = dataFrag2.estadoTemperaturaObs,
            vientoDireccionObs = dataFrag2.vientoDireccion,
            visibilidadPorcentajeObs = dataFrag2.visibilidad?.toInt(),
            fenomenosDiversos1hr = dataFrag2.fenomenos,

            // Frag 3
            estadoTiempo24hr = dataFrag3.estadoTiempo24hr,
            estadoTemperatura24hr = dataFrag3.estadoTemperatura24hr,
            vientoDireccion24hr = dataFrag3.vientoDireccion24hr,
            visibilidadPorcentaje24hr = dataFrag3.visibilidadPorcentaje24hr,
            fenomenosDiversos24hr = dataFrag3.fenomenosDiversos24hr
        )

        // 3. Guardar o Actualizar
        try {
            if (registroEdicion == null) {
                // INSERTAR NUEVO
                val id = dbHelper.addRegistro(nuevoRegistro)
                if (id > -1) {
                    Toast.makeText(this, "Guardado Exitoso", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar (Fecha duplicada)", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                // ACTUALIZAR EXISTENTE
                val filas = dbHelper.updateRegistro(nuevoRegistro)
                if (filas > 0) {
                    Toast.makeText(this, "Registro Actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // CLASE ADAPTER INTERNA
    // Modificada para llamar a cargarDatos cuando el fragmento esté listo
    private inner class ClimateFormPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        private var temperaturasFragment: TemperaturasFragment? = null
        private var estadoHoraFragment: EstadoHoraFragment? = null
        private var estado24HFragment: Estado24HFragment? = null

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    if (temperaturasFragment == null) temperaturasFragment = TemperaturasFragment()
                    // Si estamos editando, pasarle los datos
                    registroEdicion?.let { temperaturasFragment?.arguments = createBundle(it, 1) }
                    temperaturasFragment!!
                }

                1 -> {
                    if (estadoHoraFragment == null) estadoHoraFragment = EstadoHoraFragment()
                    registroEdicion?.let { estadoHoraFragment?.arguments = createBundle(it, 2) }
                    estadoHoraFragment!!
                }

                2 -> {
                    if (estado24HFragment == null) estado24HFragment = Estado24HFragment()
                    registroEdicion?.let { estado24HFragment?.arguments = createBundle(it, 3) }
                    estado24HFragment!!
                }

                else -> TemperaturasFragment()
            }
        }

        // Helpers para recuperar instancias
        fun getTemperaturasFragment() = temperaturasFragment
        fun getEstadoHoraFragment() = estadoHoraFragment
        fun getEstado24HFragment() = estado24HFragment

        // Helper para no complicarnos con Bundles serializables,
        // usaremos un truco en el onResume de los fragments mejor.
        private fun createBundle(reg: RegistroClimatico, type: Int): Bundle {
            return Bundle() // Placeholder
        }
    }
}