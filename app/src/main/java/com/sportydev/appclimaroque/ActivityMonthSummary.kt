package com.sportydev.appclimaroque

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActivityMonthSummary : AppCompatActivity() {

    private lateinit var dbHelper: AdminBdClima
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_month_summary)

        dbHelper = AdminBdClima(this)
        tvTitle = findViewById(R.id.tvSummaryTitle)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // Recibir datos del Intent
        val monthName = intent.getStringExtra("MONTH_NAME") ?: "MES"
        val monthNum = intent.getIntExtra("MONTH_NUMBER", 1)
        val year = intent.getIntExtra("YEAR", 2025)

        tvTitle.text = "RESUMEN: ${monthName.uppercase()} $year"

        // Calcular y Mostrar
        val datos = dbHelper.calcularResumenMensual(monthNum, year)
        llenarVistas(datos)
    }

    private fun llenarVistas(d: ResumenMensualData) {
        // Helper para setear filas (ID del include, Label, Valor)
        fun setRow(includeId: Int, label: String, value: String) {
            val view = findViewById<View>(includeId)
            view.findViewById<TextView>(R.id.tvLabel).text = label
            view.findViewById<TextView>(R.id.tvValue).text = value
        }

        val df = "%.1f" // Formato decimal

        // 1. Temperaturas
        setRow(
            R.id.rowTempMax,
            "Máxima en el mes:",
            "${String.format(df, d.tempMaxAbs ?: 0.0)}°C (Día ${d.diaTempMax})"
        )
        setRow(
            R.id.rowTempMin,
            "Mínima en el mes:",
            "${String.format(df, d.tempMinAbs ?: 0.0)}°C (Día ${d.diaTempMin})"
        )
        setRow(R.id.rowTempMedia, "Media del mes:", "${String.format(df, d.tempMedia)}°C")

        // 2. Lluvia
        setRow(
            R.id.rowLluviaMax24,
            "Máxima en 24h:",
            "${String.format(df, d.lluviaMax24 ?: 0.0)} mm (Día ${d.diaLluviaMax})"
        )
        setRow(
            R.id.rowLluviaMin24,
            "Mínima en 24h:",
            "${String.format(df, d.lluviaMin24 ?: 0.0)} mm (Día ${d.diaLluviaMin})"
        )
        setRow(R.id.rowLluviaMedia, "Media del mes:", "${String.format(df, d.lluviaMedia)} mm")
        setRow(R.id.rowLluviaTotal, "TOTAL del mes:", "${String.format(df, d.lluviaTotal)} mm")

        // 3. Evaporación
        setRow(
            R.id.rowEvapMax,
            "Máxima en el mes:",
            "${String.format(df, d.evapMax ?: 0.0)} mm (Día ${d.diaEvapMax})"
        )
        setRow(
            R.id.rowEvapMin,
            "Mínima en el mes:",
            "${String.format(df, d.evapMin ?: 0.0)} mm (Día ${d.diaEvapMin})"
        )
        setRow(R.id.rowEvapMedia, "Media del mes:", "${String.format(df, d.evapMedia)} mm")
        setRow(R.id.rowEvapTotal, "TOTAL del mes:", "${String.format(df, d.evapTotal)} mm")

        // 4. Conteo Días
        setRow(R.id.rowDiasLluvia, "Lluvia (≥ 0.1mm):", "${d.diasLluvia} días")
        setRow(R.id.rowDiasLluviaInap, "Lluvia inapreciable:", "${d.diasLluviaInap} días")
        setRow(R.id.rowDiasNiebla, "Niebla / Neblina:", "${d.diasNiebla} días")
        setRow(R.id.rowDiasTempestad, "Tempestad eléctrica:", "${d.diasTempestad} días")
        setRow(R.id.rowDiasNieve, "Nevadas:", "${d.diasNieve} días")
        setRow(R.id.rowDiasGranizo, "Granizo:", "${d.diasGranizo} días")

        // 5. Cielo
        setRow(R.id.rowCieloDespejado, "Despejados:", "${d.diasDespejado} días")
        setRow(R.id.rowCieloMedio, "Medio Nublados:", "${d.diasMedioNublado} días")
        setRow(R.id.rowCieloNublado, "Nublados:", "${d.diasNublado} días")
    }
}