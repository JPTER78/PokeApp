package com.example.pokeapp.fragments

import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pokeapp.PokemonCard
import com.example.pokeapp.adapters.PokemonCardAdapter
import com.example.pokeapp.databinding.FragmentExplorarBinding
import com.example.pokeapp.decorations.GridSpacingItemDecoration
import com.example.pokeapp.viewmodels.PokemonCardsViewModel

class FragmentExplorar : Fragment() {
    private var _binding: FragmentExplorarBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PokemonCardAdapter
    private val viewModel: PokemonCardsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExplorarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupSearch()

        binding.btnFiltros.setOnClickListener {
            showTypeFilterDialog()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCartas.layoutManager = GridLayoutManager(requireContext(), 3)

        binding.recyclerViewCartas.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 3,
                spacing = 6.dpToPx(),
                includeEdge = true
            )
        )

        adapter = PokemonCardAdapter(emptyList()) { card ->
            showCardDetails(card)
        }
        binding.recyclerViewCartas.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.pokemonCards.observe(viewLifecycleOwner) { cards ->
            adapter.updateList(cards)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterCards(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showTypeFilterDialog() {
        val types = arrayOf("Fire", "Water", "Grass", "Lightning", "Psychic", "Fighting", "Darkness", "Metal", "Fairy", "Dragon", "Colorless")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona un tipo")
        builder.setItems(types) { _, which ->
            val selectedType = types[which]
            viewModel.applyTypeFilter(selectedType)
        }
        builder.setNegativeButton("Limpiar filtros") { _, _ ->
            viewModel.clearFilters()
        }
        builder.setNeutralButton("Cancelar", null)
        builder.show()
    }

    private fun showCardDetails(card: PokemonCard) {
        Toast.makeText(requireContext(), "Seleccionado: ${card.name}", Toast.LENGTH_SHORT).show()
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
