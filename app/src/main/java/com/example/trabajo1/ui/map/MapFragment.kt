package com.example.trabajo1.ui.map

import com.example.trabajo1.R
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale
import kotlin.collections.isNotEmpty
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.firebase.database.FirebaseDatabase

//logica del mapa

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap //guarda referencia al mapa
    private lateinit var fusedLocationClient: FusedLocationProviderClient //es quien me da la ubicacion actual del disp
//    private lateinit var txtDireccion: TextView //donde quiero que se vea la direccion
    private lateinit var edtDireccion: EditText
    private lateinit var btnBuscar: ImageButton
    private lateinit var btnGuardar: android.widget.Button
    private lateinit var btnBack: ImageButton

    private var ultimaLatLng: LatLng? = null
    private var ultimaDireccion: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Oculta la Toolbar del Activity (si existe) en el oncreatedview
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        // Oculta la BottomNavigationView
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.GONE

        edtDireccion = view.findViewById(R.id.edtDireccion)
        btnBuscar = view.findViewById(R.id.btnBuscarDireccion)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnBack = view.findViewById(R.id.btnBack)

        // Cuando tocan buscar
        btnBuscar.setOnClickListener {
            val query = edtDireccion.text.toString()
            if (query.isNotEmpty()) {
                buscarLugar(query)
            } else {
                Toast.makeText(requireContext(), "Ingrese una dirección", Toast.LENGTH_SHORT).show()
            }
        }

        // Guardar en Firebase cuando aprietan el botón
        btnGuardar.setOnClickListener {
            if (ultimaLatLng != null && ultimaDireccion != null) {
//                guardar(ultimaLatLng!!, ultimaDireccion!!)
                mostrarDialogoReferencia()
            } else {
                Toast.makeText(requireContext(), "Seleccione una ubicación primero", Toast.LENGTH_SHORT).show()
            }
        }

        val btnVerGuardados = view.findViewById<Button>(R.id.btnVerGuardados)
        btnVerGuardados.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_listaFragment)
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        btnBack.setOnClickListener {
//            stopRecording()
            findNavController().navigateUp()
        }
    }

    private fun mostrarDialogoReferencia() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialogo_referencia, null)
        builder.setView(dialogView)

        //Para que escriba en la referencia
        val input = dialogView.findViewById<EditText>(R.id.edtReferenciaDialog)
        val btnGuardarDialog = dialogView.findViewById<Button>(R.id.btnGuardarDialog)
        val btnCancelarDialog = dialogView.findViewById<Button>(R.id.btnCancelarDialog)

        val dialog = builder.create()
        dialog.show()

        // Botón Guardar
        btnGuardarDialog.setOnClickListener {
            val referencia = input.text.toString()
            if (referencia.isNotEmpty()) {
                guardarConReferencia(ultimaLatLng!!, ultimaDireccion!!, referencia)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Ingrese una referencia", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Cancelar
        btnCancelarDialog.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun guardarConReferencia(latLng: LatLng, direccion: String, referencia: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("lugares")

        val lugarId = ref.push().key
        val datos = hashMapOf(
            "lat" to latLng.latitude,
            "lng" to latLng.longitude,
            "direccion" to direccion,
            "referencia" to referencia
        )

        if (lugarId != null) {
            ref.child(lugarId).setValue(datos)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Lugar guardado", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar el lugar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarLugar(query: String) {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }

        val placesClient = Places.createClient(requireContext())
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("AR") // limitar a Argentina
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                if (response.autocompletePredictions.isNotEmpty()) {
                    val prediction = response.autocompletePredictions[0]
                    val placeId = prediction.placeId

                    val placeRequest = FetchPlaceRequest.builder(
                        placeId,
                        listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
                    ).build()

                    placesClient.fetchPlace(placeRequest)
                        .addOnSuccessListener { placeResponse ->
                            val place = placeResponse.place
                            val latLng = place.latLng

                            if (latLng != null) {
                                mMap.clear()
                                mMap.addMarker(MarkerOptions().position(latLng).title(place.name))
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

                                // 👇 Actualizar EditText con la dirección encontrada
                                val direccion = place.address ?: place.name
                                edtDireccion.setText(direccion)
                                ultimaDireccion = direccion
                                ultimaLatLng = latLng
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "No se encontró el lugar", Toast.LENGTH_SHORT).show()
                }
            }

    }


    //lo hice para cargar el mapa y que se guarde en mMap y pedir los permisos de ubicacion para usarlo
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap //guarda referencia al mapa
        pedirPermisos()

        //para mover el boton de "mi ubicacion"
        val mapView =(childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view
        mapView?.let {
            val locationButton = (it.parent as View).findViewById<View>("1".toInt())?.parent
                ?.let { parent -> (parent as View).findViewById<View>("2".toInt()) }
            locationButton?.let { btn ->
                val params = btn.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, 500, 50, 0)
                btn.layoutParams =params
            }
        }

        //click en el mapa
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Punto elegido"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            // Usamos Geocoder para traducir coords -> dirección
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val direcciones = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (direcciones != null && direcciones.isNotEmpty()) {
                val direccion = direcciones[0].getAddressLine(0)
                edtDireccion.setText(direccion)
                ultimaDireccion = direccion
            } else {
                ultimaDireccion = "${latLng.latitude}, ${latLng.longitude}"
                edtDireccion.setText(ultimaDireccion)
//                edtDireccion.setText("${latLng.latitude}, ${latLng.longitude}")
            }
            ultimaLatLng = latLng
        }
    }

    //si los permisos se dieron, se muestra la ubicacion sino se vuelven a pedir los permisos
    private fun pedirPermisos() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mostrarUbicacion()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
    }

    private fun mostrarUbicacion() {
        // Verificar explícitamente el permiso antes de usar la ubicación
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val miPos = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miPos, 16f))
                mMap.addMarker(MarkerOptions().position(miPos).title("Estoy aquí"))

                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val direcciones = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (direcciones != null && direcciones.isNotEmpty()) {
                    val direccion = direcciones[0].getAddressLine(0)
                    edtDireccion.setText(direccion)
                }
            }
        }
    }

    // Para manejar el permiso
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mostrarUbicacion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Vuelve a mostrarla cuando salís del fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        // Vuelve a mostrar la BottomNavigationView
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.VISIBLE
    }
}