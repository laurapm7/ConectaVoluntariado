package com.conecta_voluntariado_tfg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.conecta_voluntariado_tfg.databinding.ActivitySearchVolunteeringBinding

class SearchVolunteeringActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchVolunteeringBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchVolunteeringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHomeVolunteer.setOnClickListener {
            startActivity(Intent(this, VolunteerHomeActivity::class.java))
            finish()
        }
    }
}