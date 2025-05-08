package com.example.pokeapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokeapp.PokemonCard
import com.example.pokeapp.R

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
            .override(300, 300) // Tamaño fijo para consistencia
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = cards.size

    fun updateList(newList: List<PokemonCard>) {
        cards = newList
        notifyDataSetChanged()
    }
}