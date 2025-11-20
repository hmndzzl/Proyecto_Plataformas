package com.example.proyecto.presentation.utils

import androidx.navigation.NavController

/**
 * Extension para NavController para hacer popBackStack seguro
 */
fun NavController.safePopBackStack(): Boolean {
    return if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        false
    }
}