package com.sportydev.appclimaroque

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException

class AdminBdClima(private val contexto: Context) : SQLiteOpenHelper(contexto, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbPath: String = contexto.getDatabasePath(DATABASE_NAME).path

    companion object {
        // Información de la Base de Datos
        private const val DATABASE_NAME = "BDClima.db"
        private const val DATABASE_VERSION = 1

        // Nombre de la tabla
        const val TABLE_REGISTROS = "RegistrosClimaticos"

        // --- TODAS LAS NUEVAS COLUMNAS ---
        const val COL_ID = "id"
        const val COL_FECHA = "fecha"

        // Columnas: Observaciones 8 Horas
        const val COL_TEMP_AMBIENTE = "temp_ambiente"
        const val COL_TEMP_MAX = "temp_max"
        const val COL_TEMP_MIN = "temp_min"
        const val COL_PRECIP_MM = "precipitacion_mm"
        const val COL_EVAP_LEC_MICRO = "evap_lectura_micrometro"
        const val COL_EVAP_MM = "evap_mm"
        const val COL_EVAP_24HR = "evap_24hr"
        const val COL_HELADA = "helada" // INTEGER 0 o 1

        // Columnas: Estado a la hora de observacion
        const val COL_ESTADO_TIEMPO_OBS = "estado_tiempo_obs"
        const val COL_ESTADO_TEMP_OBS = "estado_temperatura_obs"
        const val COL_VIENTO_DIR_OBS = "viento_direccion_obs"
        const val COL_VISIBILIDAD_OBS = "visibilidad_porcentaje_obs"
        const val COL_FENOMENOS_1HR = "fenomenos_diversos_1hr"

        // Columnas: Estado 24 horas anteriores
        const val COL_ESTADO_TIEMPO_24HR = "estado_tiempo_24hr"
        const val COL_ESTADO_TEMP_24HR = "estado_temperatura_24hr"
        const val COL_VIENTO_DIR_24HR = "viento_direccion_24hr"
        const val COL_VISIBILIDAD_24HR = "visibilidad_porcentaje_24hr"
        const val COL_FENOMENOS_24HR = "fenomenos_diversos_24hr"
    }

    init {
        // Este bloque se asegura que la base de datos exista
        createDatabase()
    }

    /**
     * Comprueba si la base de datos ya existe; si no, la copia desde 'assets'.
     */
    fun createDatabase() {
        if (!databaseExists()) {
            this.readableDatabase
            this.close()
            try {
                copyDatabase()
            } catch (e: IOException) {
                throw Error("Error copiando la base de datos desde assets")
            }
        }
    }

    /**
     * Revisa si el archivo de la base de datos ya está en el directorio de la app.
     */
    private fun databaseExists(): Boolean {
        return contexto.getDatabasePath(DATABASE_NAME).exists()
    }

    /**
     * Copia la base de datos desde la carpeta 'assets' al directorio de la app.
     * !! IMPORTANTE: Coloca tu "BDClima.db" en la carpeta src/main/assets !!
     */
    private fun copyDatabase() {
        val inputStream = contexto.assets.open(DATABASE_NAME)
        val outputStream = FileOutputStream(dbPath)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    // Como la BD ya está creada, estos métodos van vacíos.
    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}


    // --- Métodos CRUD (Actualizados a 20 columnas) ---

    /**
     * Inserta un nuevo registro climático en la base de datos.
     */
    fun addRegistro(registro: RegistroClimatico): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_FECHA, registro.fecha)

            // Parte 1
            put(COL_TEMP_AMBIENTE, registro.tempAmbiente)
            put(COL_TEMP_MAX, registro.tempMax)
            put(COL_TEMP_MIN, registro.tempMin)
            put(COL_PRECIP_MM, registro.precipitacionMm)
            put(COL_EVAP_LEC_MICRO, registro.evapLecturaMicrometro)
            put(COL_EVAP_MM, registro.evapMm)
            put(COL_EVAP_24HR, registro.evap24hr)
            put(COL_HELADA, if (registro.helada) 1 else 0) // Convertir Boolean a Int

            // Parte 2
            put(COL_ESTADO_TIEMPO_OBS, registro.estadoTiempoObs)
            put(COL_ESTADO_TEMP_OBS, registro.estadoTemperaturaObs)
            put(COL_VIENTO_DIR_OBS, registro.vientoDireccionObs)
            put(COL_VISIBILIDAD_OBS, registro.visibilidadPorcentajeObs)
            put(COL_FENOMENOS_1HR, registro.fenomenosDiversos1hr)

            // Parte 3
            put(COL_ESTADO_TIEMPO_24HR, registro.estadoTiempo24hr)
            put(COL_ESTADO_TEMP_24HR, registro.estadoTemperatura24hr)
            put(COL_VIENTO_DIR_24HR, registro.vientoDireccion24hr)
            put(COL_VISIBILIDAD_24HR, registro.visibilidadPorcentaje24hr)
            put(COL_FENOMENOS_24HR, registro.fenomenosDiversos24hr)
        }

        val id = db.insert(TABLE_REGISTROS, null, values)
        //db.close()
        return id
    }

    /**
     * Obtiene un registro específico usando su fecha (que es ÚNICA).
     */
    fun getRegistroByFecha(fecha: String): RegistroClimatico? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_REGISTROS,
            null, // Todas las columnas
            "$COL_FECHA = ?", // Clausula WHERE
            arrayOf(fecha), // Argumentos del WHERE
            null, null, null
        )

        var registro: RegistroClimatico? = null
        if (cursor.moveToFirst()) {
            registro = cursorToRegistro(cursor)
        }

        cursor.close()
        //db.close()
        return registro
    }

    /**
     * Obtiene todos los registros climáticos ordenados por fecha (más reciente primero).
     */
    fun getAllRegistros(): List<RegistroClimatico> {
        val listaRegistros = mutableListOf<RegistroClimatico>()
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_REGISTROS ORDER BY $COL_FECHA DESC", null)

        if (cursor.moveToFirst()) {
            do {
                listaRegistros.add(cursorToRegistro(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        //db.close()
        return listaRegistros
    }

    /**
     * Actualiza un registro existente basado en su ID.
     */
    fun updateRegistro(registro: RegistroClimatico): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_FECHA, registro.fecha)

            // Parte 1
            put(COL_TEMP_AMBIENTE, registro.tempAmbiente)
            put(COL_TEMP_MAX, registro.tempMax)
            put(COL_TEMP_MIN, registro.tempMin)
            put(COL_PRECIP_MM, registro.precipitacionMm)
            put(COL_EVAP_LEC_MICRO, registro.evapLecturaMicrometro)
            put(COL_EVAP_MM, registro.evapMm)
            put(COL_EVAP_24HR, registro.evap24hr)
            put(COL_HELADA, if (registro.helada) 1 else 0)

            // Parte 2
            put(COL_ESTADO_TIEMPO_OBS, registro.estadoTiempoObs)
            put(COL_ESTADO_TEMP_OBS, registro.estadoTemperaturaObs)
            put(COL_VIENTO_DIR_OBS, registro.vientoDireccionObs)
            put(COL_VISIBILIDAD_OBS, registro.visibilidadPorcentajeObs)
            put(COL_FENOMENOS_1HR, registro.fenomenosDiversos1hr)

            // Parte 3
            put(COL_ESTADO_TIEMPO_24HR, registro.estadoTiempo24hr)
            put(COL_ESTADO_TEMP_24HR, registro.estadoTemperatura24hr)
            put(COL_VIENTO_DIR_24HR, registro.vientoDireccion24hr)
            put(COL_VISIBILIDAD_24HR, registro.visibilidadPorcentaje24hr)
            put(COL_FENOMENOS_24HR, registro.fenomenosDiversos24hr)
        }

        val rowsAffected = db.update(
            TABLE_REGISTROS,
            values,
            "$COL_ID = ?",
            arrayOf(registro.id.toString())
        )

        //db.close()
        return rowsAffected
    }

    /**
     * Borra un registro de la base de datos usando su ID.
     */
    fun deleteRegistro(id: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(
            TABLE_REGISTROS,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )
        //db.close()
        return rowsAffected
    }

    /**
     * Función ayudante para convertir un Cursor a un objeto RegistroClimatico.
     */
    @SuppressLint("Range")
    private fun cursorToRegistro(cursor: Cursor): RegistroClimatico {
        // Helper para leer Double o Null
        fun getDouble(colName: String): Double? {
            val index = cursor.getColumnIndex(colName)
            return if (cursor.isNull(index)) null else cursor.getDouble(index)
        }
        // Helper para leer Int o Null
        fun getInt(colName: String): Int? {
            val index = cursor.getColumnIndex(colName)
            return if (cursor.isNull(index)) null else cursor.getInt(index)
        }
        // Helper para leer String o Null
        fun getString(colName: String): String? {
            val index = cursor.getColumnIndex(colName)
            return if (cursor.isNull(index)) null else cursor.getString(index)
        }

        return RegistroClimatico(
            id = cursor.getInt(cursor.getColumnIndex(COL_ID)),
            fecha = cursor.getString(cursor.getColumnIndex(COL_FECHA)),

            // Parte 1
            tempAmbiente = getDouble(COL_TEMP_AMBIENTE),
            tempMax = getDouble(COL_TEMP_MAX),
            tempMin = getDouble(COL_TEMP_MIN),
            precipitacionMm = getDouble(COL_PRECIP_MM),
            evapLecturaMicrometro = getDouble(COL_EVAP_LEC_MICRO),
            evapMm = getDouble(COL_EVAP_MM),
            evap24hr = getDouble(COL_EVAP_24HR),
            helada = cursor.getInt(cursor.getColumnIndex(COL_HELADA)) == 1, // Convertir Int a Boolean

            // Parte 2
            estadoTiempoObs = getString(COL_ESTADO_TIEMPO_OBS),
            estadoTemperaturaObs = getString(COL_ESTADO_TEMP_OBS),
            vientoDireccionObs = getString(COL_VIENTO_DIR_OBS),
            visibilidadPorcentajeObs = getInt(COL_VISIBILIDAD_OBS),
            fenomenosDiversos1hr = getString(COL_FENOMENOS_1HR),

            // Parte 3
            estadoTiempo24hr = getString(COL_ESTADO_TIEMPO_24HR),
            estadoTemperatura24hr = getString(COL_ESTADO_TEMP_24HR),
            vientoDireccion24hr = getString(COL_VIENTO_DIR_24HR),
            visibilidadPorcentaje24hr = getInt(COL_VISIBILIDAD_24HR),
            fenomenosDiversos24hr = getString(COL_FENOMENOS_24HR)
        )
    }
}