package com.example.pokeapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokeapp.PokemonCard
import com.example.pokeapp.R
import com.example.pokeapp.adapters.FavoritosAdapter
import com.example.pokeapp.decorations.GridSpacingItemDecoration
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FragmentHome : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeText: TextView
    private lateinit var adapter: FavoritosAdapter
    private val client = OkHttpClient()
    private val favoriteCards = mutableListOf<PokemonCard>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.rvFavorites)
        welcomeText = view.findViewById(R.id.tvWelcome)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = requireContext()
            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("email", "") ?: ""

        welcomeText.text = "¡Bienvenido, $email!"

        adapter = FavoritosAdapter(favoriteCards)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(2, 16, true)
        )
        recyclerView.adapter = adapter

        fetchFavorites(email)
    }

    private fun fetchFavorites(email: String) {
        val request = Request.Builder()
            .url("http://10.152.94.33:8080/favoritos")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast("Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        showToast("Error ${response.code}")
                        return
                    }

                    val jsonArray = JSONArray(response.body?.string() ?: "[]")
                    val cards = mutableListOf<PokemonCard>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val itemEmail = item.getString("emailUsuario")
                        if (itemEmail == email) {
                            val idCarta = item.getString("idCarta")
                            fetchCardDetails(idCarta, cards)
                        }
                    }
                }
            }
        })
    }

    private fun fetchCardDetails(cardId: String, cardsList: MutableList<PokemonCard>) {
        val request = Request.Builder()
            .url("https://api.pokemontcg.io/v2/cards/$cardId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast("Error al cargar carta: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        showToast("Error ${response.code}")
                        return
                    }

                    val json = JSONObject(response.body?.string() ?: "")
                    val data = json.getJSONObject("data")
                    val id = data.getString("id")
                    val name = data.getString("name")
                    val images = data.getJSONObject("images")
                    val imageLarge = images.getString("large")
                    val types = if (data.has("types")) {
                        val typesArray = data.getJSONArray("types")
                        List(typesArray.length()) { typesArray.getString(it) }
                    } else {
                        null
                    }

                    val card = PokemonCard(
                        id = id,
                        name = name,
                        images = com.example.pokeapp.CardImage(imageLarge),
                        types = types
                    )

                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            favoriteCards.add(card)
                            adapter.notifyItemInserted(favoriteCards.size - 1)
                        }
                    }
                }
            }
        })
    }

    private fun showToast(message: String) {
        if (isAdded) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
