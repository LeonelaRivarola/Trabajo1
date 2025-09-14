package com.example.trabajo1.ui.map
import com.example.trabajo1.R


import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContentProviderCompat.requireContext
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

//logica del mapa

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var viewModel: MapViewModel

    private lateinit var mMap: GoogleMap //guarda referencia al mapa
    private lateinit var fusedLocationClient: FusedLocationProviderClient //es quien me da la ubicacion actual del disp
    private lateinit var txtDireccion: TextView //donde quiero que se vea la direccion





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        // Inicializar TextView de dirección
        txtDireccion = view.findViewById(R.id.txtDireccion)
        viewModel.direccion.observe(viewLifecycleOwner) { direccion ->
            txtDireccion.text = direccion
        }

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inicializar el SupportMapFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }



    //lo hice para cargar el mapa y que se guarde en mMap y pedir los permisos de ubicacion para usarlo
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        pedirPermisos()
        //boton de "mi ubicacion"
        mMap.uiSettings.isMyLocationButtonEnabled = true
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
                    txtDireccion.text = direccion
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