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

    init {
        fetchPokemonCards()
    }

    fun fetchPokemonCards() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getListadoCartas()
                _pokemonCards.value = response.data
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterCards(query: String) {
        val currentList = _pokemonCards.value ?: return
        if (query.isEmpty()) {
            _pokemonCards.value = currentList
        } else {
            _pokemonCards.value = currentList.filter { card ->
                card.name.contains(query, ignoreCase = true)
            }
        }
    }
}