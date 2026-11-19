package com.example.contactsapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactDbHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ContactContract.SQL_CREATE_TABLE)
        insererContactsDemonstration(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(ContactContract.SQL_DROP_TABLE)
        onCreate(db)
    }

    /**
     * Insertion de 5 contacts de démonstration dont 2 sans coordonnées GPS
     */
    private fun insererContactsDemonstration(db: SQLiteDatabase) {
        val contactsDemo = listOf(
            Contact(0L, "Ben Ali", "Youssef", "+216 20 123 456", "youssef.benali@example.com", null, 36.8065, 10.1815),
            Contact(0L, "Dupont", "Claire", "+33 6 12 34 56 78", "claire.dupont@example.com", null, 48.8566, 2.3522),
            Contact(0L, "Trabelsi", "Amira", "+216 98 765 432", "amira.trabelsi@example.com", null, 35.8256, 10.6084),
            Contact(0L, "Martin", "Lucas", "+33 7 98 76 54 32", "lucas.martin@example.com", null, null, null), // Sans GPS
            Contact(0L, "Khemir", "Sami", "+216 55 443 322", "sami.khemir@example.com", null, null, null)    // Sans GPS
        )

        for (c in contactsDemo) {
            val values = ContentValues().apply {
                put(ContactContract.ContactEntry.COLUMN_NOM, c.nom)
                put(ContactContract.ContactEntry.COLUMN_PRENOM, c.prenom)
                put(ContactContract.ContactEntry.COLUMN_TELEPHONE, c.telephone)
                put(ContactContract.ContactEntry.COLUMN_EMAIL, c.email)
                put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, c.photoUri)
                if (c.latitude != null) {
                    put(ContactContract.ContactEntry.COLUMN_LATITUDE, c.latitude)
                } else {
                    putNull(ContactContract.ContactEntry.COLUMN_LATITUDE)
                }
                if (c.longitude != null) {
                    put(ContactContract.ContactEntry.COLUMN_LONGITUDE, c.longitude)
                } else {
                    putNull(ContactContract.ContactEntry.COLUMN_LONGITUDE)
                }
            }
            db.insert(ContactContract.ContactEntry.TABLE_NAME, null, values)
        }
    }
}
