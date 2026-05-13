package com.conecta_voluntariado_tfg

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.conecta_voluntariado_tfg.databinding.ActivitySearchVolunteeringBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class SearchVolunteeringActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchVolunteeringBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val filteredVolunteeringsList = ArrayList<Volunteering>()
    private val allVolunteeringsList = ArrayList<Volunteering>()
    private val volunteeringTypeMap = HashMap<String, String>()

    private lateinit var searchVolunteeringsAdapter: SearchVolunteeringsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchVolunteeringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchVolunteeringsAdapter = SearchVolunteeringsAdapter(
            filteredVolunteeringsList,
            volunteeringTypeMap,
            onSignUpClick = { volunteering ->
                signUpToVolunteering(volunteering)
            }
        )

        binding.rvVolunteerings.layoutManager = LinearLayoutManager(this)
        binding.rvVolunteerings.adapter = searchVolunteeringsAdapter

        binding.btnHomeVolunteer.setOnClickListener {
            startActivity(Intent(this, VolunteerHomeActivity::class.java))
            finish()
        }

        binding.btnSearchVolunteer.setOnClickListener {
            loadVolunteeringTypes()
        }

        binding.btnProfileVolunteer.setOnClickListener {
            startActivity(Intent(this, VolunteerProfileActivity::class.java))
        }

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }

        binding.btnClearFilters.setOnClickListener {
            clearFilters()
        }

        loadVolunteeringTypes()
    }

    private fun loadVolunteeringTypes() {
        db.collection("volunteering_types")
            .get()
            .addOnSuccessListener { typesResult ->
                volunteeringTypeMap.clear()

                val typeNames = ArrayList<String>()
                typeNames.add("Todos")

                for (doc in typesResult.documents) {
                    val typeName = doc.getString("type_name")
                    if (typeName != null) {
                        volunteeringTypeMap[doc.id] = typeName
                        typeNames.add(typeName)
                    }
                }

                val typeAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    typeNames
                )
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spTypeFilter.adapter = typeAdapter

                loadVolunteerings()
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar los tipos: ${e.message}")
            }
    }

    private fun loadVolunteerings() {
        db.collection("volunteerings")
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { volunteeringsResult ->
                filteredVolunteeringsList.clear()
                allVolunteeringsList.clear()

                val cities = ArrayList<String>()
                cities.add("Todas")

                val currentDate = Date()
                for (doc in volunteeringsResult.documents) {
                    val volunteering = doc.toObject(Volunteering::class.java)

                    if (volunteering != null) {
                        val volunteeringDate = volunteering.date?.toDate()

                        if (volunteeringDate != null && volunteeringDate.after(currentDate)) {

                            volunteering.id = doc.id

                            filteredVolunteeringsList.add(volunteering)
                            allVolunteeringsList.add(volunteering)

                            if (!cities.contains(volunteering.city)) {
                                cities.add(volunteering.city)
                            }
                        }
                    }
                }

                val cityAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    cities
                )
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spCityFilter.adapter = cityAdapter

                searchVolunteeringsAdapter.notifyDataSetChanged()

                binding.textEmpty.visibility =
                    if (filteredVolunteeringsList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar los voluntariados: ${e.message}")
            }
    }

    private fun applyFilters() {
        val selectedCity = binding.spCityFilter.selectedItem.toString()
        val selectedType = binding.spTypeFilter.selectedItem.toString()
        val accessibleChecked = binding.cbAccessible.isChecked
        val minorsChecked = binding.cbMinors.isChecked

        filteredVolunteeringsList.clear()

        for (volunteering in allVolunteeringsList) {
            val typeName = volunteeringTypeMap[volunteering.volunteering_type_id] ?: "Sin tipo"

            val matchesCity = selectedCity == "Todas" || volunteering.city == selectedCity
            val matchesType = selectedType == "Todos" || typeName == selectedType
            val matchesAccessibility = !accessibleChecked || volunteering.accessibility
            val matchesMinors = !minorsChecked || volunteering.involves_minors

            if (matchesCity && matchesType && matchesAccessibility && matchesMinors) {
                filteredVolunteeringsList.add(volunteering)
            }
        }

        searchVolunteeringsAdapter.notifyDataSetChanged()

        binding.textEmpty.visibility =
            if (filteredVolunteeringsList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun clearFilters() {
        binding.cbAccessible.isChecked = false
        binding.cbMinors.isChecked = false

        if (binding.spCityFilter.adapter != null) {
            binding.spCityFilter.setSelection(0)
        }

        if (binding.spTypeFilter.adapter != null) {
            binding.spTypeFilter.setSelection(0)
        }

        filteredVolunteeringsList.clear()
        filteredVolunteeringsList.addAll(allVolunteeringsList)

        searchVolunteeringsAdapter.notifyDataSetChanged()

        binding.textEmpty.visibility =
            if (filteredVolunteeringsList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun signUpToVolunteering(volunteering: Volunteering) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showSnack("No hay sesión iniciada")
            return
        }

        db.collection("volunteer_registrations")
            .whereEqualTo("volunteer_id", uid)
            .whereEqualTo("volunteering_id", volunteering.id)
            .get()
            .addOnSuccessListener { registrationResult ->
                if (!registrationResult.isEmpty) {
                    showSnack("Ya estás inscrito en este voluntariado")
                    return@addOnSuccessListener
                }

                val registrationData = hashMapOf(
                    "volunteer_id" to uid,
                    "volunteering_id" to volunteering.id,
                    "registered_at" to FieldValue.serverTimestamp()
                )

                db.collection("volunteer_registrations")
                    .add(registrationData)
                    .addOnSuccessListener {
                        showSnack("Inscripción realizada correctamente")
                    }
                    .addOnFailureListener { e ->
                        showSnack("Error al inscribirse: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showSnack("Error al comprobar la inscripción: ${e.message}")
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}