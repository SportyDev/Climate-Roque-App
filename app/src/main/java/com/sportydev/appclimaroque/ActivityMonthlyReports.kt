package com.sportydev.appclimaroque

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityMonthlyReports : AppCompatActivity() {

    private lateinit var btnClose: ImageButton
    private lateinit var btnPrevMonth: ImageButton

    // CardViews para cada mes
    private lateinit var cardEnero: CardView
    private lateinit var cardFebrero: CardView
    private lateinit var cardMarzo: CardView
    private lateinit var cardAbril: CardView
    private lateinit var cardMayo: CardView
    private lateinit var cardJunio: CardView
    private lateinit var cardJulio: CardView
    private lateinit var cardAgosto: CardView
    private lateinit var cardSeptiembre: CardView
    private lateinit var cardOctubre: CardView
    private lateinit var cardNoviembre: CardView
    private lateinit var cardDiciembre: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_monthly_reports)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        // Botones del toolbar
        btnClose = findViewById(R.id.btnClose)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)

        // CardViews de los meses
        cardEnero = findViewById(R.id.cardEnero)
        cardFebrero = findViewById(R.id.cardFebrero)
        cardMarzo = findViewById(R.id.cardMarzo)
        cardAbril = findViewById(R.id.cardAbril)
        cardMayo = findViewById(R.id.cardMayo)
        cardJunio = findViewById(R.id.cardJunio)
        cardJulio = findViewById(R.id.cardJulio)
        cardAgosto = findViewById(R.id.cardAgosto)
        cardSeptiembre = findViewById(R.id.cardSeptiembre)
        cardOctubre = findViewById(R.id.cardOctubre)
        cardNoviembre = findViewById(R.id.cardNoviembre)
        cardDiciembre = findViewById(R.id.cardDiciembre)
    }

    private fun setupClickListeners() {
        // Botón cerrar
        btnClose.setOnClickListener {
            finish()
        }

        // Botón mes anterior (puedes implementar lógica específica)
        btnPrevMonth.setOnClickListener {
            // Implementar navegación al mes anterior si es necesario
        }

        // Listeners para cada mes
        cardEnero.setOnClickListener {
            showMonthDetailDialog("Enero", 1)
        }

        cardFebrero.setOnClickListener {
            showMonthDetailDialog("Febrero", 2)
        }

        cardMarzo.setOnClickListener {
            showMonthDetailDialog("Marzo", 3)
        }

        cardAbril.setOnClickListener {
            showMonthDetailDialog("Abril", 4)
        }

        cardMayo.setOnClickListener {
            showMonthDetailDialog("Mayo", 5)
        }

        cardJunio.setOnClickListener {
            showMonthDetailDialog("Junio", 6)
        }

        cardJulio.setOnClickListener {
            showMonthDetailDialog("Julio", 7)
        }

        cardAgosto.setOnClickListener {
            showMonthDetailDialog("Agosto", 8)
        }

        cardSeptiembre.setOnClickListener {
            showMonthDetailDialog("Septiembre", 9)
        }

        cardOctubre.setOnClickListener {
            showMonthDetailDialog("Octubre", 10)
        }

        cardNoviembre.setOnClickListener {
            showMonthDetailDialog("Noviembre", 11)
        }

        cardDiciembre.setOnClickListener {
            showMonthDetailDialog("Diciembre", 12)
        }
    }

    private fun showMonthDetailDialog(monthName: String, monthNumber: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_month_detail)

        // Configurar el diálogo
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Referencias a las vistas del diálogo
        val titleText: TextView = dialog.findViewById(R.id.tvDialogTitle)
        val monthText: TextView = dialog.findViewById(R.id.tvMonthName)
        val closeButton: ImageButton = dialog.findViewById(R.id.btnCloseDialog)
        val generateReportButton: View = dialog.findViewById(R.id.btnGenerateReport)
        val viewDataButton: View = dialog.findViewById(R.id.btnViewData)

        // Configurar contenido
        titleText.text = "Reporte Mensual"
        monthText.text = "$monthName 2025"

        // Botón cerrar diálogo
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Botón generar reporte
        generateReportButton.setOnClickListener {
            generateMonthlyReport(monthName, monthNumber)
            dialog.dismiss()
        }

        // Botón ver datos
        viewDataButton.setOnClickListener {
            viewMonthData(monthName, monthNumber)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun generateMonthlyReport(monthName: String, monthNumber: Int) {
        // 1. Obtener instancia de BD
        val db = AdminBdClima(this)

        // TODO: Asegúrate de tener el año correcto (puedes pasarlo como extra o usar el actual)
        val anio = 2025

        // 2. Obtener los registros de la BD
        val listaRegistros = db.getRegistrosPorMes(monthNumber, anio)

        if (listaRegistros.isEmpty()) {
            showToast("No hay registros guardados para $monthName $anio")
            // Aún así podríamos querer generar el reporte vacío, tú decides.
            // Si quieres generar aunque esté vacío, quita el return.
            // return
        }

        // 3. Generar el PDF Real
        val pdfGenerator = ReportePdfGenerator(this)
        pdfGenerator.generarReporteReal(monthNumber, anio, listaRegistros)
    }

    private fun viewMonthData(monthName: String, monthNumber: Int) {
        val intent = Intent(this, ActivityMonthSummary::class.java)
        intent.putExtra("MONTH_NAME", monthName)
        intent.putExtra("MONTH_NUMBER", monthNumber)
        intent.putExtra(
            "YEAR",
            2025
        ) // Ojo: Aquí deberías manejar el año dinámicamente si tu app crece
        startActivity(intent)
    }

    // Función auxiliar para mostrar mensajes (opcional)
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}