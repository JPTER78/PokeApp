package com.example.pokeapp.modelos

data class Venta(
    val idCarta: String,
    val direccion: String,
    val fecha: String,
    var estado: String,
    var imageUrl: String? = null,
    val emailVendedor: String? = null,  // Nuevo campo
    var emailComprador: String? = null  // Nuevo campo
)