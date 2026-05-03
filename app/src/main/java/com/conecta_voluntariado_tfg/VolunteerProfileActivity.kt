package com.conecta_voluntariado_tfg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.conecta_voluntariado_tfg.databinding.ActivityVolunteerProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VolunteerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        disableEditText()
        loadVolunteerProfile()

        binding.btnSaveProfileVolunteer.setOnClickListener {
            if (!isEditing) {
                enableEditText()
                binding.btnSaveProfileVolunteer.text = "Guardar cambios"
                isEditing = true
            } else {
                saveVolunteerProfile()
            }
        }

        binding.btnSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun loadVolunteerProfile() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showSnack("No hay sesión iniciada")
            finish()
            return
        }

        db.collection("volunteers").document(uid).get()
            .addOnSuccessListener { volunteersDocument ->
                if (volunteersDocument.exists()) {
                    binding.etNameVolunteer.setText(volunteersDocument.getString("first_name") ?: "")
                    binding.etLastNameVolunteer.setText(volunteersDocument.getString("last_name") ?: "")
                    binding.etEmailVolunteer.setText(volunteersDocument.getString("volunteer_email") ?: "")

                } else {
                    showSnack("No se encontraron los datos del voluntario")
                }
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar el perfil: ${e.message}")
            }
    }

    private fun disableEditText() {
        binding.etNameVolunteer.isEnabled = false
        binding.etLastNameVolunteer.isEnabled = false
        binding.etEmailVolunteer.isEnabled = false
    }

    private fun enableEditText() {
        binding.etNameVolunteer.isEnabled = true
        binding.etLastNameVolunteer.isEnabled = true
        binding.etEmailVolunteer.isEnabled = true
    }

    private fun saveVolunteerProfile() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showSnack("No hay sesión iniciada")
            return
        }

        val volunteerName = binding.etNameVolunteer.text.toString().trim()
        val volunteerLastName = binding.etLastNameVolunteer.text.toString().trim()
        val volunteerEmail = binding.etEmailVolunteer.text.toString().trim()

        if (volunteerName.isEmpty() || volunteerLastName.isEmpty() || volunteerEmail.isEmpty()) {
            showSnack("Faltan datos por rellenar")
            return
        }

        val updatedVolunteers = hashMapOf(
            "first_name" to volunteerName,
            "last_name" to volunteerLastName,
            "volunteer_email" to volunteerEmail,
        )

        db.collection("volunteers").document(uid).update(updatedVolunteers as Map<String, Any>)
            .addOnSuccessListener {
                showSnack("El perfil se ha actualizado correctamente")
                disableEditText()
                binding.btnSaveProfileVolunteer.text = "Editar perfil"
                isEditing = false
            }
            .addOnFailureListener { e ->
                showSnack("Error al guardar los cambios: ${e.message}")
            }
    }

    private fun signOut() {
        auth.signOut()
        showSnack("Has cerrado tu sesión ¡Nos vemos pronto!")

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}