package com.example.pokeapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pokeapp.databinding.ActivityRegisterBinding
import okhttp3.OkHttpClient
import org.json.JSONObject
import okhttp3.*
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val client = OkHttpClient()


        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCrear.setOnClickListener {

            val json = JSONObject().apply {
                put("email", binding.etEmailRegister.text)
                put("nombre", binding.etNombreRegister.text)
                put("contrasena", binding.etContrasenaRegister.text)
                put("apellidos", "nada")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder().url("http://10.152.94.83:8080/pokeapp/usuarios").post(requestBody).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {

                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        Log.d("API", "Usuario creado: $responseData")
                    } else {
                        Log.e("API", "Error en la respuesta: ${response.code}")
                    }

                }

            })

        }

        binding.btnIrLogin.setOnClickListener {

            startActivity(Intent(this, MainActivity::class.java))

        }

    }
}