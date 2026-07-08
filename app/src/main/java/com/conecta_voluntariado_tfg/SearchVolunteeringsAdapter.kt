package com.conecta_voluntariado_tfg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conecta_voluntariado_tfg.databinding.ItemVolunteeringVolunteerBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchVolunteeringsAdapter(

    private val filteredVolunteeringsList: ArrayList<Volunteering>,
    private val volunteeringTypeMap: HashMap<String, String>,
    private val onSignUpClick: (Volunteering) -> Unit
) : RecyclerView.Adapter<SearchVolunteeringsAdapter.SearchVolunteeringViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class SearchVolunteeringViewHolder(val binding: ItemVolunteeringVolunteerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchVolunteeringViewHolder {
        val binding = ItemVolunteeringVolunteerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return SearchVolunteeringViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchVolunteeringViewHolder, position: Int) {
        val volunteering = filteredVolunteeringsList[position]
        val typeName = volunteeringTypeMap[volunteering.volunteering_type_id] ?: "Sin tipo"

        holder.binding.tvItemTitle.text = volunteering.title
        holder.binding.tvItemType.text = "Tipo: $typeName"
        holder.binding.tvItemDescription.text = volunteering.description
        holder.binding.tvItemCity.text = "Ciudad: ${volunteering.city}"
        holder.binding.tvItemAddress.text = "Dirección: ${volunteering.address}"
        holder.binding.tvItemDate.text = "Fecha y hora: ${formatDate(volunteering.date)}"

        if (volunteering.involves_minors) {
            holder.binding.tvItemMinors.visibility = View.VISIBLE
        } else {
            holder.binding.tvItemMinors.visibility = View.GONE
        }

        if (volunteering.accessibility) {
            holder.binding.tvItemAccessibility.visibility = View.VISIBLE
        } else {
            holder.binding.tvItemAccessibility.visibility = View.GONE
        }

        val currentDate = Date()
        val volunteeringDate = volunteering.date?.toDate()

        if (volunteeringDate != null && volunteeringDate.before(currentDate)) {
            holder.binding.tvItemStatus.text = "Estado: Finalizado"
            holder.binding.btnSignUpVolunteering.visibility = View.GONE
        } else {
            holder.binding.tvItemStatus.text = "Estado: Activo"
            holder.binding.btnSignUpVolunteering.visibility = View.VISIBLE

            holder.binding.btnSignUpVolunteering.setOnClickListener {
                onSignUpClick(volunteering)
            }
        }
    }

    override fun getItemCount(): Int = filteredVolunteeringsList.size

    private fun formatDate(timestamp: Timestamp?): String {
        return if (timestamp != null) {
            dateFormat.format(timestamp.toDate())
        } else {
            "Sin fecha"
        }
    }
}