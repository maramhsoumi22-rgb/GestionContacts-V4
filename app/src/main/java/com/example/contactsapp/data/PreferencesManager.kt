package com.example.contactsapp.data

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

data class UserPreferences(
    val taillePoliceSp: Float,
    val couleurTexte: Int,
    val appelDepuisListe: Boolean
)

class PreferencesManager(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    companion object {
        const val KEY_TAILLE_POLICE = "taille_police"
        const val KEY_COULEUR_PREFEREE = "couleur_preferee"
        const val KEY_APPEL_DEPUIS_LISTE = "appel_depuis_liste"
    }

    fun getPreferences(): UserPreferences {
        val tailleStr = sharedPreferences.getString(KEY_TAILLE_POLICE, "16") ?: "16"
        val tailleSp = tailleStr.toFloatOrNull() ?: 16f

        val couleurHex = sharedPreferences.getString(KEY_COULEUR_PREFEREE, "#212121") ?: "#212121"
        val couleur = try {
            Color.parseColor(couleurHex)
        } catch (e: Exception) {
            Color.parseColor("#212121")
        }

        val appelActif = sharedPreferences.getBoolean(KEY_APPEL_DEPUIS_LISTE, false)

        return UserPreferences(
            taillePoliceSp = tailleSp,
            couleurTexte = couleur,
            appelDepuisListe = appelActif
        )
    }
}
