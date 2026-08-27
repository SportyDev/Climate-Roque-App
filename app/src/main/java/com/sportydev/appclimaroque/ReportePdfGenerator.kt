package com.sportydev.appclimaroque

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm
import java.io.File
import java.util.Locale

class ReportePdfGenerator(private val context: Context) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun generarReporteReal(mes: Int, anio: Int, registros: List<RegistroClimatico>) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("plantilla.pdf")
            val document = PDDocument.load(inputStream)
            val acroForm: PDAcroForm? = document.documentCatalog.acroForm

            if (acroForm != null) {

                // 1. LLENAR CABECERAS
                acroForm.getField("mes")?.setValue(getNombreMes(mes))
                acroForm.getField("year")?.setValue(anio.toString())

                // 2. ORGANIZAR DATOS POR DÍA
                // Convertimos la lista en un mapa para buscar rápido: Mapa[Día] = Registro
                // Asume formato fecha "YYYY-MM-DD", tomamos los últimos 2 caracteres
                val mapaRegistros = registros.associateBy { it.fecha.takeLast(2).toInt() }

                // 3. LLENAR TABLA (Días 1 al 31)
                for (i in 1..31) {
                    // Buscamos si existe registro para el día 'i'
                    val dato = mapaRegistros[i]

                    if (dato != null) {
                        // SI HAY DATOS, LLENAMOS LOS CAMPOS

                        // Temperaturas
                        acroForm.getField("temp_amb_$i")
                            ?.setValue(dato.tempAmbiente?.toString() ?: "")
                        acroForm.getField("temp_max_$i")?.setValue(dato.tempMax?.toString() ?: "")
                        acroForm.getField("temp_min_$i")?.setValue(dato.tempMin?.toString() ?: "")

                        // Lluvia
                        acroForm.getField("lluvia_$i")
                            ?.setValue(dato.precipitacionMm?.toString() ?: "")

                        // Evaporación
                        acroForm.getField("evap_lec_$i")
                            ?.setValue(dato.evapLecturaMicrometro?.toString() ?: "")
                        acroForm.getField("evap_mm_$i")?.setValue(dato.evapMm?.toString() ?: "")
                        acroForm.getField("evap_24_$i")?.setValue(dato.evap24hr?.toString() ?: "")

                        // Helada (Si es true pone X, si no vacío)
                        val marcaHelada = if (dato.helada) "X" else ""
                        acroForm.getField("helada_$i")?.setValue(marcaHelada)

                        // Viento y Visibilidad
                        acroForm.getField("viento_$i")?.setValue(dato.vientoDireccionObs ?: "")
                        // Agregamos el "%" si existe el dato
                        val visibilidadStr =
                            if (dato.visibilidadPorcentajeObs != null) "${dato.visibilidadPorcentajeObs}%" else ""
                        acroForm.getField("visibilidad_$i")?.setValue(visibilidadStr)

                        // Fenómenos
                        acroForm.getField("fenomenos_$i")?.setValue(dato.fenomenosDiversos1hr ?: "")
                        acroForm.getField("fenomenos_24_$i")
                            ?.setValue(dato.fenomenosDiversos24hr ?: "")
                    }
                    // Si dato == null, no hacemos nada y el campo se queda vacío en el PDF
                }

                acroForm.flatten() // Aplanar para finalizar

            } else {
                Toast.makeText(context, "Error: PDF sin formulario", Toast.LENGTH_LONG).show()
                return
            }

            // 4. GUARDAR
            val nombreMes = getNombreMes(mes)
            val nombreArchivo = "Reporte_${nombreMes}_$anio.pdf"

            val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, nombreArchivo)

            document.save(file)
            document.close()

            // 5. ABRIR
            abrirPdf(file)

        } catch (e: Exception) {
            Log.e("PDF_GEN", "Error", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generarPrueba() {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("plantilla.pdf")
            val document = PDDocument.load(inputStream)
            val acroForm: PDAcroForm? = document.documentCatalog.acroForm

            if (acroForm != null) {
                // Ciclo para llenar los 31 días
                for (i in 1..31) {
                    acroForm.getField("temp_amb_$i")?.setValue("25.$i")
                    acroForm.getField("temp_max_$i")?.setValue("30.$i")
                    acroForm.getField("temp_min_$i")?.setValue("15.$i")
                    acroForm.getField("lluvia_$i")?.setValue("10.$i")
                    acroForm.getField("evap_lec_$i")?.setValue("5.$i")
                    acroForm.getField("evap_mm_$i")?.setValue("3.$i")
                    acroForm.getField("evap_24_$i")?.setValue("4.$i")
                    acroForm.getField("helada_$i")?.setValue("X")
                    acroForm.getField("viento_$i")?.setValue("Norte")
                    acroForm.getField("visibilidad_$i")?.setValue("100%")
                    acroForm.getField("fenomenos_$i")?.setValue("Niebla")
                    acroForm.getField("fenomenos_24_$i")?.setValue("Lluvia")
                }

                acroForm.getField("mes")?.setValue("SEPTIEMBRE")
                acroForm.getField("year")?.setValue("2025")

                acroForm.flatten()
            } else {
                Toast.makeText(
                    context,
                    "Error: El PDF no tiene formulario (AcroForm)",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // --- CAMBIO 1: ALMACENAMIENTO SEGURO ---
            val nombreArchivo = "Prueba_Completa_1-31_${System.currentTimeMillis()}.pdf"
            val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, nombreArchivo)

            document.save(file)
            document.close()

            // Abrir el PDF
            abrirPdf(file)

        } catch (e: Exception) {
            Log.e("PDF_ERROR", "Error generando PDF", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            val chooser = Intent.createChooser(intent, "Abrir Reporte")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No hay app para ver PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNombreMes(mes: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.MONTH, mes - 1)
        return java.text.SimpleDateFormat("MMMM", Locale("es", "ES")).format(cal.time).uppercase()
    }
}