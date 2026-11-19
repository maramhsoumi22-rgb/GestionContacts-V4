package com.example.contactsapp.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class ContactDao(private val dbHelper: ContactDbHelper) {

    /**
     * Insère un nouveau contact dans la base SQLite
     * @return L'_id généré par SQLite
     */
    fun insert(contact: Contact): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.ContactEntry.COLUMN_NOM, contact.nom)
            put(ContactContract.ContactEntry.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.ContactEntry.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.ContactEntry.COLUMN_EMAIL, contact.email)
            put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, contact.photoUri)
            if (contact.latitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LATITUDE, contact.latitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LATITUDE)
            }
            if (contact.longitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LONGITUDE, contact.longitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LONGITUDE)
            }
        }
        return db.insert(ContactContract.ContactEntry.TABLE_NAME, null, values)
    }

    /**
     * Met à jour les informations d'un contact existant
     * @return Le nombre de lignes modifiées (1 en cas de succès)
     */
    fun update(contact: Contact): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.ContactEntry.COLUMN_NOM, contact.nom)
            put(ContactContract.ContactEntry.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.ContactEntry.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.ContactEntry.COLUMN_EMAIL, contact.email)
            put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, contact.photoUri)
            if (contact.latitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LATITUDE, contact.latitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LATITUDE)
            }
            if (contact.longitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LONGITUDE, contact.longitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LONGITUDE)
            }
        }
        val selection = "${ContactContract.ContactEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(contact.id.toString())
        return db.update(ContactContract.ContactEntry.TABLE_NAME, values, selection, selectionArgs)
    }

    /**
     * Supprime un contact par son identifiant unique
     * @return Le nombre de lignes supprimées (1 en cas de succès)
     */
    fun delete(id: Long): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val selection = "${ContactContract.ContactEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        return db.delete(ContactContract.ContactEntry.TABLE_NAME, selection, selectionArgs)
    }

    /**
     * Récupère tous les contacts triés par nom puis prénom sans tenir compte de la casse
     */
    fun getAll(): List<Contact> {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val contacts = mutableListOf<Contact>()
        val orderBy = "${ContactContract.ContactEntry.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.ContactEntry.COLUMN_PRENOM} COLLATE NOCASE ASC"

        db.query(
            ContactContract.ContactEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            orderBy
        ).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(extraireContactDuCursor(cursor))
            }
        }
        return contacts
    }

    /**
     * Récupère un contact précis via son identifiant _id
     */
    fun getById(id: Long): Contact? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val selection = "${ContactContract.ContactEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        return db.query(
            ContactContract.ContactEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                extraireContactDuCursor(cursor)
            } else {
                null
            }
        }
    }

    /**
     * Recherche textuelle paramétrée insensible à la casse sur nom, prénom ou téléphone
     */
    fun search(texte: String): List<Contact> {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val contacts = mutableListOf<Contact>()
        val pattern = "%$texte%"
        val selection = """
            ${ContactContract.ContactEntry.COLUMN_NOM} LIKE ? OR 
            ${ContactContract.ContactEntry.COLUMN_PRENOM} LIKE ? OR 
            ${ContactContract.ContactEntry.COLUMN_TELEPHONE} LIKE ?
        """.trimIndent()
        val selectionArgs = arrayOf(pattern, pattern, pattern)
        val orderBy = "${ContactContract.ContactEntry.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.ContactEntry.COLUMN_PRENOM} COLLATE NOCASE ASC"

        db.query(
            ContactContract.ContactEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        ).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(extraireContactDuCursor(cursor))
            }
        }
        return contacts
    }

    private fun extraireContactDuCursor(cursor: Cursor): Contact {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_ID))
        val nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NOM))
        val prenom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_PRENOM))
        val telephone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_TELEPHONE))

        val emailIndex = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_EMAIL)
        val email = if (cursor.isNull(emailIndex)) null else cursor.getString(emailIndex)

        val photoIndex = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_PHOTO_URI)
        val photoUri = if (cursor.isNull(photoIndex)) null else cursor.getString(photoIndex)

        val latIndex = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_LATITUDE)
        val latitude = if (cursor.isNull(latIndex)) null else cursor.getDouble(latIndex)

        val lngIndex = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_LONGITUDE)
        val longitude = if (cursor.isNull(lngIndex)) null else cursor.getDouble(lngIndex)

        return Contact(
            id = id,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude
        )
    }
}
