package com.sportydev.appclimaroque

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // --- Vistas del Calendario ---
    private lateinit var txtMonth: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var btnOpenDatePicker: ImageButton
    private var btnToday: TextView? = null
    private lateinit var recyclerDays: RecyclerView
    private lateinit var daysAdapter: DaysAdapter

    // --- Vistas de Navegación Inferior ---
    private lateinit var btnSync: ImageButton
    private lateinit var btnManual: ImageButton
    private lateinit var btnMothy: ImageButton // Botón de reportes mensuales

    // Layouts contenedores
    private lateinit var layoutEmptyState: LinearLayout // Estado "Sin Registro"
    private lateinit var layoutDataState: androidx.constraintlayout.widget.ConstraintLayout

    private lateinit var btnRegistrar: Button
    private lateinit var btnEditarRegistro: Button
    private lateinit var txtTituloEstado: TextView
    private lateinit var txtDescripcionEstado: TextView

    // Elementos del Estado Con Datos (Resumen)
    private lateinit var tvResumenAmbiente: TextView
    private lateinit var tvResumenMaxMin: TextView
    private lateinit var tvResumenEstado: TextView
    private lateinit var tvResumenLluvia: TextView
    private lateinit var tvResumenViento: TextView
    private lateinit var tvResumenEvap: TextView // Nuevo
    private lateinit var btnCharts: ImageButton

    // --- Lógica y Base de Datos ---
    private lateinit var dbHelper: AdminBdClima

    private var currentCalendar = Calendar.getInstance()  // Mes que se está viendo
    private var selectedCalendar = Calendar.getInstance() // Día seleccionado específicamente

    // Formatos
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    private val dayNameFormat = SimpleDateFormat("EEE", Locale("es", "ES"))
    private val dbDateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inicializar Base de Datos
        dbHelper = AdminBdClima(this)

        // 2. Inicializar Vistas y Configuración
        initViews()
        setupRecyclerView()
        setupClickListeners()

        // 3. Cargar Calendario inicial
        updateCalendar()
    }


    override fun onResume() {
        super.onResume()
        updateUIForSelectedDate()
    }

    private fun initViews() {
        // Calendario
        txtMonth = findViewById(R.id.txtMonth)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        btnOpenDatePicker = findViewById(R.id.btnOpenDatePicker)
        recyclerDays = findViewById(R.id.recyclerDays)
        btnCharts =
            findViewById<ImageButton>(R.id.btnCharts) // Asegúrate de tener la referencia en initViews si prefieres

        // Toolbar inferior
        btnMothy = findViewById(R.id.btnReportMonth)
        btnSync = findViewById(R.id.btnSync)
        btnManual = findViewById(R.id.btnManual)

        // Botón Today (puede ser nulo dependiendo del layout)
        val todayButton = findViewById<TextView?>(R.id.btnToday)
        if (todayButton != null) {
            btnToday = todayButton
        }

        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        layoutDataState = findViewById(R.id.layoutDataState)

        // Estado Vacío
        btnRegistrar = findViewById(R.id.btnOpenBottomSheet)
        txtTituloEstado = findViewById(R.id.txtTitulo)
        txtDescripcionEstado = findViewById(R.id.txtdescripcion)

        // Estado Con Datos
        tvResumenAmbiente = findViewById(R.id.tvResumenAmbiente)
        tvResumenMaxMin = findViewById(R.id.tvResumenMaxMin)
        tvResumenEstado = findViewById(R.id.tvResumenEstado)
        tvResumenLluvia = findViewById(R.id.tvResumenLluvia)
        btnEditarRegistro = findViewById(R.id.btnEditarRegistro)
        tvResumenViento = findViewById(R.id.tvResumenViento)
        tvResumenEvap = findViewById(R.id.tvResumenEvap)
    }

    private fun setupRecyclerView() {
        daysAdapter = DaysAdapter(emptyList()) { day, position ->
            if (day.isSelectable) {
                // Actualizamos el calendario seleccionado
                selectedCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
                selectedCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
                selectedCalendar.set(Calendar.DAY_OF_MONTH, day.dayNumber)

                updateUIForSelectedDate()
            }
        }

        recyclerDays.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = daysAdapter
        }
    }

    private fun setupClickListeners() {

        btnSync.setOnClickListener {

            Toast.makeText(this, "Sincronizando con la Base de Datos...", Toast.LENGTH_SHORT).show()

            it.postDelayed({
                Toast.makeText(this, "¡Sincronización completada con éxito!", Toast.LENGTH_LONG)
                    .show()
            }, 2000)
        }

        btnManual.setOnClickListener {
            Toast.makeText(this, "Abriendo Manual de Usuario...", Toast.LENGTH_SHORT).show()
        }
        // Navegación Meses
        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        btnCharts.setOnClickListener {
            val intent = Intent(this, ActivityCharts::class.java)
            startActivity(intent)
        }

        // Date Picker
        btnOpenDatePicker.setOnClickListener { showDatePicker() }

        // Botón Hoy
        btnToday?.setOnClickListener { goToToday() }

        // Botón Reportes
        btnMothy.setOnClickListener {
            val intent = Intent(this, ActivityMonthlyReports::class.java)
            startActivity(intent)
        }

        // --- BOTONES PRINCIPALES ---

        // 1. Botón "REGISTRAR" (Visible solo si NO hay datos)
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, RegistroClimaActivity::class.java)
            startActivity(intent)
        }

        // 2. Botón "VER DETALLES" (Visible solo si SÍ hay datos)
        btnEditarRegistro.setOnClickListener {
            // Creamos el intent hacia el formulario
            val intent = Intent(this, RegistroClimaActivity::class.java)

            // Obtenemos la fecha seleccionada actual
            val dateString = dbDateFormat.format(selectedCalendar.time)

            // Se la enviamos a la actividad
            intent.putExtra("EXTRA_FECHA", dateString)

            // Iniciamos la actividad
            startActivity(intent)
        }
    }


    private fun updateUIForSelectedDate() {
        val dateString = dbDateFormat.format(selectedCalendar.time)

        // 1. Consultar BD
        val registro = dbHelper.getRegistroByFecha(dateString)

        if (registro != null) {
            layoutEmptyState.visibility = View.GONE
            layoutDataState.visibility = View.VISIBLE

            // 1. Temperaturas
            tvResumenAmbiente.text = "${registro.tempAmbiente ?: "--"}°"
            tvResumenMaxMin.text =
                "H:${registro.tempMax?.toInt() ?: "--"}° L:${registro.tempMin?.toInt() ?: "--"}°"

            // 2. Estado (Cielo)
            tvResumenEstado.text = registro.estadoTiempoObs ?: "Sin datos"

            // 3. Lluvia
            tvResumenLluvia.text = "${registro.precipitacionMm ?: 0} mm"

            // 4. Viento (NUEVO)
            val dirViento = registro.vientoDireccionObs ?: "-"
            tvResumenViento.text = "$dirViento" // Puedes agregar velocidad si quieres: "$dirViento"

            // 5. Evaporación (NUEVO)
            tvResumenEvap.text = "${registro.evapMm ?: 0} mm"

        } else {
        }
    }

    private fun updateCalendar() {
        // 1. Actualizar título del mes
        txtMonth.text = monthFormat.format(currentCalendar.time).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        // 2. Generar y actualizar días
        val days = generateDaysForMonth()
        daysAdapter.updateDays(days)

        // 3. Scroll inteligente
        scrollToTodayIfCurrentMonth()

        // 4. Estilo botón Hoy
        updateTodayButtonState()

        // 5. IMPORTANTE: Al cambiar de mes, verificar si el día seleccionado tiene datos
        updateUIForSelectedDate()
    }

    private fun generateDaysForMonth(): List<Day> {
        val days = mutableListOf<Day>()
        val today = Calendar.getInstance()
        val tempCalendar = currentCalendar.clone() as Calendar

        // Ir al primer día del mes
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (dayNumber in 1..daysInMonth) {
            tempCalendar.set(Calendar.DAY_OF_MONTH, dayNumber)

            val dayName = dayNameFormat.format(tempCalendar.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            val isToday = tempCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    tempCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    tempCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            val isSelectable = tempCalendar.timeInMillis <= today.timeInMillis

            // Verificar si este día coincide con el seleccionado globalmente
            val isSelected =
                tempCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                        tempCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                        tempCalendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH) &&
                        isSelectable

            days.add(
                Day(
                    dayNumber = dayNumber,
                    dayName = dayName,
                    isToday = isToday,
                    isSelected = isSelected,
                    isSelectable = isSelectable
                )
            )
        }
        return days
    }

    private fun scrollToTodayIfCurrentMonth() {
        val today = Calendar.getInstance()

        // Si estamos viendo el mes actual
        if (currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
        ) {

            val todayPosition = today.get(Calendar.DAY_OF_MONTH) - 1
            recyclerDays.post {
                (recyclerDays.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    todayPosition,
                    100
                )
            }
        }
        // Si estamos viendo el mes de la fecha seleccionada
        else if (currentCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
            currentCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH)
        ) {

            val selectedPosition = selectedCalendar.get(Calendar.DAY_OF_MONTH) - 1
            recyclerDays.post {
                (recyclerDays.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    selectedPosition,
                    100
                )
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val today = Calendar.getInstance()

                if (selectedDate.timeInMillis <= today.timeInMillis) {
                    // Actualizar fecha seleccionada
                    selectedCalendar.set(year, month, dayOfMonth)

                    // Si la fecha elegida es de otro mes, mover el calendario visual
                    if (currentCalendar.get(Calendar.YEAR) != year ||
                        currentCalendar.get(Calendar.MONTH) != month
                    ) {
                        currentCalendar.set(Calendar.YEAR, year)
                        currentCalendar.set(Calendar.MONTH, month)
                    }
                    updateCalendar()
                    // Actualizar tarjeta central
                    updateUIForSelectedDate()
                }
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun goToToday() {
        val today = Calendar.getInstance()

        // Mover calendario visual a hoy
        currentCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR))
        currentCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH))

        // Mover selección a hoy
        selectedCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR))
        selectedCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH))
        selectedCalendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))

        updateCalendar()
        updateUIForSelectedDate()
    }

    private fun updateTodayButtonState() {
        btnToday?.let { button ->
            val today = Calendar.getInstance()
            val isCurrentMonth = currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
            val isTodaySelected = selectedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    selectedCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            if (isCurrentMonth && isTodaySelected) {
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.today_blue))
            } else {
                button.setTextColor(ContextCompat.getColor(this, R.color.today_blue))
                button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            }
        }
    }
}