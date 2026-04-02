package com.conecta_voluntariado_tfg

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.conecta_voluntariado_tfg.databinding.ActivityEntityProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EntityProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        disableEditText()
        loadEntityProfile()

        binding.btnSaveProfile.setOnClickListener {
            if (!isEditing) {
                enableEditText()
                binding.btnSaveProfile.text = "Guardar cambios"
                isEditing = true
            } else {
                saveEntityProfile()
            }
        }
    }

    private fun loadEntityProfile() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showSnack("No hay sesión iniciada")
            finish()
            return
        }

        db.collection("entities").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etNameEntity.setText(document.getString("entity_name") ?: "")
                    binding.etPhoneNumber.setText(document.getString("entity_phone") ?: "")
                    binding.etEmailEntity.setText(document.getString("entity_email") ?: "")
                    binding.etDescEntity.setText(document.getString("entity_description") ?: "")
                } else {
                    showSnack("No se encontraron los datos de la entidad")
                }
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar el perfil: ${e.message}")
            }
    }

    private fun disableEditText() {
        binding.etNameEntity.isEnabled = false
        binding.etPhoneNumber.isEnabled = false
        binding.etEmailEntity.isEnabled = false
        binding.etDescEntity.isEnabled = false
    }

    private fun enableEditText() {
        binding.etNameEntity.isEnabled = true
        binding.etPhoneNumber.isEnabled = true
        binding.etEmailEntity.isEnabled = true
        binding.etDescEntity.isEnabled = true
    }

    private fun saveEntityProfile() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showSnack("No hay sesión iniciada")
            return
        }

        val name = binding.etNameEntity.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()
        val email = binding.etEmailEntity.text.toString().trim()
        val description = binding.etDescEntity.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showSnack("Faltan datos por rellenar")
            return
        }

        val updatedEntity = hashMapOf(
            "entity_name" to name,
            "entity_phone" to phone,
            "entity_email" to email,
            "entity_description" to description
        )

        db.collection("entities").document(uid).update(updatedEntity as Map<String, Any>)
            .addOnSuccessListener {
                showSnack("El perfil se ha actualizado correctamente")
                disableEditText()
                binding.btnSaveProfile.text = "Editar perfil"
                isEditing = false
            }
            .addOnFailureListener { e ->
                showSnack("Error al guardar los cambios: ${e.message}")
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}