package com.example.oblivion
import android.util.Base64

data class Profile(
    val user_id: String,   // base64 encoded
    val username: String,
    val seed: String,      // base64 encoded
    val pwd_hash: String,  // base64 encoded
    val created_at: String
) {
}
object RustBridge {
    external fun init(dbPath: String, callback: RustBridgeCallback)
    external fun createProfile(password: String, username: String)
    external fun getProfiles(): String
    external fun loadWithProfile(user_id: String, password: String): Int

}