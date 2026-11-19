package com.example.contactsapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.data.PreferencesManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class ListeContactsFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var preferencesManager: PreferencesManager

    private lateinit var editTextRecherche: TextInputEditText
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var textViewEmpty: TextView
    private lateinit var fabAjouterContact: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liste_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        editTextRecherche = view.findViewById(R.id.editTextRecherche)
        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts)
        textViewEmpty = view.findViewById(R.id.textViewEmpty)
        fabAjouterContact = view.findViewById(R.id.fabAjouterContact)

        // Configuration de l'Adapter avec les préférences utilisateur courantes
        val prefs = preferencesManager.getPreferences()
        adapter = ContactAdapter(emptyList(), prefs) { contact ->
            // Ouverture en mode modification
            val detailFragment = DetailContactFragment.newInstance(contact.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack("liste_to_detail")
                .commit()
        }

        recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewContacts.adapter = adapter

        // Observation des contacts depuis SQLite via ViewModel
        viewModel.contacts.observe(viewLifecycleOwner) { contactsList ->
            adapter.soumettreListe(contactsList)
            if (contactsList.isEmpty()) {
                textViewEmpty.visibility = View.VISIBLE
            } else {
                textViewEmpty.visibility = View.GONE
            }
        }

        // Recherche en direct insensible à la casse
        editTextRecherche.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrerContacts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Bouton FAB + pour créer un nouveau contact (CRUD Create)
        fabAjouterContact.setOnClickListener {
            val nouveauContactFragment = DetailContactFragment.newInstance(-1L)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, nouveauContactFragment)
                .addToBackStack("liste_to_create")
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.titre_liste)

        // Application immédiate des nouvelles préférences utilisateur sans redémarrer
        val nouvellesPrefs = preferencesManager.getPreferences()
        adapter.actualiserPreferences(nouvellesPrefs)

        // Rechargement des données fraîches depuis SQLite
        viewModel.chargerContacts()
    }
}
