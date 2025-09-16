package com.example.trabajo1.ui.map
import com.example.trabajo1.R


import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.PixelCopy.request
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

//logica del mapa

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var viewModel: MapViewModel

    private lateinit var mMap: GoogleMap //guarda referencia al mapa
    private lateinit var fusedLocationClient: FusedLocationProviderClient //es quien me da la ubicacion actual del disp
//    private lateinit var txtDireccion: TextView //donde quiero que se vea la direccion
    private lateinit var edtDireccion: EditText
    private lateinit var btnBuscar: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edtDireccion = view.findViewById(R.id.edtDireccion)
        btnBuscar = view.findViewById(R.id.btnBuscarDireccion)

        // Cuando tocan buscar
        btnBuscar.setOnClickListener {
            val query = edtDireccion.text.toString()
            if (query.isNotEmpty()) {
                buscarLugar(query)
            } else {
                Toast.makeText(requireContext(), "Ingrese una dirección", Toast.LENGTH_SHORT).show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
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
                                edtDireccion.setText(place.address ?: place.name)
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "No se encontró el lugar", Toast.LENGTH_SHORT).show()
                }
            }

    }
//    private fun buscarDireccion(direccion: String) {
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        try {
//            //tomar la ubicacion actual como referencia
//            if(ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//            ){
//                return
//            }
//
//            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                if (location != null){
//                    //defino el radio
//                    val lat = location.latitude
//                    val lng = location.longitude
//                    val delta = 0.5 //radio en km
//
//                    val direcciones = geocoder.getFromLocationName(
//                        direccion,
//                        1,
//                        lat - delta, lng - delta,
//                        lat + delta, lng + delta
//                    )
//
//                    if(direcciones != null && direcciones.isNotEmpty()){
//                        val loc = direcciones[0]
//                        val latLng = LatLng(loc.latitude, loc.longitude)
//
//                        mMap.clear()
//                        mMap.addMarker(MarkerOptions().position(latLng).title(direccion))
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
//                    }else{
//                        Toast.makeText(requireContext(), "No se encontró la dirección", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(requireContext(), "Error buscando la dirección", Toast.LENGTH_SHORT).show()
//        }
//    }


    //lo hice para cargar el mapa y que se guarde en mMap y pedir los permisos de ubicacion para usarlo
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap //guarda referencia al mapa
        pedirPermisos()

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

                // 👇 acá podés guardar en Firebase lo que necesites
                val datos = hashMapOf(
                    "lat" to latLng.latitude,
                    "lng" to latLng.longitude,
                    "direccion" to direccion
                )
                // Firebase.firestore.collection("lugares").add(datos) ...
            } else {
                edtDireccion.setText("${latLng.latitude}, ${latLng.longitude}")
            }
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

}