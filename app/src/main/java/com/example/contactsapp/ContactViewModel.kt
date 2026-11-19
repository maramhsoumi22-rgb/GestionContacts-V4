package com.example.contactsapp

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactsapp.data.Contact
import com.example.contactsapp.data.ContactDao
import com.example.contactsapp.data.ContactDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: ContactDao = ContactDao(ContactDbHelper.getInstance(application))

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private var dernierTexteRecherche: String = ""

    init {
        chargerContacts()
    }

    fun chargerContacts() {
        viewModelScope.launch {
            val liste = withContext(Dispatchers.IO) {
                if (dernierTexteRecherche.isEmpty()) {
                    dao.getAll()
                } else {
                    dao.search(dernierTexteRecherche)
                }
            }
            _contacts.value = liste
        }
    }

    fun filtrerContacts(query: String) {
        dernierTexteRecherche = query.trim()
        chargerContacts()
    }

    suspend fun getContactById(id: Long): Contact? {
        return withContext(Dispatchers.IO) {
            dao.getById(id)
        }
    }

    fun insererContact(contact: Contact, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                dao.insert(contact)
            }
            chargerContacts()
            onComplete?.invoke(id)
        }
    }

    fun modifierContact(contact: Contact, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.update(contact)
            }
            chargerContacts()
            onComplete?.invoke()
        }
    }

    fun supprimerContact(id: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.delete(id)
            }
            chargerContacts()
            onComplete?.invoke()
        }
    }

    /**
     * Enregistre une copie de l'image sélectionnée dans le stockage interne privé de l'application (filesDir)
     * afin d'éviter qu'une URI temporaire devienne inaccessible après redémarrage de l'appareil.
     */
    suspend fun copierImageEnStockageInterne(sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@withContext null
                val fileName = "contact_img_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.filesDir, fileName)

                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                destinationFile.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Sauvegarde un bitmap issu de la caméra dans le stockage interne filesDir
     */
    suspend fun sauvegarderBitmapEnStockageInterne(bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val fileName = "contact_cam_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.filesDir, fileName)

                FileOutputStream(destinationFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                destinationFile.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }
}
