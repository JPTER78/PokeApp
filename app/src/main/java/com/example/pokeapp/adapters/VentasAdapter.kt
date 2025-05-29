package com.example.pokeapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokeapp.R
import com.example.pokeapp.modelos.Venta

class VentasAdapter(
    private val ventas: MutableList<Venta>,
    private val onClick: (Venta) -> Unit
) : RecyclerView.Adapter<VentasAdapter.VentaViewHolder>() {

    inner class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCarta: ImageView = itemView.findViewById(R.id.ivCarta)
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = ventas[position]

        holder.apply {
            tvDireccion.text = venta.direccion
            tvFecha.text = venta.fecha
            tvEstado.text = venta.estado

            Glide.with(itemView)
                .load(venta.imageUrl)
                .placeholder(R.drawable.pokemon_placeholder)
                .into(ivCarta)

            itemView.setOnClickListener { onClick(venta) }
        }
    }

    override fun getItemCount(): Int = ventas.size

    fun actualizarEstadoVenta(position: Int, nuevoEstado: String) {
        if (position in 0 until ventas.size) {
            ventas[position].estado = nuevoEstado
            notifyItemChanged(position)
        }
    }

    fun actualizarLista(nuevaLista: List<Venta>) {
        ventas.clear()
        ventas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
