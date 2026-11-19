package com.example.contactsapp.data

import java.io.Serializable

/**
 * Entité représentant un Contact dans la base SQLite
 * @property id Identifiant unique (_id) attribué par la base (0 si non encore inséré)
 */
data class Contact(
    val id: Long = 0L,
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String? = null,
    var photoUri: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) : Serializable
