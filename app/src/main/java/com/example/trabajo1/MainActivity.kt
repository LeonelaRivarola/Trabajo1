package com.example.trabajo1

import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trabajo1.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.Handler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //variables para la linterna:
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false //estado del flash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // codigo toolbar:
        //ocultar la toolbar de android por defecto para dejar visible la nuestra que es personalizada.
        supportActionBar?.hide()
        // Configuramos la toolbar
        //setSupportActionBar(binding.customToolbar)
        //supportActionBar?.setDisplayShowTitleEnabled(false) // Oculta el título por defecto

        //código para inciar el reloj:
        iniciarReloj()

        //codigo para inciar el nivel de bateria y realizar el calculo aproximado de duracion
        registrarBateria()

        //código para la linterna:
        // Inicializar CameraManager
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.first {
            cameraManager.getCameraCharacteristics(it)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        val btnLinterna: ImageButton = findViewById(R.id.btnLinterna)
        btnLinterna.setOnClickListener {
            if (isFlashOn) {
                apagarFlash()
                btnLinterna.setImageResource(R.drawable.ic_linterna_apagada)
            } else {
                encenderFlash()
                btnLinterna.setImageResource(R.drawable.ic_linterna_encendida)
            }
        }

        //barra de navegación inferior:
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_inicio, R.id.navigation_chat, R.id.navigation_consejo, R.id.navigation_mas
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val txtFragment = findViewById<TextView>(R.id.txtFragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_inicio -> txtFragment.text = "Inicio"
                R.id.navigation_chat -> txtFragment.text = "Chat"
                R.id.navigation_consejo -> txtFragment.text = "Consejo"
                R.id.navigation_mas -> txtFragment.text = "Más"
            }
        }

    }
    //fin on create ------------------------------------------------------------------------------------------


    //funciones para controlar la linterna:
    private fun encenderFlash() {
        cameraId?.let {
            cameraManager.setTorchMode(it, true)
            isFlashOn = true
        }
    }

    private fun apagarFlash() {
        cameraId?.let {
            cameraManager.setTorchMode(it, false)
            isFlashOn = false
        }
    }
    //----------------------------------------------------------------------------------------------

    //funcion para actualizar el reloj
    private val handler = android.os.Handler(Looper.getMainLooper())
    private fun iniciarReloj() {
        actualizarHora()

        val ahora = System.currentTimeMillis()
        val retraso = 60000 - (ahora % 60000) // tiempo que falta para el próximo minuto exacto

        handler.postDelayed(object : Runnable {
            override fun run() {
                actualizarHora()
                handler.postDelayed(this, 60000) // cada minuto exacto
            }
        }, retraso)
    }
    private fun actualizarHora() {
        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        findViewById<TextView>(R.id.txtHora).text = horaActual
    }
    //------------------------------------------------------------------------------------------------


    //funcion para obtener la duraciòn de la bateria y calcular su duraciòn aproximada
    private fun registrarBateria() {
        val batteryStatus: Intent? = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        batteryStatus?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val porcentaje = (level * 100 / scale.toFloat()).toInt()

            // Mostrar porcentaje
            findViewById<TextView>(R.id.txtBatteryPercentage).text = "$porcentaje%"

            // Calcular duración aproximada (muy estimativa, ej: 5 horas con 100%)
            val horasEstimadas = (porcentaje / 20) // ejemplo: 100% ≈ 5 horas
            findViewById<ImageView>(R.id.iconBattery).setOnClickListener {
                Toast.makeText(this, "Duración estimada de batería: $horasEstimadas horas", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------



    override fun onDestroy() {
        super.onDestroy()
        if (isFlashOn) {
            apagarFlash()
        }
    }
}
