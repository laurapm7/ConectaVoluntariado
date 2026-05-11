package com.conecta_voluntariado_tfg

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.conecta_voluntariado_tfg.databinding.ActivityCreateVolunteeringBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateVolunteeringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateVolunteeringBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val volunteeringTypeIds = ArrayList<String>()
    private val volunteeringTypeNames = ArrayList<String>()
    private var volunteeringId: String? = null
    private var isEditMode = false
    private val calendar = Calendar.getInstance()
    private var pickedDate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateVolunteeringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volunteeringId = intent.getStringExtra("volunteering_id")
        isEditMode = volunteeringId != null

        if (isEditMode) {
            binding.btnSave.text = "Guardar cambios"
        } else {
            binding.btnSave.text = "Publicar"
        }

        loadTypes()

        binding.btnPickDate.setOnClickListener {
            openDatePicker()
        }

        binding.btnSave.setOnClickListener {
            if (isEditMode) {
                updateVolunteering()
            } else {
                saveVolunteering()
            }
        }
    }

    private fun loadTypes() {
        db.collection("volunteering_types")
            .get()
            .addOnSuccessListener { typesResult ->
                volunteeringTypeIds.clear()
                volunteeringTypeNames.clear()
                volunteeringTypeIds.add("")
                volunteeringTypeNames.add("Selecciona un tipo")

                for (doc in typesResult.documents) {
                    val name = doc.getString("type_name")
                    if (name != null) {
                        volunteeringTypeIds.add(doc.id)
                        volunteeringTypeNames.add(name)
                    }
                }

                val adapter = android.widget.ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    volunteeringTypeNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spType.adapter = adapter

                if (isEditMode) {
                    loadVolunteeringData()
                }
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar los tipos: ${e.message}")
            }
    }

    private fun openDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            openTimePicker()
        }, year, month, day).show()
    }

    private fun openTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            pickedDate = true
            updateDateLabel()
        }, hour, minute, true).show()
    }

    private fun updateDateLabel() {
        if (!pickedDate) {
            binding.tvDateSelected.text = "Selecciona fecha y hora"
            return
        }
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.tvDateSelected.text = "Fecha y hora: ${format.format(calendar.time)}"
    }

    private fun loadVolunteeringData() {
        val id = volunteeringId ?: return

        db.collection("volunteerings").document(id).get()
            .addOnSuccessListener { volunteeringDocument ->
                if (volunteeringDocument.exists()) {
                    binding.etTitle.setText(volunteeringDocument.getString("title") ?: "")
                    binding.etDescription.setText(
                        volunteeringDocument.getString("description") ?: ""
                    )
                    binding.etCity.setText(volunteeringDocument.getString("city") ?: "")
                    binding.etAddress.setText(volunteeringDocument.getString("address") ?: "")

                    binding.cbAccessibility.isChecked =
                        volunteeringDocument.getBoolean("accessibility") ?: false

                    binding.cbInvolvesMinors.isChecked =
                        volunteeringDocument.getBoolean("involves_minors") ?: false

                    binding.swActive.isChecked =
                        volunteeringDocument.getString("status") == "active"

                    val date = volunteeringDocument.getTimestamp("date")
                    if (date != null) {
                        calendar.time = date.toDate()
                        pickedDate = true
                        updateDateLabel()
                    }

                    val typeId = volunteeringDocument.getString("volunteering_type_id") ?: ""
                    val position = volunteeringTypeIds.indexOf(typeId)

                    if (position >= 0) {
                        binding.spType.setSelection(position)
                    }
                }
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar el voluntariado: ${e.message}")
            }
    }

    private fun saveVolunteering() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showSnack("No hay sesión iniciada")
            return
        }

        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val pos = binding.spType.selectedItemPosition
        val typeId =
            if (pos >= 0 && pos < volunteeringTypeIds.size) volunteeringTypeIds[pos] else ""
        val accessibility = binding.cbAccessibility.isChecked
        val involvesMinors = binding.cbInvolvesMinors.isChecked
        val status = if (binding.swActive.isChecked) "active" else "finished"

        if (title.isEmpty() || description.isEmpty() || city.isEmpty() || address.isEmpty()) {
            showSnack("Rellena todos los campos")
            return
        }

        if (typeId.isEmpty()) {
            showSnack("Selecciona un tipo de voluntariado")
            return
        }

        if (!pickedDate) {
            showSnack("Selecciona fecha")
            return
        }

        binding.btnSave.isEnabled = false

        val volunteeringData = hashMapOf(
            "title" to title,
            "description" to description,
            "city" to city,
            "address" to address,
            "date" to Timestamp(calendar.time),
            "accessibility" to accessibility,
            "involves_minors" to involvesMinors,
            "status" to status,
            "volunteering_type_id" to typeId,
            "entity_id" to uid,
            "created_at" to FieldValue.serverTimestamp()
        )

        db.collection("volunteerings")
            .add(volunteeringData)
            .addOnSuccessListener {
                showSnack("Voluntariado creado")
                finish()
            }
            .addOnFailureListener { e ->
                showSnack("Error al guardar el voluntariado: ${e.message}")
                binding.btnSave.isEnabled = true
            }
    }

    private fun updateVolunteering() {
        val id = volunteeringId ?: return

        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val pos = binding.spType.selectedItemPosition
        val typeId =
            if (pos >= 0 && pos < volunteeringTypeIds.size) volunteeringTypeIds[pos] else ""

        val accessibility = binding.cbAccessibility.isChecked
        val involvesMinors = binding.cbInvolvesMinors.isChecked
        val status = if (binding.swActive.isChecked) "active" else "finished"

        if (title.isEmpty() || description.isEmpty() || city.isEmpty() || address.isEmpty()) {
            showSnack("Rellena todos los campos")
            return
        }

        if (typeId.isEmpty()) {
            showSnack("Selecciona un tipo de voluntariado")
            return
        }

        if (!pickedDate) {
            showSnack("Selecciona fecha")
            return
        }

        binding.btnSave.isEnabled = false

        val volunteeringData = hashMapOf<String, Any>(
            "title" to title,
            "description" to description,
            "city" to city,
            "address" to address,
            "date" to Timestamp(calendar.time),
            "accessibility" to accessibility,
            "involves_minors" to involvesMinors,
            "status" to status,
            "volunteering_type_id" to typeId
        )

        db.collection("volunteerings").document(id)
            .update(volunteeringData)
            .addOnSuccessListener {
                showSnack("Voluntariado actualizado")
                finish()
            }
            .addOnFailureListener { e ->
                showSnack("Error al actualizar el voluntariado: ${e.message}")
                binding.btnSave.isEnabled = true
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}