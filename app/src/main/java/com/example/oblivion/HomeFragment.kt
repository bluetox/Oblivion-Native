package com.example.oblivion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.let
import android.net.Uri
import android.util.Log
import android.widget.Toast


data class ChatExport(
    val dest_id_b64: String,
    val name: String
)
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsIcon = view.findViewById<View>(R.id.chatIcon) // still ImageView in XML
        settingsIcon.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, MainSettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        val chatList = view.findViewById<LinearLayout>(R.id.chatList)

        val mainActivity = requireActivity() as? MainActivity
        mainActivity?.pendingDeepLink?.let { uri: Uri ->
            // Clear immediately
            mainActivity.pendingDeepLink = null
            Log.d("DeepLink", "DeepLink Found")
            if (uri.scheme == "oblivion" && uri.host == "add") {
                val base64UserId = uri.lastPathSegment
                Log.d("DeepLink", "Scheme matches $base64UserId")
                if (base64UserId != null) {
                    try {
                        val res = RustBridge.createChat("base64UserId", "DeeplinkChat")
                        Log.d("RUST", "CreatedChat status: $res")
                    } catch (e: IllegalArgumentException) {
                        Log.d("DeepLink", "Invalid base64 in link: $base64UserId", e)

                    }
                }
            }
        }

        val json: String = RustBridge.getChats()
        val avatarBackgrounds = listOf(
            R.drawable.avatar_background,
        )
        // Parse JSON into Kotlin objects
        val type = object : com.google.gson.reflect.TypeToken<List<ChatExport>>() {}.type
        val chats: List<ChatExport> = com.google.gson.Gson().fromJson(json, type)
        for (chat in chats) {
            val chatItem = layoutInflater.inflate(R.layout.chat_item, chatList, false)

            chatItem.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, ChatFragment())
                    .addToBackStack(null)
                    .commit()
            }

            chatItem.findViewById<TextView>(R.id.chatName).text = chat.name
            chatItem.findViewById<TextView>(R.id.chatMessage).text = "No messages yet"  // placeholder
            chatItem.findViewById<TextView>(R.id.chatTime).text = "" // or format last message time

            // Avatar
            val avatarContainer = chatItem.findViewById<FrameLayout>(R.id.chatAvatar)
            val avatarLetter = chatItem.findViewById<TextView>(R.id.chatAvatarLetter)
            avatarLetter.text = chat.name.first().uppercase()
            avatarContainer.setBackgroundResource(avatarBackgrounds.random())

            chatList.addView(chatItem)

        }
    }
}
