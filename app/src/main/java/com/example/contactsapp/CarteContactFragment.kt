package com.example.contactsapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarteContactFragment : Fragment(), OnMapReadyCallback {

    private var contactNom: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        private const val ARG_NOM = "arg_nom"
        private const val ARG_LAT = "arg_lat"
        private const val ARG_LNG = "arg_lng"

        fun newInstance(nom: String, latitude: Double, longitude: Double): CarteContactFragment {
            val fragment = CarteContactFragment()
            val args = Bundle().apply {
                putString(ARG_NOM, nom)
                putDouble(ARG_LAT, latitude)
                putDouble(ARG_LNG, longitude)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactNom = arguments?.getString(ARG_NOM) ?: "Contact"
        latitude = arguments?.getDouble(ARG_LAT) ?: 0.0
        longitude = arguments?.getDouble(ARG_LNG) ?: 0.0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_carte_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.titre_carte)

        val btnRetourDetail = view.findViewById<Button>(R.id.btnRetourDetail)
        btnRetourDetail.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, it)
                    .commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(latitude, longitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(contactNom)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14.0f))
        googleMap.uiSettings.isZoomControlsEnabled = true
    }
}
