package com.example.app

import com.google.android.gms.maps.model.LatLng

data class Zona(
    val nombre: String,
    var coordenadas: LatLng,
    var rango: Int,
    var nombreUbicacion: String? = null
)
