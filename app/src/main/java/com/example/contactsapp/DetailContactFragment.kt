package com.example.contactsapp

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.contactsapp.data.Contact
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File

class DetailContactFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private var contactId: Long = -1L
    private var contactExistant: Contact? = null

    // Vues
    private lateinit var imageViewPhoto: ImageView
    private lateinit var btnGalerie: Button
    private lateinit var btnCamera: Button
    private lateinit var btnAppeler: Button
    private lateinit var btnEmail: Button
    private lateinit var editTextNom: TextInputEditText
    private lateinit var editTextPrenom: TextInputEditText
    private lateinit var editTextTelephone: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextLatitude: TextInputEditText
    private lateinit var editTextLongitude: TextInputEditText
    private lateinit var btnVoirCarte: Button
    private lateinit var btnAnnuler: Button
    private lateinit var btnEnregistrer: Button
    private lateinit var btnSupprimer: Button

    private var cheminPhotoInterne: String? = null

    // Sélection depuis la Galerie avec copie automatique dans les fichiers internes de l'app
    private val selectPictureLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val path = viewModel.copierImageEnStockageInterne(uri)
                if (path != null) {
                    cheminPhotoInterne = path
                    val bitmap = BitmapFactory.decodeFile(path)
                    imageViewPhoto.setImageBitmap(bitmap)
                } else {
                    imageViewPhoto.setImageURI(uri)
                    cheminPhotoInterne = uri.toString()
                }
            }
        }
    }

    // Capture caméra avec enregistrement dans filesDir
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val path = viewModel.sauvegarderBitmapEnStockageInterne(bitmap)
                cheminPhotoInterne = path
                imageViewPhoto.setImageBitmap(bitmap)
            }
        }
    }

    companion object {
        private const val ARG_CONTACT_ID = "arg_contact_id"

        fun newInstance(contactId: Long): DetailContactFragment {
            val fragment = DetailContactFragment()
            val args = Bundle().apply {
                putLong(ARG_CONTACT_ID, contactId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getLong(ARG_CONTACT_ID) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewPhoto = view.findViewById(R.id.detailImageViewPhoto)
        btnGalerie = view.findViewById(R.id.btnGalerie)
        btnCamera = view.findViewById(R.id.btnCamera)
        btnAppeler = view.findViewById(R.id.btnAppeler)
        btnEmail = view.findViewById(R.id.btnEmail)
        editTextNom = view.findViewById(R.id.editTextNom)
        editTextPrenom = view.findViewById(R.id.editTextPrenom)
        editTextTelephone = view.findViewById(R.id.editTextTelephone)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextLatitude = view.findViewById(R.id.editTextLatitude)
        editTextLongitude = view.findViewById(R.id.editTextLongitude)
        btnVoirCarte = view.findViewById(R.id.btnVoirCarte)
        btnAnnuler = view.findViewById(R.id.btnAnnuler)
        btnEnregistrer = view.findViewById(R.id.btnEnregistrer)
        btnSupprimer = view.findViewById(R.id.btnSupprimer)

        val isModeCreation = (contactId <= 0L)
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            if (isModeCreation) getString(R.string.titre_nouveau) else getString(R.string.titre_detail)

        if (isModeCreation) {
            btnSupprimer.visibility = View.GONE
        } else {
            btnSupprimer.visibility = View.VISIBLE
            chargerContactExistant()
        }

        btnGalerie.setOnClickListener { selectPictureLauncher.launch("image/*") }
        btnCamera.setOnClickListener { takePictureLauncher.launch(null) }

        btnAppeler.setOnClickListener {
            val tel = editTextTelephone.text?.toString()?.trim() ?: ""
            if (tel.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), getString(R.string.erreur_aucune_app), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.champ_obligatoire), Toast.LENGTH_SHORT).show()
            }
        }

        btnEmail.setOnClickListener {
            val email = editTextEmail.text?.toString()?.trim() ?: ""
            if (email.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), getString(R.string.erreur_aucune_app), Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnVoirCarte.setOnClickListener { ouvrirCarte() }

        btnAnnuler.setOnClickListener { parentFragmentManager.popBackStack() }

        btnEnregistrer.setOnClickListener { validerEtSauvegarder() }

        btnSupprimer.setOnClickListener { demanderConfirmationSuppression() }
    }

    private fun chargerContactExistant() {
        viewLifecycleOwner.lifecycleScope.launch {
            contactExistant = viewModel.getContactById(contactId)
            contactExistant?.let { contact ->
                editTextNom.setText(contact.nom)
                editTextPrenom.setText(contact.prenom)
                editTextTelephone.setText(contact.telephone)
                editTextEmail.setText(contact.email ?: "")
                editTextLatitude.setText(contact.latitude?.toString() ?: "")
                editTextLongitude.setText(contact.longitude?.toString() ?: "")
                cheminPhotoInterne = contact.photoUri

                if (!contact.photoUri.isNullOrEmpty()) {
                    val file = File(contact.photoUri!!)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        imageViewPhoto.setImageBitmap(bitmap)
                    } else {
                        try {
                            imageViewPhoto.setImageURI(Uri.parse(contact.photoUri))
                        } catch (e: Exception) {
                            imageViewPhoto.setImageResource(R.drawable.ic_avatar_placeholder)
                        }
                    }
                }
            }
        }
    }

    private fun ouvrirCarte() {
        val latStr = editTextLatitude.text?.toString()?.trim() ?: ""
        val lngStr = editTextLongitude.text?.toString()?.trim() ?: ""

        if (latStr.isEmpty() || lngStr.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.erreur_coordonnees_absentes), Toast.LENGTH_LONG).show()
            return
        }

        val lat = latStr.toDoubleOrNull()
        val lng = lngStr.toDoubleOrNull()

        if (lat == null || lng == null || lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            Toast.makeText(requireContext(), getString(R.string.erreur_coordonnees_invalides), Toast.LENGTH_LONG).show()
            return
        }

        val nom = editTextNom.text?.toString() ?: "Contact"
        val prenom = editTextPrenom.text?.toString() ?: ""
        val carteFragment = CarteContactFragment.newInstance("$prenom $nom", lat, lng)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, carteFragment)
            .addToBackStack("detail_to_map")
            .commit()
    }

    private fun validerEtSauvegarder() {
        val nom = editTextNom.text?.toString()?.trim() ?: ""
        val prenom = editTextPrenom.text?.toString()?.trim() ?: ""
        val telephone = editTextTelephone.text?.toString()?.trim() ?: ""
        val email = editTextEmail.text?.toString()?.trim()
        val latStr = editTextLatitude.text?.toString()?.trim() ?: ""
        val lngStr = editTextLongitude.text?.toString()?.trim() ?: ""

        var valide = true
        if (nom.isEmpty()) {
            editTextNom.error = getString(R.string.champ_obligatoire)
            valide = false
        }
        if (prenom.isEmpty()) {
            editTextPrenom.error = getString(R.string.champ_obligatoire)
            valide = false
        }
        if (telephone.isEmpty()) {
            editTextTelephone.error = getString(R.string.champ_obligatoire)
            valide = false
        }

        var lat: Double? = null
        var lng: Double? = null
        if (latStr.isNotEmpty() || lngStr.isNotEmpty()) {
            lat = latStr.toDoubleOrNull()
            lng = lngStr.toDoubleOrNull()
            if (lat == null || lng == null || lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
                Toast.makeText(requireContext(), getString(R.string.erreur_coordonnees_invalides), Toast.LENGTH_LONG).show()
                valide = false
            }
        }

        if (!valide) return

        val contact = Contact(
            id = if (contactId > 0L) contactId else 0L,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = if (email.isNullOrEmpty()) null else email,
            photoUri = cheminPhotoInterne,
            latitude = lat,
            longitude = lng
        )

        if (contactId > 0L) {
            viewModel.modifierContact(contact) {
                Toast.makeText(requireContext(), getString(R.string.contact_enregistre), Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        } else {
            viewModel.insererContact(contact) {
                Toast.makeText(requireContext(), getString(R.string.contact_enregistre), Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun demanderConfirmationSuppression() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_suppression_titre))
            .setMessage(getString(R.string.confirm_suppression_message))
            .setPositiveButton(getString(R.string.action_oui)) { _, _ ->
                viewModel.supprimerContact(contactId) {
                    Toast.makeText(requireContext(), getString(R.string.contact_supprime), Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
            .setNegativeButton(getString(R.string.action_non), null)
            .show()
    }
}
