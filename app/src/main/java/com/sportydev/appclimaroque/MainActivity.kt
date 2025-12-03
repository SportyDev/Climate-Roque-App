package com.sportydev.appclimaroque

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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
    private lateinit var btnMothy: ImageButton // Botón de reportes mensuales

    // --- Vistas del Contenido Principal (CardView) ---
    // Layouts contenedores
    private lateinit var layoutEmptyState: LinearLayout // Estado "Sin Registro"
    private lateinit var layoutDataState: LinearLayout  // Estado "Con Datos"

    // Elementos del Estado Vacío
    private lateinit var btnRegistrar: Button
    private lateinit var txtTituloEstado: TextView
    private lateinit var txtDescripcionEstado: TextView

    // Elementos del Estado Con Datos (Resumen)
    private lateinit var tvResumenAmbiente: TextView
    private lateinit var tvResumenMaxMin: TextView
    private lateinit var tvResumenEstado: TextView
    private lateinit var tvResumenLluvia: TextView
    private lateinit var btnEditarRegistro: Button

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
    ) // IMPORTANTE: Mismo formato que al guardar

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

    /**
     * IMPORTANTE: onResume se ejecuta cada vez que esta pantalla vuelve a ser visible.
     * Aquí actualizamos los datos por si el usuario acaba de guardar un registro.
     */
    override fun onResume() {
        super.onResume()
        // Forzamos la actualización de la UI con la fecha que esté seleccionada
        updateUIForSelectedDate()
    }

    private fun initViews() {
        // Calendario
        txtMonth = findViewById(R.id.txtMonth)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        btnOpenDatePicker = findViewById(R.id.btnOpenDatePicker)
        recyclerDays = findViewById(R.id.recyclerDays)

        // Toolbar inferior
        btnMothy = findViewById(R.id.btnReportMonth)

        // Botón Today (puede ser nulo dependiendo del layout)
        val todayButton = findViewById<TextView?>(R.id.btnToday)
        if (todayButton != null) {
            btnToday = todayButton
        }

        // --- REFERENCIAS A LOS NUEVOS LAYOUTS (Debes haber actualizado el XML) ---
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
    }

    private fun setupRecyclerView() {
        daysAdapter = DaysAdapter(emptyList()) { day, position ->
            // Callback: Al hacer clic en un día del recycler
            if (day.isSelectable) {
                // Actualizamos el calendario seleccionado
                selectedCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
                selectedCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
                selectedCalendar.set(Calendar.DAY_OF_MONTH, day.dayNumber)

                // IMPORTANTE: Actualizar la tarjeta central inmediatamente
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
        // Navegación Meses
        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
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
            // Aquí lanzas tu Activity de Formulario (RegistroClimaActivity)
            // Podrías pasar la fecha seleccionada con .putExtra si quisieras que el formulario se abra en esa fecha
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

    /**
     * LÓGICA PRINCIPAL:
     * Consulta la base de datos para la fecha seleccionada y decide qué Layout mostrar.
     */
    private fun updateUIForSelectedDate() {
        val dateString = dbDateFormat.format(selectedCalendar.time)

        // 1. Consultar BD
        val registro = dbHelper.getRegistroByFecha(dateString)

        if (registro != null) {
            // CASO A: SÍ HAY DATOS
            // Ocultar estado vacío, mostrar estado de datos
            layoutEmptyState.visibility = View.GONE
            layoutDataState.visibility = View.VISIBLE

            // Llenar los datos del resumen
            tvResumenAmbiente.text = "${registro.tempAmbiente ?: "--"} °C"
            tvResumenMaxMin.text =
                "Máx: ${registro.tempMax ?: "--"}°C / Mín: ${registro.tempMin ?: "--"}°C"

            // Lógica visual para estado del tiempo y viento
            val cielo = registro.estadoTiempoObs ?: "Sin datos"
            val viento = registro.vientoDireccionObs ?: "-"
            tvResumenEstado.text = "$cielo, Viento $viento"

            tvResumenLluvia.text = "${registro.precipitacionMm ?: 0} mm"

        } else {
            // CASO B: NO HAY DATOS (NULL)
            // Mostrar estado vacío, ocultar estado de datos
            layoutEmptyState.visibility = View.VISIBLE
            layoutDataState.visibility = View.GONE

            // Personalizar el mensaje dependiendo si es HOY o un día PASADO
            val today = Calendar.getInstance()
            val isToday = dbDateFormat.format(today.time) == dateString

            if (isToday) {
                txtTituloEstado.text = "REGISTRO DE HOY"
                txtDescripcionEstado.text = "Aún no has registrado el clima de hoy."
                btnRegistrar.text = "REGISTRAR CLIMA DE HOY"
            } else {
                txtTituloEstado.text = "SIN REGISTRO"
                txtDescripcionEstado.text = "No hay datos para el día $dateString"
                btnRegistrar.text = "REGISTRAR ESTE DÍA"
            }
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