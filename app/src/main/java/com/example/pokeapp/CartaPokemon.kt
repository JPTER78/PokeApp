package com.example.pokeapp

data class PokemonCardResponse(
    val data: List<PokemonCard>
)

data class PokemonCard(
    val name: String,
    val types: List<String>?,
    val images: CardImage
)

data class CardImage(
    val large: String
)
