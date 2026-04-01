package com.conecta_voluntariado_tfg

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.conecta_voluntariado_tfg.databinding.ActivityEntityHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EntityHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntityHomeBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val volunteeringList = ArrayList<Volunteering>()
    private lateinit var adapter: VolunteeringEntityAdapter
    private val typeMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = VolunteeringEntityAdapter(
            volunteeringList,
            typeMap,
            onDelete = { v ->
                deleteVolunteering(v.id)
            }
        )

        binding.rvVolunteerings.layoutManager = LinearLayoutManager(this)
        binding.rvVolunteerings.adapter = adapter

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, CreateVolunteeringActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, EntityProfileActivity::class.java))
        }

        binding.btnHome.setOnClickListener {
            loadMyVolunteerings()
        }

        loadTypesAndVolunteerings()
    }

    override fun onResume() {
        super.onResume()

        loadMyVolunteerings()
    }

    private fun loadMyVolunteerings() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showSnack("No hay sesión iniciada")
            return
        }

        db.collection("volunteerings")
            .whereEqualTo("entity_id", uid)
            .get()
            .addOnSuccessListener { result ->
                volunteeringList.clear()

                for (doc in result.documents) {
                    val v = doc.toObject(Volunteering::class.java)
                    if (v != null) {
                        v.id = doc.id
                        volunteeringList.add(v)
                    }
                }

                adapter.notifyDataSetChanged()

                binding.tvEmpty.visibility =
                    if (volunteeringList.isEmpty()) View.VISIBLE else View.GONE
                 }
                    .addOnFailureListener { e->
                        showSnack("Error al cargar los voluntariados: ${e.message}")
                    }
    }

    private fun deleteVolunteering(id: String) {
        db.collection("volunteerings").document(id).delete()
            .addOnSuccessListener {
                showSnack("Voluntariado borrado")
                loadMyVolunteerings()
            }
            .addOnFailureListener { e ->
                showSnack("Error al borrar el voluntariado: ${e.message}")
            }
    }

    private fun loadTypesAndVolunteerings() {
        db.collection("volunteering_types")
            .get()
            .addOnSuccessListener { result ->
                typeMap.clear()

                for (doc in result.documents) {
                    val name = doc.getString("type_name")
                    if (name != null) {
                        typeMap[doc.id] = name
                    }
                }

                loadMyVolunteerings()
            }
            .addOnFailureListener { e->
                showSnack("Error al cargar los tipos de voluntariado: ${e.message}")
            }
    }

    private fun showSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}