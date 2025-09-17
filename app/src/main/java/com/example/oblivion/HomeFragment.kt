package com.example.oblivion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val settingsIcon = view.findViewById<ImageView>(R.id.chatIcon)
        settingsIcon.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // enter
                    R.anim.slide_out_left,  // exit
                    R.anim.slide_in_left,   // popEnter (when coming back)
                    R.anim.slide_out_right  // popExit (when going back)
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


        for ((name, message, time) in chats) {
            val chatItem = layoutInflater.inflate(R.layout.chat_item, chatList, false)
            chatItem.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,  // enter
                        R.anim.slide_out_left,  // exit
                        R.anim.slide_in_left,   // popEnter (when coming back)
                        R.anim.slide_out_right  // popExit (when going back)
                    )
                    .replace(R.id.fragment_container, ChatFragment())
                    .addToBackStack(null)
                    .commit()
            }
            chatItem.findViewById<TextView>(R.id.chatName).text = name
            chatItem.findViewById<TextView>(R.id.chatMessage).text = message
            chatItem.findViewById<TextView>(R.id.chatTime).text = time
            chatItem.findViewById<ImageView>(R.id.chatAvatar).setImageResource(R.drawable.ic_user)

            chatList.addView(chatItem)
        }
    }
}
