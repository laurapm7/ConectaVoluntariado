package com.conecta_voluntariado_tfg

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.conecta_voluntariado_tfg.databinding.ActivityRegistrationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val birthDateCalendar = Calendar.getInstance()
    private var pickedDate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.entityRadio.id -> {
                    binding.entityContainer.visibility = View.VISIBLE
                    binding.volunteerContainer.visibility = View.GONE
                }

                binding.volunteerRadio.id -> {
                    binding.volunteerContainer.visibility = View.VISIBLE
                    binding.entityContainer.visibility = View.GONE
                }
            }
        }

        binding.btnSignUp.setOnClickListener {
            binding.btnSignUp.isEnabled = false

            if (binding.entityRadio.isChecked) {
                registerEntity()
            } else {
                registerVolunteer()
            }
        }

        binding.btnPickBirthDate.setOnClickListener {
            openBirthDatePicker()
        }
    }

    private fun openBirthDatePicker() {
        val day = birthDateCalendar.get(Calendar.DAY_OF_MONTH)
        val month = birthDateCalendar.get(Calendar.MONTH)
        val year = birthDateCalendar.get(Calendar.YEAR)

        DatePickerDialog (this, { _, selectedYear, selectedMonth, selectedDay  ->
            birthDateCalendar.set(Calendar.YEAR,selectedYear)
            birthDateCalendar.set(Calendar.MONTH,selectedMonth)
            birthDateCalendar.set(Calendar.DAY_OF_MONTH,selectedDay)
            birthDateCalendar.set(Calendar.HOUR,0)
            birthDateCalendar.set(Calendar.MINUTE,0)
            birthDateCalendar.set(Calendar.SECOND,0)
            birthDateCalendar.set(Calendar.MILLISECOND,0)

            pickedDate = true

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.tvBirthDateSelected.text = dateFormat.format(birthDateCalendar.time)
        }, year, month,day).show()
    }

    private fun registerEntity() {
        val entityName = binding.etNameEntity.text.toString().trim()
        val entityPhone = binding.etPhoneNumber.text.toString().trim()
        val cif = binding.etCif.text.toString().trim()
        val entityEmail = binding.etUserEntity.text.toString().trim()
        val entityPass = binding.etPassEntity.text.toString().trim()

        if (entityName.isEmpty() || cif.isEmpty() || entityPhone.isEmpty() || entityEmail.isEmpty() || entityPass.isEmpty()) {
            showSnack("Faltan datos por introducir")
            binding.btnSignUp.isEnabled = true
            return
        }

        if (!entityEmail.contains("@")) {
            showSnack("El email debe contener @")
            binding.btnSignUp.isEnabled = true
            return
        }

        if (entityPass.length < 6) {
            showSnack("La contraseña debe tener al menos 6 caracteres")
            binding.btnSignUp.isEnabled = true
            return
        }

        auth.createUserWithEmailAndPassword(entityEmail, entityPass)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid

                if (uid == null) {
                    showSnack("Error al crear el usuario")
                    binding.btnSignUp.isEnabled = true
                    return@addOnSuccessListener
                }

                val userDoc = hashMapOf(
                    "role" to "entity",
                    "email" to entityEmail,
                    "created_at" to FieldValue.serverTimestamp()
                )

                val entityDoc = hashMapOf(
                    "entity_name" to entityName,
                    "entity_description" to "",
                    "cif" to cif,
                    "entity_phone" to entityPhone,
                    "entity_email" to entityEmail,
                    "created_at" to FieldValue.serverTimestamp()
                )

                db.collection("users").document(uid).set(userDoc)
                    .addOnSuccessListener {
                        db.collection("entities").document(uid).set(entityDoc)
                            .addOnSuccessListener {
                                showSnack("Registro de la entidad completado")
                                startActivity(Intent(this, EntityHomeActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                showSnack("Error al guardar la entidad: ${e.message}")
                                binding.btnSignUp.isEnabled = true
                            }
                    }
                    .addOnFailureListener { e ->
                        showSnack("Error al guardar el usuario: ${e.message}")
                        binding.btnSignUp.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                showSnack("Error al registrarse: ${e.message}")
                binding.btnSignUp.isEnabled = true
            }
    }

    private fun registerVolunteer() {
        val firstName = binding.etNameVolunteer.text.toString().trim()
        val lastName = binding.etSurnameVolunteer.text.toString().trim()
        val birthDate = binding.tvBirthDateSelected.text.toString().trim()
        val volunteerEmail = binding.etUserVolunteer.text.toString().trim()
        val volunteerPass = binding.etPassVolunteer.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || volunteerEmail.isEmpty() || volunteerPass.isEmpty()) {
            showSnack("Faltan datos por introducir")
            binding.btnSignUp.isEnabled = true
            return
        }

        if(!pickedDate){
            showSnack("Selecciona tu fecha de nacimiento")
            binding.btnSignUp.isEnabled = true
            return
            }

        if (!volunteerEmail.contains("@")) {
            showSnack("El email debe contener @")
            binding.btnSignUp.isEnabled = true
            return
        }

        if (volunteerPass.length < 6) {
            showSnack("La contraseña debe tener al menos 6 caracteres")
            binding.btnSignUp.isEnabled = true
            return
        }

        auth.createUserWithEmailAndPassword(volunteerEmail, volunteerPass)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid

                if (uid == null) {
                    showSnack("Error al crear el usuario")
                    binding.btnSignUp.isEnabled = true
                    return@addOnSuccessListener
                }

                val userDoc = hashMapOf(
                    "role" to "volunteer",
                    "email" to volunteerEmail,
                    "created_at" to FieldValue.serverTimestamp()
                )

                val volunteerDoc = hashMapOf(
                    "first_name" to firstName,
                    "last_name" to lastName,
                    "birth_date" to Timestamp(birthDateCalendar.time),
                    "volunteer_email" to volunteerEmail,
                    "created_at" to FieldValue.serverTimestamp()
                )

                db.collection("users").document(uid).set(userDoc)
                    .addOnSuccessListener {
                        db.collection("volunteers").document(uid).set(volunteerDoc)
                            .addOnSuccessListener {
                                showSnack("Registro del voluntario completado")
                                startActivity(Intent(this, VolunteerHomeActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                showSnack("Error al guardar el voluntario: ${e.message}")
                                binding.btnSignUp.isEnabled = true
                            }
                    }
                    .addOnFailureListener { e ->
                        showSnack("Error al guardar el usuario: ${e.message}")
                        binding.btnSignUp.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                showSnack("Error al registrarse: ${e.message}")
                binding.btnSignUp.isEnabled = true
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}