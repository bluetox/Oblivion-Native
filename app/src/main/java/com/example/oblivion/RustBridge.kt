package com.example.oblivion
import android.util.Base64

data class Profile(
    val user_id: String,
    val username: String,
    val created_at: String
) {
}
object RustBridge {
    external fun init(dbPath: String, callback: RustBridgeCallback): Int
    external fun createProfile(password: String, username: String)
    external fun getProfiles(): String
    external fun loadWithProfile(user_id: String, password: String): Int

    external fun createChat(dst_user_id: String, chat_name: String): Int
    external fun getChats(): String
    external fun getCurrentProfile(): String

    external fun sendMessage(dst_user_id: String, message: String): Int
}