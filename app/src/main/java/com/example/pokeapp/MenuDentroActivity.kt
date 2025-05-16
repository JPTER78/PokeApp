package com.example.pokeapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.pokeapp.databinding.ActivityMainBinding
import com.example.pokeapp.databinding.ActivityMenuDentroBinding
import com.example.pokeapp.fragments.FragmentExplorar
import com.example.pokeapp.fragments.FragmentHome
import com.example.pokeapp.fragments.FragmentPokeia
import com.example.pokeapp.fragments.FragmentTienda

class MenuDentroActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var binding: ActivityMenuDentroBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMenuDentroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentHome())
                .commit()
        }

        binding.ayuda.setOnNavigationItemSelectedListener {

            it.isChecked = true
            when (it.itemId) {

                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentHome()).commit()}
                R.id.navigation_tienda -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentTienda()).commit()}
                R.id.navigation_explorar -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentExplorar()).commit()}
                R.id.navigation_ia -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentPokeia()).commit()}

            }
            false

        }

    }

}