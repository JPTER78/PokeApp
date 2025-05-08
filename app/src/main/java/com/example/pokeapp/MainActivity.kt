package com.example.pokeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pokeapp.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import okhttp3.*
import android.util.Log
import android.widget.Toast
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCrear.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))

        }

        binding.btnLogin.setOnClickListener {
            val json = JSONObject().apply {
                put("email", binding.etEmail.text)
                put("contrasena", binding.etContrasena.text)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("http://10.152.94.83:8080/pokeapp/usuarios/login")
                .post(requestBody)
                .build()

            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()

                    if (response.isSuccessful) {
                        Log.d("API", "Login exitoso: $responseData")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Login correcto", Toast.LENGTH_SHORT).show()

                            // GUARDAR DATOS //

                            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("email", "")
                                putString("email", binding.etEmail.text.toString())
                                apply()
                            }

                            // IR A OTRO SITIO //

                            startActivity(Intent(this@MainActivity, MenuDentroActivity::class.java))

                        }
                    } else {
                        Log.e("API", "Error en login: ${response.code} - $responseData")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

    }

}