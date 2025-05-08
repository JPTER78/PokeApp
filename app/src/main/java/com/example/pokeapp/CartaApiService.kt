package com.example.pokeapp

import retrofit2.http.GET

interface CartaApiService {
    @GET("cards")
    suspend fun getListadoCartas(): PokemonCardResponse
}