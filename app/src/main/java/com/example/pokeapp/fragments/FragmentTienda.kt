package com.example.pokeapp.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.pokeapp.R
import com.example.pokeapp.adapters.VentasAdapter
import com.example.pokeapp.modelos.Venta
import com.example.pokeapp.modelos.VentaSubida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class FragmentTienda : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VentasAdapter
    private val ventasList = mutableListOf<Venta>()
    private val ventasOriginales = mutableListOf<Venta>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tienda, container, false)
        recyclerView = view.findViewById(R.id.rvVentas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VentasAdapter(ventasList) { venta ->
            if (venta.estado == "Disponible") {
                mostrarDialogoReserva(venta)
            } else {
                Toast.makeText(requireContext(), "No se puede reservar, este artículo ya está comprado", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.adapter = adapter

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarVentasPorCodigo(newText)
                return true
            }
        })

        fetchVentas()

        val btnCrearVenta = view.findViewById<Button>(R.id.btnSubirProducto)
        btnCrearVenta.setOnClickListener {
            mostrarDialogoCrearVenta()
        }

        return view
    }

    private fun filtrarVentasPorCodigo(query: String?) {
        val texto = query?.trim()?.lowercase() ?: ""
        val listaFiltrada = if (texto.isEmpty()) {
            ventasOriginales
        } else {
            ventasOriginales.filter {
                it.idCarta.lowercase().contains(texto)
            }
        }

        ventasList.clear()
        ventasList.addAll(listaFiltrada)
        adapter.notifyDataSetChanged()
    }

    private fun mostrarDialogoReserva(venta: Venta) {
        AlertDialog.Builder(requireContext())
            .setTitle("Reservar carta")
            .setMessage("¿Deseas reservar esta carta?")
            .setPositiveButton("Reservar") { _, _ -> reservarVenta(venta) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun reservarVenta(venta: Venta) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val emailComprador = requireContext()
                    .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getString("email", "") ?: ""

                val url = URL("http://10.152.94.33:8080/ventas/${venta.idCarta}/reservar?emailComprador=$emailComprador")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PUT"
                conn.setRequestProperty("Content-Type", "application/json")

                val responseCode = conn.responseCode

                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Reserva realizada con éxito", Toast.LENGTH_SHORT).show()
                            fetchVentas()
                        }
                    }
                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "La carta no existe o ya fue reservada", Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {
                        val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "Sin detalles"
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error al reservar: ${errorBody.take(100)}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error de conexión: ${e.message ?: "Verifica tu red"}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fetchVentas() {
        val queue = Volley.newRequestQueue(requireContext())
        val urlVentas = "http://10.152.94.33:8080/ventas"

        val ventasRequest = JsonArrayRequest(
            Request.Method.GET, urlVentas, null,
            { jsonArray ->
                ventasList.clear()
                ventasOriginales.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val venta = Venta(
                        idCarta = obj.getString("idCarta"),
                        direccion = obj.getString("direccion"),
                        fecha = obj.getString("fecha").substring(0, 10),
                        estado = obj.getString("estado"),
                        imageUrl = null,
                        emailVendedor = obj.optString("emailVendedor", null),
                        emailComprador = obj.optString("emailComprador", null)
                    )
                    ventasList.add(venta)
                    ventasOriginales.add(venta)
                    fetchImageFor(venta, queue)
                }
                adapter.notifyDataSetChanged()
            },
            { error ->
                Toast.makeText(requireContext(), "Error al obtener ventas", Toast.LENGTH_SHORT).show()
                Log.e("FragmentTienda", error.toString())
            }
        )
        queue.add(ventasRequest)
    }

    private fun fetchImageFor(venta: Venta, queue: com.android.volley.RequestQueue) {
        val urlCardApi = "https://api.pokemontcg.io/v2/cards/${venta.idCarta}"
        val cardRequest = JsonObjectRequest(
            Request.Method.GET, urlCardApi, null,
            { response ->
                try {
                    val images = response.getJSONObject("data").getJSONObject("images")
                    venta.imageUrl = images.getString("large")
                    adapter.notifyItemChanged(ventasList.indexOf(venta))
                } catch (e: Exception) {
                    Log.e("FragmentTienda", "Parse card JSON: ${e.message}")
                }
            },
            { error ->
                Log.e("FragmentTienda", "Error cargando carta ${venta.idCarta}: $error")
            }
        )
        queue.add(cardRequest)
    }

    private fun mostrarDialogoCrearVenta() {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_crear_venta, null)
        builder.setView(view)

        val spinnerCodigo = view.findViewById<Spinner>(R.id.spinnerCodigo)
        val spinnerIdioma = view.findViewById<Spinner>(R.id.spinnerIdioma)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)
        val tvFecha = view.findViewById<TextView>(R.id.tvFecha)

        val calendar = Calendar.getInstance()
        tvFecha.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    tvFecha.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val idiomas = listOf("Español", "Inglés", "Japonés")
        val adapterIdioma = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, idiomas)
        adapterIdioma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerIdioma.adapter = adapterIdioma

        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val emailUsuario = sharedPref.getString("email", "") ?: ""

        lifecycleScope.launch {
            val favoritos = withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://10.152.94.33:8080/favoritos")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val jsonArray = JSONArray(response)
                    val codigos = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val fav = jsonArray.getJSONObject(i)
                        if (fav.getString("emailUsuario") == emailUsuario) {
                            codigos.add(fav.getString("idCarta"))
                        }
                    }
                    codigos
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val adapterCodigo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, favoritos)
            adapterCodigo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCodigo.adapter = adapterCodigo
        }

        builder.setPositiveButton("Crear") { _, _ ->
            val venta = VentaSubida(
                emailVendedor = emailUsuario,
                idCarta = spinnerCodigo.selectedItem.toString(),
                idioma = spinnerIdioma.selectedItem.toString(),
                direccion = etDireccion.text.toString(),
                fecha = tvFecha.text.toString()
            )

            subirVenta(venta)
        }

        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    private fun subirVenta(venta: VentaSubida) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://10.152.94.33:8080/ventas")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonVenta = JSONObject().apply {
                    put("emailVendedor", venta.emailVendedor)
                    put("idCarta", venta.idCarta)
                    put("emailComprador", venta.emailComprador)
                    put("estado", venta.estado)
                    put("idioma", venta.idioma)
                    put("direccion", venta.direccion)
                    put("fecha", venta.fecha + "T00:00:00")
                }

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(jsonVenta.toString())
                writer.flush()
                writer.close()

                val responseCode = conn.responseCode
                val responseMessage = conn.responseMessage

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Venta creada con éxito", Toast.LENGTH_SHORT).show()
                        fetchVentas()
                    }
                } else {
                    val errorBody = conn.errorStream?.bufferedReader()?.readText()
                    withContext(Dispatchers.Main) {
                        Log.e("CrearVenta", "Error $responseCode: $responseMessage\n$errorBody")
                        Toast.makeText(requireContext(), "Error al crear la venta", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("CrearVenta", "Excepción: ${e.message}", e)
                }
            }
        }
    }
}
