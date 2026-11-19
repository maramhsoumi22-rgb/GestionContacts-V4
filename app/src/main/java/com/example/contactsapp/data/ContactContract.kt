package com.example.contactsapp.data

import android.provider.BaseColumns

/**
 * Contrat de la base de données SQLite regroupant le nom de la table et des colonnes en constantes
 */
object ContactContract {

    object ContactEntry : BaseColumns {
        const val TABLE_NAME = "contacts"
        const val COLUMN_ID = "_id"
        const val COLUMN_NOM = "nom"
        const val COLUMN_PRENOM = "prenom"
        const val COLUMN_TELEPHONE = "telephone"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHOTO_URI = "photo_uri"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
    }

    const val SQL_CREATE_TABLE = """
        CREATE TABLE ${ContactEntry.TABLE_NAME} (
            ${ContactEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${ContactEntry.COLUMN_NOM} TEXT NOT NULL,
            ${ContactEntry.COLUMN_PRENOM} TEXT NOT NULL,
            ${ContactEntry.COLUMN_TELEPHONE} TEXT NOT NULL,
            ${ContactEntry.COLUMN_EMAIL} TEXT,
            ${ContactEntry.COLUMN_PHOTO_URI} TEXT,
            ${ContactEntry.COLUMN_LATITUDE} REAL,
            ${ContactEntry.COLUMN_LONGITUDE} REAL
        );
    """

    const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS ${ContactEntry.TABLE_NAME}"
}
