package com.sportydev.appclimaroque

data class ResumenMensualData(
    // Temperaturas
    var tempMaxAbs: Double? = null,
    var diaTempMax: String = "",
    var tempMinAbs: Double? = null,
    var diaTempMin: String = "",
    var tempMedia: Double = 0.0,

    // Lluvia
    var lluviaTotal: Double = 0.0,
    var lluviaMedia: Double = 0.0,
    var lluviaMax24: Double? = null,
    var diaLluviaMax: String = "",
    var lluviaMin24: Double? = null, // Generalmente 0
    var diaLluviaMin: String = "",

    // Evaporación
    var evapTotal: Double = 0.0,
    var evapMedia: Double = 0.0,
    var evapMax: Double? = null,
    var diaEvapMax: String = "",
    var evapMin: Double? = null,
    var diaEvapMin: String = "",

    // Conteo Días
    var diasLluvia: Int = 0, // > 0.1mm
    var diasLluviaInap: Int = 0, // Inapreciable (depende de tu lógica, ej: >0 y <0.1 o flag específica)
    var diasNiebla: Int = 0,
    var diasTempestad: Int = 0,
    var diasNieve: Int = 0,
    var diasGranizo: Int = 0,

    // Nubosidad
    var diasDespejado: Int = 0,
    var diasMedioNublado: Int = 0,
    var diasNublado: Int = 0
)
