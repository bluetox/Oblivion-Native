package com.example.oblivion

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Data class for a chat message
data class Message(val text: String, val isSent: Boolean)

// RecyclerView Adapter
class ChatAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val SENT = 1
        private const val RECEIVED = 2
    }

    override fun getItemViewType(position: Int) = if (messages[position].isSent) SENT else RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == SENT)
            SentViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false))
        else
            ReceivedViewHolder(inflater.inflate(R.layout.item_message_received, parent, false))
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) holder.bind(message)
        if (holder is ReceivedViewHolder) holder.bind(message)
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text = view.findViewById<TextView>(R.id.messageText)
        fun bind(msg: Message) { text.text = msg.text }
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text = view.findViewById<TextView>(R.id.messageText)
        fun bind(msg: Message) { text.text = msg.text }
    }
}

// ChatFragment
class ChatFragment : Fragment(), MessageListener {
    private var destId: String? = null
    private lateinit var adapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        destId = arguments?.getString(ARG_DEST_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        view.findViewById<ImageView>(R.id.arrowBackIcon).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left, R.anim.slide_out_right,
                    R.anim.slide_in_right, R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.chatRecyclerView)
        adapter = ChatAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }

        // Message input
        messageInput = view.findViewById(R.id.messageEditText)
        view.findViewById<ImageView>(R.id.sendButton).setOnClickListener {
            val text = messageInput.text.toString().trim()
            val id = destId ?: return@setOnClickListener
            if (text.isNotEmpty()) {
                addMessage(text, true)
                messageInput.text.clear()

                val res = RustBridge.sendMessage(id, text)
                Log.d("RES_RUST", "$res")
                addMessage("Received: $text", false)
            }
        }

        // Register fragment as active listener
        (activity as? MainActivity)?.setMessageListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister listener when fragment is destroyed
        (activity as? MainActivity)?.setMessageListener(null)
    }

    // Called by MainActivity when Rust sends a new message
    override fun onMessage(message: String) {
        addMessage(message, false)
    }

    fun addMessage(text: String, isSent: Boolean) {
        val msg = Message(text, isSent)
        adapter.addMessage(msg)
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }
    companion object {
        private const val ARG_DEST_ID = "dest_id"

        fun newInstance(destId: String) = ChatFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_DEST_ID, destId)
            }
        }
    }
}
