package com.example.app

import com.google.android.gms.maps.model.LatLng

data class Zona(
    val nombre: String,
    var coordenadas: LatLng,
    var rango: Int,
    var nombreUbicacion: String? = null,
    var documentId: String? = null // Agregar el ID del documento
) {
    // Método para convertir las coordenadas LatLng a GeoPoint
    fun toGeoPoint(): com.google.firebase.firestore.GeoPoint {
        return com.google.firebase.firestore.GeoPoint(coordenadas.latitude, coordenadas.longitude)
    }
}
