package com.conecta_voluntariado_tfg

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.conecta_voluntariado_tfg.databinding.ActivityVolunteerProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

class VolunteerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isEditing = false
    private val birthDateCalendar = Calendar.getInstance()
    private var pickedDate = false

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

        binding.btnDeleteUser.setOnClickListener {
            deleteAccountDialog()
        }

        binding.tvBirthDateSelected.setOnClickListener {
            if (isEditing){
                openBirthDatePicker()
            }
        }
    }

    private fun openBirthDatePicker(){
        val year = birthDateCalendar.get(Calendar.YEAR)
        val month = birthDateCalendar.get(Calendar.MONTH)
        val day = birthDateCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this,{_, selectedYear, selectedMonth, selectedDay ->
            birthDateCalendar.set(Calendar.YEAR, selectedYear)
            birthDateCalendar.set(Calendar.MONTH, selectedMonth)
            birthDateCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)

            pickedDate = true

            val dateFormat = SimpleDateFormat("dd/MM/yyy", Locale.getDefault())
            binding.tvBirthDateSelected.text = dateFormat.format(birthDateCalendar.time)
        }, year, month, day).show()


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
                    binding.etNameVolunteer.setText(
                        volunteersDocument.getString("first_name") ?: ""
                    )
                    binding.etLastNameVolunteer.setText(
                        volunteersDocument.getString("last_name") ?: ""
                    )
                    val birthDateTimestamp = volunteersDocument.getTimestamp("birth_date")

                    if (birthDateTimestamp != null) {
                        birthDateCalendar.time = birthDateTimestamp.toDate()
                        pickedDate = true

                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        binding.tvBirthDateSelected.text = dateFormat.format(birthDateCalendar.time)
                    } else {
                        binding.tvBirthDateSelected.text = "No seleccionada"
                        pickedDate = false
                    }
                    binding.etEmailVolunteer.setText(
                        volunteersDocument.getString("volunteer_email") ?: ""
                    )

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
        binding.tvBirthDateSelected.isEnabled = false
        binding.etEmailVolunteer.isEnabled = false
    }

    private fun enableEditText() {
        binding.etNameVolunteer.isEnabled = true
        binding.etLastNameVolunteer.isEnabled = true
        binding.tvBirthDateSelected.isEnabled = true
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

        if (!pickedDate){
            showSnack("Selecciona tu fecha de nacimiento")
            return
        }

        val updatedVolunteers = hashMapOf(
            "first_name" to volunteerName,
            "last_name" to volunteerLastName,
            "birth_date" to Timestamp(birthDateCalendar.time),
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

    private fun deleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar cuenta")
            .setMessage("¿Estás seguro de que quieres eliminar tu cuenta?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteVolunteerAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteVolunteerAccount() {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid == null) {
            showSnack("Debes iniciar sesión para continuar")
            return
        }

        db.collection("volunteer_registrations")
            .whereEqualTo("volunteer_id", uid)
            .get()
            .addOnSuccessListener { volunteeringsResult ->

                for (doc in volunteeringsResult.documents) {
                    db.collection("volunteer_registrations").document(doc.id).delete()
                }

                db.collection("volunteers").document(uid).delete()
                    .addOnSuccessListener {
                        db.collection("users").document(uid).delete()
                            .addOnSuccessListener {
                                user.delete()
                                    .addOnSuccessListener {
                                        val intent = Intent(this, LoginActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        showSnack("Error al eliminar la cuenta: ${e.message}")
                                    }
                            }
                    }
            }
            .addOnFailureListener { e ->
                showSnack("Error al eliminar los datos: ${e.message}")
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}