package com.example.pokeapp.adapters

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokeapp.PokemonCard
import com.example.pokeapp.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PokemonCardAdapter(
    private var cards: List<PokemonCard>,
    private val onItemClick: (PokemonCard) -> Unit
) : RecyclerView.Adapter<PokemonCardAdapter.PokemonCardViewHolder>() {

    inner class PokemonCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgPokemonCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_card, parent, false)
        return PokemonCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonCardViewHolder, position: Int) {
        val card = cards[position]

        Glide.with(holder.itemView.context)
            .load(card.images.large)
            .placeholder(R.drawable.pokemon_placeholder)
            .error(R.drawable.error)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            showFavoritesDialog(holder.itemView.context, card.id, card.name)
            onItemClick(card)
        }
    }

    private fun showFavoritesDialog(context: Context, cardId: String, cardName: String) {
        val dialog = Dialog(context).apply {
            setContentView(R.layout.dialog_favoritos)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            findViewById<TextView>(R.id.tvDialog).text =
                "¿Quieres guardar '$cardName' (ID: $cardId) en favoritos?"
        }

        dialog.findViewById<Button>(R.id.btnSi).setOnClickListener {
            saveToFavorites(context, cardId)
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveToFavorites(context: Context, cardId: String) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("email", "") ?: ""

        if (email.isEmpty()) {
            showToast(context, "Usuario no identificado")
            return
        }

        if (cardId.isEmpty()) {
            showToast(context, "ID de carta inválido")
            return
        }

        try {
            val json = JSONObject().apply {
                put("emailUsuario", email)
                put("idCarta", cardId)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("http://10.152.94.33:8080/favoritos")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast(context, "Error de conexión: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        if (response.isSuccessful) {
                            showToast(context, "¡Carta añadida a favoritos!")
                        } else {
                            showToast(context, "Error ${response.code}: $responseBody")
                        }
                    } catch (e: Exception) {
                        showToast(context, "Error al procesar respuesta")
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (e: Exception) {
            showToast(context, "Error creando la petición: ${e.message}")
        }
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = cards.size

    fun updateList(newList: List<PokemonCard>) {
        cards = newList
        notifyDataSetChanged()
    }
}