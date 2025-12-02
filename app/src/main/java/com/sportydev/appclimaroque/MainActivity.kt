package com.sportydev.appclimaroque

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var txtMonth: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var btnOpenDatePicker: ImageButton
    private var btnToday: TextView? = null // Cambiar a TextView
    private lateinit var recyclerDays: RecyclerView
    private lateinit var daysAdapter: DaysAdapter
    private lateinit var btnOpen: Button
    private lateinit var btnMothy: ImageButton

    private var currentCalendar = Calendar.getInstance()
    private var selectedCalendar = Calendar.getInstance() // Nuevo: para rastrear el día seleccionado
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    private val dayNameFormat = SimpleDateFormat("EEE", Locale("es", "ES"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        updateCalendar()
    }

    private fun initViews() {
        txtMonth = findViewById(R.id.txtMonth)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        btnOpenDatePicker = findViewById(R.id.btnOpenDatePicker)
        recyclerDays = findViewById(R.id.recyclerDays)
        btnOpen = findViewById(R.id.btnOpenBottomSheet)
        btnMothy = findViewById(R.id.btnReportMonth)

        // Inicializar btnToday solo si existe en el layout
        val todayButton = findViewById<TextView?>(R.id.btnToday)
        if (todayButton != null) {
            btnToday = todayButton
        }
    }

    private fun setupRecyclerView() {
        daysAdapter = DaysAdapter(emptyList()) { day, position ->
            // Callback cuando se selecciona un día manualmente
            // Solo proceder si el día es seleccionable
            if (day.isSelectable) {
                selectedCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
                selectedCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
                selectedCalendar.set(Calendar.DAY_OF_MONTH, day.dayNumber)
            }
        }

        recyclerDays.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = daysAdapter
        }
    }

    private fun setupClickListeners() {
        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        btnMothy.setOnClickListener {
            val intent = Intent(this, ActivityMonthlyReports::class.java)
            startActivity(intent)
        }

        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        btnOpenDatePicker.setOnClickListener {
            showDatePicker()
        }

        btnToday?.setOnClickListener {
            goToToday()
        }
        btnOpen.setOnClickListener {
            val intent = Intent(this, RegistroClimaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateCalendar() {
        // Actualizar el texto del mes
        txtMonth.text = monthFormat.format(currentCalendar.time).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        // Generar los días del mes
        val days = generateDaysForMonth()
        daysAdapter.updateDays(days)

        // Hacer scroll al día de hoy si estamos en el mes actual
        scrollToTodayIfCurrentMonth()

        // Actualizar el estado del botón HOY
        updateTodayButtonState()
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

            // Un día es seleccionable solo si es hoy o anterior a hoy
            val isSelectable = tempCalendar.timeInMillis <= today.timeInMillis

            val isSelected = tempCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                    tempCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                    tempCalendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH) &&
                    !isToday && isSelectable // Solo puede estar seleccionado si es seleccionable

            days.add(Day(
                dayNumber = dayNumber,
                dayName = dayName,
                isToday = isToday,
                isSelected = isSelected,
                isSelectable = isSelectable
            ))
        }

        return days
    }

    private fun scrollToTodayIfCurrentMonth() {
        val today = Calendar.getInstance()

        // Si estamos en el mes actual, hacer scroll al día de hoy
        if (currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {

            val todayPosition = today.get(Calendar.DAY_OF_MONTH) - 1
            recyclerDays.post {
                (recyclerDays.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(todayPosition, 100)
            }
        }
        // Si hay un día seleccionado en el mes actual, hacer scroll a él
        else if (currentCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
            currentCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH)) {

            val selectedPosition = selectedCalendar.get(Calendar.DAY_OF_MONTH) - 1
            recyclerDays.post {
                (recyclerDays.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(selectedPosition, 100)
                // También seleccionar el día en el adaptador
                daysAdapter.selectDay(selectedPosition)
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

                // Solo permitir seleccionar fechas de hoy hacia atrás
                if (selectedDate.timeInMillis <= today.timeInMillis) {
                    // Actualizar el calendario seleccionado
                    selectedCalendar.set(year, month, dayOfMonth)

                    // Si el mes seleccionado es diferente al actual, cambiar el mes
                    if (currentCalendar.get(Calendar.YEAR) != year ||
                        currentCalendar.get(Calendar.MONTH) != month) {
                        currentCalendar.set(Calendar.YEAR, year)
                        currentCalendar.set(Calendar.MONTH, month)
                    }

                    updateCalendar()
                }
                // Si intentan seleccionar una fecha futura, no hacer nada o mostrar mensaje
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Establecer la fecha máxima como hoy
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun goToToday() {
        val today = Calendar.getInstance()

        // Establecer el calendario actual al mes de hoy
        currentCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR))
        currentCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH))

        // Establecer el día seleccionado como hoy
        selectedCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR))
        selectedCalendar.set(Calendar.MONTH, today.get(Calendar.MONTH))
        selectedCalendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))

        updateCalendar()
    }

    private fun updateTodayButtonState() {
        btnToday?.let { button ->
            val today = Calendar.getInstance()
            val isCurrentMonth = currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
            val isTodaySelected = selectedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    selectedCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            // Si estamos en el mes actual y hoy está seleccionado, el botón se ve diferente
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