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
import android.content.Intent
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
    var pendingDeepLink: android.net.Uri? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
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
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private var listener: MessageListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askNotificationPermission()
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "Token: $token")
                Toast.makeText(this, "Token: $token", Toast.LENGTH_SHORT).show()

            }
        val callback = object : RustBridgeCallback {
            override fun onNewMessage(message: String) {
                runOnUiThread {
                    listener?.onMessage(message)
                }
            }
        }
        val dbPath = this.getDatabasePath("storage.db").absolutePath
        val res = RustBridge.init(dbPath, callback)
        Log.d("RUST", "Init returned: $res")
        loadFragment(ProfileMenuFragment())
        handleIntent(intent)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                pendingDeepLink = uri
            }
        }
    }

    fun setMessageListener(l: MessageListener?) {
        listener = l
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }
}
