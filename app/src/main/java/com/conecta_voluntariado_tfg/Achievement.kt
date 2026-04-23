package com.conecta_voluntariado_tfg

data class Achievement(
    var id: String = "",
    var achievement_name: String = "",
    var achievement_description: String = "",
    var achievement_icon: String = "",
    var condition_value: Int = 0
)