package com.conecta_voluntariado_tfg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conecta_voluntariado_tfg.databinding.ItemVolunteeringEntityBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class VolunteeringEntityAdapter(

    private var volunteeringList: ArrayList<Volunteering>,
    private val volunteeringTypeMap: HashMap<String, String>,
    private val onEdit: (Volunteering) -> Unit,
    private val onDelete: (Volunteering) -> Unit
) : RecyclerView.Adapter<VolunteeringEntityAdapter.VolunteeringEntityViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class VolunteeringEntityViewHolder(val binding: ItemVolunteeringEntityBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VolunteeringEntityViewHolder {
        val binding = ItemVolunteeringEntityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VolunteeringEntityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VolunteeringEntityViewHolder, position: Int) {
        val volunteering = volunteeringList[position]
        val typeName = volunteeringTypeMap[volunteering.volunteering_type_id] ?: "Sin tipo"
        holder.binding.tvItemType.text = "Tipo: $typeName"
        holder.binding.tvItemTitle.text = volunteering.title
        holder.binding.tvItemDescription.text = volunteering.description
        holder.binding.tvItemCity.text = volunteering.city
        holder.binding.tvItemDate.text = formatDate(volunteering.date)

        if (volunteering.involves_minors) {
            holder.binding.tvItemMinors.visibility = View.VISIBLE
            holder.binding.tvItemMinors.text =
                "Implica menores (Requiere certificado de delitos sexuales)"
        } else {
            holder.binding.tvItemMinors.visibility = View.GONE
        }

        if (volunteering.accessibility) {
            holder.binding.tvItemAccessibility.visibility = View.VISIBLE
            holder.binding.tvItemAccessibility.text =
                "Accesible para personas con movilidad reducida"
        } else {
            holder.binding.tvItemAccessibility.visibility = View.GONE
        }

        val statusText = if (volunteering.status == "active") "Activo" else "Finalizado"
        holder.binding.tvItemStatus.text = "Estado: $statusText"

        holder.binding.btnEditVolunteering.setOnClickListener {
            onEdit(volunteering)
        }

        holder.binding.btnDelete.setOnClickListener {
            onDelete(volunteering)
        }
    }

    override fun getItemCount(): Int {
        return volunteeringList.size
    }

    private fun formatDate(timestamp: Timestamp?): String {
        return if (timestamp != null) {
            dateFormat.format(timestamp.toDate())
        } else {
            "Sin fecha"
        }
    }
}