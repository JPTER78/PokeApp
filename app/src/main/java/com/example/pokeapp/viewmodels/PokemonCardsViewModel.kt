package com.example.pokeapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokeapp.PokemonCard
import com.example.pokeapp.RetrofitInstance
import kotlinx.coroutines.launch

class PokemonCardsViewModel : ViewModel() {
    private val _pokemonCards = MutableLiveData<List<PokemonCard>>()
    val pokemonCards: LiveData<List<PokemonCard>> = _pokemonCards

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var originalCards: List<PokemonCard> = listOf()
    private var currentQuery: String = ""
    private var currentTypeFilter: String? = null

    init {
        fetchPokemonCards()
    }

    fun fetchPokemonCards() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getListadoCartas()
                originalCards = response.data
                _pokemonCards.value = response.data
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterCards(query: String = currentQuery, type: String? = currentTypeFilter) {
        currentQuery = query
        currentTypeFilter = type

        val filteredList = originalCards.filter { card ->
            val matchesQuery = card.name.contains(query, ignoreCase = true)
            val matchesType = type.isNullOrBlank() || card.types?.any {
                it.equals(type, ignoreCase = true)
            } == true

            matchesQuery && matchesType
        }

        _pokemonCards.value = filteredList
    }

    fun applyTypeFilter(type: String?) {
        filterCards(query = currentQuery, type = type)
    }

    fun clearFilters() {
        currentQuery = ""
        currentTypeFilter = null
        _pokemonCards.value = originalCards
    }
}
