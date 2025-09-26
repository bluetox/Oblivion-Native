package com.example.oblivion

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.EditText
import android.widget.Toast


/*
    This class is the main fragment page for the Add chat page.
    On create view it loads the "fragment_new_chat"

    Then it adds a click listener to the go back arrow to return to Home

    And then adds also a listener to the add contact button which will
    ask for a userId a username (if not provided the chat will be called
    by the userId) and then sends it to the Rust Side

    If the data was valid then redirect to Home where the chat should
    be loaded.


 */
class AddChatFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_new_chat, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val goBackArrow = view.findViewById<ImageView>(R.id.arrowBackIcon)
        goBackArrow.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_right,

                    R.anim.slide_out_left,
                )
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
        val addContactButton = view.findViewById<LinearLayout>(R.id.addContactButton)
        addContactButton.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val sheetView = layoutInflater.inflate(R.layout.bottomsheet_new_contact, null)
            bottomSheetDialog.setContentView(sheetView)
            bottomSheetDialog.show()

            // Récupère les références des vues à partir de sheetView
            val usernameEditText = sheetView.findViewById<EditText>(R.id.username)
            val userIdEditText = sheetView.findViewById<EditText>(R.id.userId)
            val createContact = sheetView.findViewById<Button>(R.id.createContact)

            createContact.setOnClickListener {
                // Récupère le texte entré par l’utilisateur
                val username = usernameEditText.text.toString().trim()
                val base64UserId = userIdEditText.text.toString().trim()

                // Ici tu peux utiliser username et userId comme tu veux
                // Par exemple :
                if (base64UserId.isNotEmpty()) {
                    // Traiter les données (ex: ajouter le contact à une liste ou une base de données)
                    val decodedUserId = String(android.util.Base64.decode(base64UserId, android.util.Base64.DEFAULT))
                    Log.d("DeepLink", "Calling native")
                    val res = RustBridge.createChat(base64UserId, username)
                    bottomSheetDialog.dismiss() // Fermer la fenêtre si besoin
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right,
                            R.anim.slide_in_right,

                            R.anim.slide_out_left,
                        )
                        .replace(R.id.fragment_container, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "UserID requis", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
