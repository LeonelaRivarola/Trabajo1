package com.example.trabajo1.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trabajo1.R

class ChatFragment : Fragment() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var inputMessage: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var adapter: ChatAdapter

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerChat = view.findViewById(R.id.recyclerChat)
        inputMessage = view.findViewById(R.id.inputMessage)
        sendButton = view.findViewById(R.id.sendButton)

        adapter = ChatAdapter(mutableListOf())
        recyclerChat.layoutManager = LinearLayoutManager(requireContext())
        recyclerChat.adapter = adapter

        //observamos los mensajes del viewModel
        chatViewModel.mensajes.observe(viewLifecycleOwner) { lista ->
            adapter.updateMensajes(lista)
            recyclerChat.scrollToPosition(lista.size - 1)
        }

        sendButton.setOnClickListener {
            val texto = inputMessage.text.toString().trim()
            if (texto.isNotEmpty()) {
                chatViewModel.enviarMensaje(texto)
                inputMessage.text.clear()
            }
        }

        return view
    }

    //Adapter interno (podes sacarlo a archivo aparte)
    class ChatAdapter(private val mensajes: MutableList<ChatViewModel.Mensaje>):
        RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

            inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val textMensaje: TextView = itemView.findViewById(R.id.textMensaje)
            }

        override fun getItemViewType(position: Int): Int {
            return if (mensajes[position].remitente == "usuario") 0 else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val layout = if (viewType == 0) R.layout.item_mensaje_usuario else R.layout.item_mensaje_admin
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val mensaje = mensajes[position]
            holder.textMensaje.text = mensaje.texto

            val layoutParams = holder.textMensaje.layoutParams as ViewGroup.MarginLayoutParams

            if (mensaje.remitente == "usuario") {
                holder.textMensaje.setBackgroundResource(R.drawable.bg_mensaje)
                (holder.textMensaje.layoutParams as ViewGroup.MarginLayoutParams).apply{
                    marginStart = 100
                    marginEnd = 0
                }
            } else {
                holder.textMensaje.setBackgroundResource(R.drawable.bg_mensaje_admin)
                (holder.textMensaje.layoutParams as ViewGroup.MarginLayoutParams).apply{
                    marginStart = 0
                    marginEnd = 50
                }
            }
            holder.textMensaje.layoutParams = layoutParams
        }
        override fun getItemCount() = mensajes.size

        fun updateMensajes(nuevos: List<ChatViewModel.Mensaje>) {
            mensajes.clear()
            mensajes.addAll(nuevos)
            notifyDataSetChanged()
        }
    }
}

