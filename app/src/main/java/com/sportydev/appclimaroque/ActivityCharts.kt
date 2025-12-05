package com.sportydev.appclimaroque

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class ActivityCharts : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private lateinit var btnVolver: Button
    private lateinit var dbHelper: AdminBdClima

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        dbHelper = AdminBdClima(this)

        lineChart = findViewById(R.id.chartTemperaturas)
        barChart = findViewById(R.id.chartLluvia)
        btnVolver = findViewById(R.id.btnVolver)

        btnVolver.setOnClickListener { finish() }

        cargarGraficas()
    }

    private fun cargarGraficas() {
        // 1. Obtener todos los registros de la BD (Ordenados por fecha)
        // Nota: Asegúrate de que tu getAllRegistros() use "ORDER BY fecha ASC" para que la gráfica tenga sentido cronológico.
        // Si tu query actual es DESC, invierte la lista aquí con .reversed()
        val registros = dbHelper.getAllRegistros().reversed()

        if (registros.isEmpty()) return

        // --- PREPARAR DATOS PARA LÍNEAS (TEMPERATURA) ---
        val entriesMax = ArrayList<Entry>()
        val entriesMin = ArrayList<Entry>()
        val entriesAmb = ArrayList<Entry>()
        val fechasLabels = ArrayList<String>()

        registros.forEachIndexed { index, registro ->
            // Eje X = indice (0, 1, 2...), Eje Y = Temperatura
            entriesMax.add(Entry(index.toFloat(), registro.tempMax?.toFloat() ?: 0f))
            entriesMin.add(Entry(index.toFloat(), registro.tempMin?.toFloat() ?: 0f))
            entriesAmb.add(Entry(index.toFloat(), registro.tempAmbiente?.toFloat() ?: 0f))

            // Guardamos la fecha corta para ponerla abajo en la gráfica
            fechasLabels.add(registro.fecha.takeLast(5)) // Ej: "2025-12-01" -> "12-01"
        }

        // Configurar Línea Máxima (Roja)
        val setMax = LineDataSet(entriesMax, "Temp Máx")
        setMax.color = Color.RED
        setMax.setCircleColor(Color.RED)
        setMax.lineWidth = 2f

        // Configurar Línea Mínima (Azul)
        val setMin = LineDataSet(entriesMin, "Temp Mín")
        setMin.color = Color.BLUE
        setMin.setCircleColor(Color.BLUE)
        setMin.lineWidth = 2f

        // Configurar Línea Ambiente (Verde)
        val setAmb = LineDataSet(entriesAmb, "Temp Amb")
        setAmb.color = Color.GREEN
        setAmb.setCircleColor(Color.GREEN)
        setAmb.lineWidth = 2f

        val lineData = LineData(setMax, setMin, setAmb)
        lineChart.data = lineData

        // Embellecer Gráfica de Líneas
        lineChart.description.isEnabled = false
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(fechasLabels)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f
        lineChart.animateX(1000)
        lineChart.invalidate() // Refrescar

        // --- PREPARAR DATOS PARA BARRAS (LLUVIA) ---
        val entriesLluvia = ArrayList<BarEntry>()

        registros.forEachIndexed { index, registro ->
            entriesLluvia.add(BarEntry(index.toFloat(), registro.precipitacionMm?.toFloat() ?: 0f))
        }

        val setLluvia = BarDataSet(entriesLluvia, "Lluvia (mm)")
        setLluvia.colors = ColorTemplate.MATERIAL_COLORS.toList()
        setLluvia.valueTextSize = 10f

        val barData = BarData(setLluvia)
        barChart.data = barData

        // Embellecer Gráfica de Barras
        barChart.description.isEnabled = false
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(fechasLabels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.animateY(1000)
        barChart.invalidate()
    }
}