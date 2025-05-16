package com.example.pokeapp.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pokeapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FragmentPokeia : Fragment() {

    private lateinit var preguntaInput: TextInputEditText
    private lateinit var enviarButton: MaterialButton
    private lateinit var respuestaTextView: MaterialTextView
    private lateinit var cardRespuesta: MaterialCardView

    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val API_URL = "https://openrouter.ai/api/v1/chat/completions"
    private val API_KEY = "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"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pokeia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupAnimations()
        setupButton()
    }

    private fun initViews(view: View) {
        preguntaInput = view.findViewById(R.id.preguntaInput)
        enviarButton = view.findViewById(R.id.enviarButton)
        respuestaTextView = view.findViewById(R.id.respuestaTextView)
        cardRespuesta = view.findViewById(R.id.cardRespuesta)
    }

    private fun setupAnimations() {
        cardRespuesta.visibility = View.GONE
    }

    private fun setupButton() {
        val bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        val slideUpAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        enviarButton.setOnClickListener {
            it.startAnimation(bounceAnim)

            if (preguntaInput.text?.isNotBlank() == true) {
                hideKeyboard()
                sendRequest(preguntaInput.text.toString())
            } else {
                showError("Por favor escribe una pregunta primero")
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun sendRequest(pregunta: String) {
        val jsonBody = createJsonBody(pregunta)
        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(jsonBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showError("Error de conexión: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                handleApiResponse(response)
            }
        })
    }

    private fun createJsonBody(pregunta: String): RequestBody {
        return JSONObject().apply {
            put("model", "google/gemma-3-27b-it")
            put("temperature", 0.7)
            put("max_tokens", 500)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", pregunta)
                })
            })
        }.toString().toRequestBody(JSON_MEDIA_TYPE)
    }

    private fun handleApiResponse(response: Response) {
        if (!response.isSuccessful) {
            showError("Error del servidor: ${response.code}")
            return
        }

        response.body?.string()?.let { responseBody ->
            try {
                val jsonResponse = JSONObject(responseBody)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                updateUIWithResponse(content)
            } catch (e: Exception) {
                showError("Error procesando la respuesta: ${e.message}")
            }
        } ?: showError("Respuesta vacía del servidor")
    }

    private fun updateUIWithResponse(content: String) {
        Handler(Looper.getMainLooper()).post {
            cardRespuesta.visibility = View.VISIBLE
            cardRespuesta.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up))
            respuestaTextView.text = content.trim()
        }
    }

    private fun showError(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
}