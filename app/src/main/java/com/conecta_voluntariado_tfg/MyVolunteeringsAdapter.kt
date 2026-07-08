package com.conecta_voluntariado_tfg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conecta_voluntariado_tfg.databinding.ItemMyVolunteeringBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class MyVolunteeringsAdapter(

    private val myVolunteeringsList: ArrayList<Volunteering>,
    private val volunteeringTypeMap: HashMap<String, String>,
    private val onCancelRegistrationClick: (Volunteering) -> Unit
) : RecyclerView.Adapter<MyVolunteeringsAdapter.MyVolunteeringsViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class MyVolunteeringsViewHolder(val binding: ItemMyVolunteeringBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVolunteeringsViewHolder {
        val binding = ItemMyVolunteeringBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyVolunteeringsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyVolunteeringsViewHolder, position: Int) {
        val volunteering = myVolunteeringsList[position]
        val typeName = volunteeringTypeMap[volunteering.volunteering_type_id] ?: "Sin tipo"

        holder.binding.tvItemTitle.text = volunteering.title
        holder.binding.tvItemType.text = "Tipo: $typeName"
        holder.binding.tvItemDescription.text = volunteering.description
        holder.binding.tvItemCity.text = "Ciudad: ${volunteering.city}"
        holder.binding.tvItemDate.text = "Fecha: ${formatDate(volunteering.date)}"
        holder.binding.btnCancelRegistration.setOnClickListener {
            onCancelRegistrationClick(volunteering)
        }

        val statusText = if (isCompleted(volunteering.date)) "Completado" else "Inscrito"
        holder.binding.tvItemStatus.text = "Estado: $statusText"

        if (isCompleted(volunteering.date)) {
            holder.binding.btnCancelRegistration.visibility = View.GONE
        } else {
            holder.binding.btnCancelRegistration.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = myVolunteeringsList.size

    private fun formatDate(timestamp: Timestamp?): String {
        return if (timestamp != null) {
            dateFormat.format(timestamp.toDate())
        } else {
            "Sin fecha"
        }
    }

    private fun isCompleted(timestamp: Timestamp?): Boolean {
        val now = Date()
        return timestamp?.toDate()?.before(now) == true
    }
}