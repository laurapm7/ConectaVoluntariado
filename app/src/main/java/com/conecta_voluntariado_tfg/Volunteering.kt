package com.conecta_voluntariado_tfg

import com.google.firebase.Timestamp

data class Volunteering(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var city: String = "",
    var date: Timestamp? = null,
    var entity_id: String = "",
    var accessibility: Boolean = false,
    var involves_minors: Boolean = false,
    var status: String = "active",
    var volunteering_type_id: String = ""
)