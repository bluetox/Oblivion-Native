package com.example.oblivion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

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

        val chats = listOf(
            Triple("Alice", "Hey, how are you?", "14:32"),
            Triple("Bob", "See you tomorrow!", "13:10"),
            Triple("Charlie", "Typing...", "12:58")
        )

        val avatarBackgrounds = listOf(
            R.drawable.avatar_background,
        )

        for ((name, message, time) in chats) {
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

            chatItem.findViewById<TextView>(R.id.chatName).text = name
            chatItem.findViewById<TextView>(R.id.chatMessage).text = message
            chatItem.findViewById<TextView>(R.id.chatTime).text = time

            // Handle avatar with letter + random gradient
            val avatarContainer = chatItem.findViewById<FrameLayout>(R.id.chatAvatar)
            val avatarLetter = chatItem.findViewById<TextView>(R.id.chatAvatarLetter)

            avatarLetter.text = name.first().uppercase()   // "A" for Alice
            avatarContainer.setBackgroundResource(avatarBackgrounds.random())

            chatList.addView(chatItem)
        }
    }
}
