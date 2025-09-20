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
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trabajo1.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.database.FirebaseDatabase
import androidx.core.net.toUri
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager
import com.google.firebase.database.DatabaseException
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // Variables para la cámara
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false
    // Variables para la autenticación biométrica
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aplicar tamaño de letra elegido
        aplicarTamanoLetra()
        // Se configura el binding y se infla el layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializamos los componentes de la autenticación
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    // Maneja el error de autenticación (ej: usuario cancela, no hay hardware)
                    Toast.makeText(applicationContext,
                        "Autenticación con error: $errString", Toast.LENGTH_SHORT).show()
                    finish() // Cierra la app por seguridad
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    // Autenticación exitosa
                    Toast.makeText(applicationContext,
                        "¡Autenticación exitosa!", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // La huella no coincide, se mantiene el diálogo abierto para reintentar
                    Toast.makeText(applicationContext, "Autenticación fallida.", Toast.LENGTH_SHORT).show()
                }
            })
        // Se configura el cuadro de diálogo de la huella digital
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación necesaria")
            .setSubtitle("Usa tu huella digital para acceder a la aplicación")
            .setDescription("Coloca tu dedo en el sensor para verificar tu identidad.")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        // Verificamos si la biometría está activada en configuraciones
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val biometricEnabled = prefs.getBoolean("biometric_enabled", false)

        if (biometricEnabled) {
            biometricPrompt.authenticate(promptInfo)
        }

        // Esto oculta la toolbar por defecto, ya que nosotros usamos una toolbar personalizada.
        supportActionBar?.hide()
        // Usar binding.root.findViewById() para acceder al Toolbar dentro del layout incluido
        val toolbar: Toolbar = binding.root.findViewById(R.id.customToolbar)
        setSupportActionBar(toolbar)

        //codigo para iniciar el reloj
        iniciarReloj()

        //codigo para iniciar el nivel de bateria y realizar el calculo aproximado
        registrarBateria()

        //codigo para la linterna
        //Inicializar CameraManager
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

        //codigo para el botón de llamar
        val btnPhone: ImageButton = findViewById(R.id.iconPhone)
        val nroConsejo = "+5402302123456"
        btnPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = "tel:$nroConsejo".toUri()
            startActivity(intent)
        }

        //Barra de navegacion inferior:
        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_inicio,
                R.id.navigation_chat,
                R.id.navigation_precios,
                R.id.navigation_mas
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_inicio -> getString(R.string.title_inicio)
                R.id.navigation_chat -> getString(R.string.title_chat)
                R.id.navigation_precios -> getString(R.string.title_precios)
                R.id.navigation_mas -> getString(R.string.title_mas)
            }
        }

        // Firebase
        try {
            // Esto permite que Firebase pueda funcionar offline.
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: DatabaseException) {
            //Se ignora la excepción de forma segura cuando se hace un recreate() al cambiar el tamaño de la letra de la app-
        }

        // Fin del onCreate.
    }

    //Código para la linterna: Funciones para encender y apagar la linterna
    private fun encenderFlash(){
        cameraId?.let{
            cameraManager.setTorchMode(it, true)
            isFlashOn = true
        }
    }
    private fun apagarFlash(){
        cameraId?.let {
            cameraManager.setTorchMode(it, false)
            isFlashOn = false
        }
    }

    // Código para el reloj
    private val handler = android.os.Handler(Looper.getMainLooper())
    // Funcion para iniciar la hora y luego actualizarla cada un minuto
    private fun iniciarReloj(){
        actualizarHora()

        val ahora =System.currentTimeMillis()
        val retraso = 60000 - (ahora % 60000) //tiempo que falta para el proximo minuto

        handler.postDelayed(object : Runnable{
            override fun run(){
                actualizarHora()
                handler.postDelayed(this, 60000) //cada minuto exacto
            }
        }, retraso)
    }
    // Funcion para actualizar el reloj
    private fun actualizarHora(){
        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        findViewById<TextView>(R.id.txtHora).text = horaActual
    }

    // Código para la bateria: Funcion para obtener la duración de la bateria y calcular su duración aproximada
    private fun registrarBateria(){
        val batteryStatus: Intent? = registerReceiver(
           null,
           IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        batteryStatus?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val porcentaje = (level * 100 / scale.toFloat()).toInt()

            // Mostrar porcentaje
            findViewById<TextView>(R.id.txtBatteryPercentage).text = getString(R.string.battery_percentage, porcentaje)

            // Calcular duración aproximada (muy estimativa, ej: 5 horas con 100%)
            val horasEstimadas = (porcentaje / 20) // ejemplo: 100% ≈ 5 horas
            findViewById<ImageView>(R.id.iconBattery).setOnClickListener {
                Toast.makeText(this, "Duración estimada de batería: $horasEstimadas horas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun aplicarTamanoLetra() {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val fontSizePref = prefs.getString("font_size", "medium")

        val config = resources.configuration
        val escala = when (fontSizePref) {
            "small" -> 0.85f
            "medium" -> 1.0f
            "large" -> 1.15f
            else -> 1.0f
        }

        config.fontScale = escala
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFlashOn) {
            apagarFlash()
        }
    }
}
