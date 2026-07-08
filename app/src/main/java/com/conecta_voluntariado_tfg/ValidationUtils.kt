package com.conecta_voluntariado_tfg

import java.util.Date
import com.google.firebase.Timestamp

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && email.contains("@")
    }

    fun isValidPassword(pass: String): Boolean {
        return pass.isNotEmpty() && pass.length >= 6
    }

}