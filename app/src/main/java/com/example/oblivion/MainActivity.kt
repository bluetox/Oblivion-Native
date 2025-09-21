package com.example.oblivion

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

interface RustBridgeCallback {
    fun onNewMessage(message: String)
}
interface MessageListener {
    fun onMessage(message: String)
}
class MainActivity : AppCompatActivity() {
    init {
        System.loadLibrary("Oblivion")
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // FCM SDK (and your app) can post notifications
                Toast.makeText(this, "Notifications enabled ✅", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications disabled ❌", Toast.LENGTH_SHORT).show()
            }
        }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // TODO: show educational UI here before calling requestPermissionLauncher.launch()
                }
                else -> {
                    // Directly request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private var chatFragment: ChatFragment? = null

    private var listener: MessageListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(HomeFragment())

        askNotificationPermission()
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get the new FCM registration token
                val token = task.result

                // Log and toast
                Log.d("FCM", "Token: $token")
                Toast.makeText(this, "Token: $token", Toast.LENGTH_SHORT).show()

                // TODO: Send token to your server if needed
            }
        val callback = object : RustBridgeCallback {
            override fun onNewMessage(message: String) {
                runOnUiThread {
                    Log.i("RustBridge", "New message: $message")
                    listener?.onMessage(message)
                }
            }
        }

        //RustBridge.init(callback)
    }

    fun setMessageListener(l: MessageListener?) {
        listener = l
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }
}
