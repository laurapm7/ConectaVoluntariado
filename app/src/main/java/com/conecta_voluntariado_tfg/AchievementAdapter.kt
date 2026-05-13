package com.conecta_voluntariado_tfg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conecta_voluntariado_tfg.databinding.ItemAchievementBinding

class AchievementAdapter(
    private val achievementList: ArrayList<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    inner class AchievementViewHolder(val binding: ItemAchievementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievementList[position]

        holder.binding.tvAchievementTitle.text = achievement.achievement_name
        holder.binding.tvAchievementDescription.text = achievement.achievement_description

        when (achievement.achievement_icon) {
            "achievement1" -> holder.binding.ivAchievementIcon.setImageResource(R.drawable.achievement1)
            "achievement3" -> holder.binding.ivAchievementIcon.setImageResource(R.drawable.achievement3)
        }
    }

    override fun getItemCount(): Int = achievementList.size
}