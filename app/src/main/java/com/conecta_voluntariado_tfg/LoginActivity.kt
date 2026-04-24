package com.conecta_voluntariado_tfg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.conecta_voluntariado_tfg.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            goToHomeByRole(uid)
        }

        binding.btnLogIn.setOnClickListener {
            login()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.etUser.text.toString().trim()
        val pass = binding.etPass.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            showSnack("Faltan datos por introducir")
            return
        }

        if (!email.contains("@")) {
            showSnack("Introduce un email válido")
            return
        }

        binding.btnLogIn.isEnabled = false

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    goToHomeByRole(uid)
                } else {
                    showSnack("Error al iniciar sesión")
                    binding.btnLogIn.isEnabled = true
                }
            }
            .addOnFailureListener { e ->
                showSnack("Correo electrónico o contraseña incorrectos: ${e.message}")
                binding.btnLogIn.isEnabled = true
            }
    }

    private fun goToHomeByRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (!userDoc.exists()) {
                    showSnack("No existe el usuario en la base de datos")
                    auth.signOut()
                    binding.btnLogIn.isEnabled = true
                    return@addOnSuccessListener
                }

                val role = userDoc.getString("role")

                if (role == "entity") {
                    startActivity(Intent(this, EntityHomeActivity::class.java))
                    finish()
                } else if (role == "volunteer") {
                    startActivity(Intent(this, VolunteerHomeActivity::class.java))
                    finish()
                } else {
                    showSnack("Rol no válido")
                    auth.signOut()
                    binding.btnLogIn.isEnabled = true
                }
            }
            .addOnFailureListener {
                showSnack("Error al obtener el rol del usuario")
                binding.btnLogIn.isEnabled = true
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}