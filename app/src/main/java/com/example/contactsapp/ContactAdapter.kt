package com.example.contactsapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.data.Contact
import com.example.contactsapp.data.UserPreferences
import java.io.File

class ContactAdapter(
    private var contacts: List<Contact>,
    private var preferences: UserPreferences,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewPhoto: ImageView = itemView.findViewById(R.id.imageViewContact)
        val textViewNomPrenom: TextView = itemView.findViewById(R.id.textViewNomPrenom)
        val textViewTelephone: TextView = itemView.findViewById(R.id.textViewTelephone)
        val btnAppelerItem: Button = itemView.findViewById(R.id.btnAppelerItem)

        fun bind(contact: Contact) {
            val nomComplet = "${contact.nom.uppercase()} ${contact.prenom}"
            textViewNomPrenom.text = nomComplet
            textViewTelephone.text = contact.telephone

            // Application dynamique des préférences utilisateur
            textViewNomPrenom.textSize = preferences.taillePoliceSp
            textViewNomPrenom.setTextColor(preferences.couleurTexte)
            textViewTelephone.textSize = (preferences.taillePoliceSp - 2f).coerceAtLeast(12f)

            // Gestion du bouton Appeler direct selon la préférence 'appel_depuis_liste'
            if (preferences.appelDepuisListe) {
                btnAppelerItem.visibility = View.VISIBLE
                btnAppelerItem.setOnClickListener {
                    val tel = contact.telephone.trim()
                    if (tel.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                        try {
                            itemView.context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(itemView.context, "Aucune application d'appel disponible", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                btnAppelerItem.visibility = View.GONE
            }

            // Chargement de l'image (depuis fichier interne ou URI)
            val photoPath = contact.photoUri
            if (!photoPath.isNullOrEmpty()) {
                val imgFile = File(photoPath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    imageViewPhoto.setImageBitmap(bitmap)
                } else {
                    try {
                        imageViewPhoto.setImageURI(Uri.parse(photoPath))
                    } catch (e: Exception) {
                        imageViewPhoto.setImageResource(R.drawable.ic_avatar_placeholder)
                    }
                }
            } else {
                imageViewPhoto.setImageResource(R.drawable.ic_avatar_placeholder)
            }

            itemView.setOnClickListener {
                onContactClick(contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    fun soumettreListe(nouvelleListe: List<Contact>) {
        this.contacts = nouvelleListe
        notifyDataSetChanged()
    }

    fun actualiserPreferences(nouvellesPrefs: UserPreferences) {
        this.preferences = nouvellesPrefs
        notifyDataSetChanged()
    }
}
