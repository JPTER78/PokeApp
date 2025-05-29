package com.example.pokeapp.modelos

data class VentaSubida(
    val emailVendedor: String,
    val idCarta: String,
    val emailComprador: String? = "gmailplaceholder@gmail.com",
    val estado: String = "Disponible",
    val idioma: String,
    val direccion: String,
    val fecha: String
)
