package com.conecta_voluntariado_tfg

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.conecta_voluntariado_tfg.databinding.ActivityVolunteerHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class VolunteerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerHomeBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val achievementList = ArrayList<Achievement>()
    private val myVolunteeringsList = ArrayList<Volunteering>()
    private val volunteeringTypeMap = HashMap<String, String>()

    private lateinit var achievementAdapter: AchievementAdapter
    private lateinit var myVolunteeringsAdapter: MyVolunteeringsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        achievementAdapter = AchievementAdapter(achievementList)
        myVolunteeringsAdapter = MyVolunteeringsAdapter(
            myVolunteeringsList,
            volunteeringTypeMap,
            onCancelRegistrationClick = { volunteering ->
                cancelRegistration(volunteering)
            })

        binding.rvAchievements.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAchievements.adapter = achievementAdapter

        binding.rvVolunteerings.layoutManager = LinearLayoutManager(this)
        binding.rvVolunteerings.adapter = myVolunteeringsAdapter

        binding.btnHomeVolunteer.setOnClickListener {
            loadVolunteeringTypes()
        }

        binding.btnSearchVolunteer.setOnClickListener {
            startActivity(Intent(this, SearchVolunteeringActivity::class.java))
        }

        binding.btnProfileVolunteer.setOnClickListener {
            startActivity(Intent(this, VolunteerProfileActivity::class.java))
        }

        loadVolunteeringTypes()
    }

    override fun onResume() {
        super.onResume()
        loadVolunteeringTypes()
    }

    private fun loadVolunteeringTypes() {
        db.collection("volunteering_types")
            .get()
            .addOnSuccessListener { typesResult ->
                volunteeringTypeMap.clear()

                for (doc in typesResult.documents) {
                    val typeName = doc.getString("type_name")
                    if (typeName != null) {
                        volunteeringTypeMap[doc.id] = typeName
                    }
                }

                loadMyVolunteerings()
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar los tipos de voluntariado: ${e.message}")
            }
    }

    private fun loadMyVolunteerings() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showSnack("No hay sesión iniciada")
            return
        }

        db.collection("volunteer_registrations")
            .whereEqualTo("volunteer_id", uid)
            .get()
            .addOnSuccessListener { registrationResult ->

                val volunteeringIds = registrationResult.documents.mapNotNull { document ->
                    document.getString("volunteering_id")
                }

                if (volunteeringIds.isEmpty()) {
                    myVolunteeringsList.clear()
                    myVolunteeringsAdapter.notifyDataSetChanged()
                    binding.textEmpty.visibility = View.VISIBLE

                    val completedCount = updateCompletedVolunteeringsCount()
                    loadUnlockedAchievements(completedCount)
                    return@addOnSuccessListener
                }

                db.collection("volunteerings")
                    .get()
                    .addOnSuccessListener { volunteeringResult ->
                        myVolunteeringsList.clear()

                        for (doc in volunteeringResult.documents) {
                            if (doc.id in volunteeringIds) {
                                val volunteering = doc.toObject(Volunteering::class.java)
                                if (volunteering != null) {
                                    volunteering.id = doc.id
                                    myVolunteeringsList.add(volunteering)
                                }
                            }
                        }

                        myVolunteeringsAdapter.notifyDataSetChanged()

                        binding.textEmpty.visibility =
                            if (myVolunteeringsList.isEmpty()) View.VISIBLE else View.GONE

                        val completedCount = updateCompletedVolunteeringsCount()
                        loadUnlockedAchievements(completedCount)
                    }
                    .addOnFailureListener { e ->
                        showSnack("Error al cargar los voluntariados: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar las inscripciones: ${e.message}")
            }
    }

    private fun cancelRegistration(volunteering: Volunteering) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showSnack("Debes iniciar sesión para realizar esta acción")
            return
        }

        db.collection("volunteer_registrations")
            .whereEqualTo("volunteer_id", uid)
            .whereEqualTo("volunteering_id", volunteering.id)
            .get()
            .addOnSuccessListener { registrationResult ->
                if (registrationResult.isEmpty) {
                    showSnack("No estás inscrito en este voluntariado")
                    return@addOnSuccessListener
                }

                val registrationDoc = registrationResult.documents.first()

                db.collection("volunteer_registrations")
                    .document(registrationDoc.id)
                    .delete()
                    .addOnSuccessListener {
                        showSnack("Has cancelado la inscripción")
                        loadMyVolunteerings()
                    }
                    .addOnFailureListener { e ->
                        showSnack("Error al cancelar la inscripción: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showSnack("Error al buscar la inscripción: ${e.message}")
            }
    }

    private fun updateCompletedVolunteeringsCount(): Int {
        val currentDate = Date()
        var completedCount = 0

        for (volunteering in myVolunteeringsList) {
            val volunteeringDate = volunteering.date?.toDate()
            if (volunteeringDate != null && volunteeringDate.before(currentDate)) {
                completedCount++
            }
        }

        binding.tvCompletedVolunteerings.text = "Voluntariados completados: $completedCount"
        return completedCount
    }

    private fun loadUnlockedAchievements(completedCount: Int) {
        db.collection("achievements")
            .get()
            .addOnSuccessListener { achievementsResult ->
                achievementList.clear()

                for (doc in achievementsResult.documents) {
                    val achievement = doc.toObject(Achievement::class.java)
                    if (achievement != null) {
                        achievement.id = doc.id

                        if (achievement.condition_value <= completedCount) {
                            achievementList.add(achievement)
                        }
                    }
                }

                achievementAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                showSnack("Error al cargar los logros: ${e.message}")
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}