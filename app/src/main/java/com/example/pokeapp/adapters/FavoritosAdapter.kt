package com.example.pokeapp.adapters

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokeapp.PokemonCard
import com.example.pokeapp.R

class FavoritosAdapter(
    private var favorites: List<PokemonCard>
) : RecyclerView.Adapter<FavoritosAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCard: ImageView = itemView.findViewById(R.id.imgPokemonCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_card2, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val card = favorites[position]

        Glide.with(holder.itemView.context)
            .load(card.images.large)
            .placeholder(R.drawable.pokemon_placeholder)
            .error(R.drawable.error)
            .into(holder.imgCard)

        holder.itemView.setOnClickListener {
            showPreviewDialog(holder.itemView.context, card.images.large)
        }
    }

    private fun showPreviewDialog(context: Context, imageUrl: String) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(R.layout.dialog_preview_image)

            val imageView = findViewById<ImageView>(R.id.imgPreview)

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.pokemon_placeholder)
                .error(R.drawable.error)
                .into(imageView)

            imageView.setOnClickListener { dismiss() }

            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundDrawable(ColorDrawable(Color.MAGENTA))
            }
        }

        dialog.show()
    }

    override fun getItemCount(): Int = favorites.size

    fun updateList(newList: List<PokemonCard>) {
        favorites = newList
        notifyDataSetChanged()
    }
}
