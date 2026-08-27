package com.sportydev.appclimaroque

/**
 * Clase maestra que representa una fila completa de la base de datos.
 * Contiene la unión de los datos de los 3 fragmentos.
 */
data class RegistroClimatico(
    val id: Int = 0, // 0 por defecto para nuevos registros (autoincrement)
    val fecha: String, // Fecha y Hora (Única)

    val tempAmbiente: Double?,
    val tempMax: Double?,
    val tempMin: Double?,
    val precipitacionMm: Double?,
    val evapLecturaMicrometro: Double?,
    val evapMm: Double?,
    val evap24hr: Double?,
    val helada: Boolean,

    val estadoTiempoObs: String?,       // Despejado, Nublado...
    val estadoTemperaturaObs: String?,  // Frio, Fresco...
    val vientoDireccionObs: String?,
    val visibilidadPorcentajeObs: Int?, // Nota: En tu BD es INTEGER
    val fenomenosDiversos1hr: String?,

    val estadoTiempo24hr: String?,
    val estadoTemperatura24hr: String?,
    val vientoDireccion24hr: String?,
    val visibilidadPorcentaje24hr: Int?,
    val fenomenosDiversos24hr: String?
)