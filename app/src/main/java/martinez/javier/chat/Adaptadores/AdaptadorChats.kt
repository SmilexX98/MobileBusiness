package martinez.javier.chat.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import martinez.javier.chat.Chat.ChatActivity
import martinez.javier.chat.Constantes
import martinez.javier.chat.Modelos.Chats
import martinez.javier.chat.R
import martinez.javier.chat.databinding.ItemChatsBinding

class AdaptadorChats : RecyclerView.Adapter<AdaptadorChats.HolderChats>/*, Filterable*/ {

    private var context : Context
    private var chatArrayList : ArrayList<Chats>//Conversacion en general que tienen el emisor con los receptores
    private lateinit var binding : ItemChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var miUid = ""

    //private var filtroLista : ArrayList<Chats>
    //private var filtro : BuscarChat? = null

    constructor(context: Context, chatArrayList: ArrayList<Chats>) {
        this.context = context
        this.chatArrayList = chatArrayList
        //this.filtroLista = chatArrayList
        firebaseAuth = FirebaseAuth.getInstance()//Crear instancia firebase
        miUid = firebaseAuth.uid!!//Uid del usuario actual
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChats {
        //Inflar nuestra vista
        binding = ItemChatsBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderChats(binding.root)
    }

    override fun getItemCount(): Int {
        //Obtener tamaño de la lista
        return chatArrayList.size
    }

    override fun onBindViewHolder(holder: HolderChats, position: Int) {
        val modeloChats = chatArrayList[position]//Obtener la información de la BD en el objeto chats

        //Función
        cargarUltimoMensaje(modeloChats, holder)
        //Evento para que al seleccionar un usuario de la lista sea dirigido a la actividad ChatActivity
        holder.itemView.setOnClickListener {
            val uidRecibimos = modeloChats.uidRecibimos
            if (uidRecibimos != null) {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("uid", uidRecibimos)
                context.startActivity(intent)
            }
        }
    }

    private fun cargarUltimoMensaje(modeloChats: Chats, holder: AdaptadorChats.HolderChats) {
        //Referencia a la obtención de la concatenación del UID emisor con el receptor o viceversa
        val chatKey = modeloChats.keyChat
        //Referencia a la BD
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)//propiedad para poder ver el último mensaje
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Recorrido a la BD
                    for (ds in snapshot.children) {
                        //Obtener información de firebase
                        val emisorUid = "${ds.child("emisorUid").value}"
                        val idMensaje = "${ds.child("idMensaje").value}"
                        val mensaje = "${ds.child("mensaje").value}"
                        val receptorUid = "${ds.child("receptorUid").value}"
                        val tiempo = ds.child("tiempo").value as Long
                        val tipoMensaje = "${ds.child("tipoMensaje").value}"
                        val formatoFechaHora = Constantes.obtenerFechaHora(tiempo)//Convertir a string el tiempo
                        //Poner la información en el modelo chats
                        modeloChats.emisorUid = emisorUid
                        modeloChats.idMensaje = idMensaje
                        modeloChats.mensaje = mensaje
                        modeloChats.receptorUid = receptorUid
                        modeloChats.tipoMensaje = tipoMensaje
                        //De item_chats
                        holder.tvFecha.text = "$formatoFechaHora"
                        //Verificar qué tipo de mensaje es
                        if (tipoMensaje == Constantes.MENSAJE_TEXTO) {
                            holder.tvUltimoMensaje.text = mensaje
                        } else {
                            holder.tvUltimoMensaje.text = "Imagen enviada"
                        }
                        cargarInfoUsuarioRecibido(modeloChats, holder)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun cargarInfoUsuarioRecibido(modeloChats: Chats, holder: AdaptadorChats.HolderChats) {
        //Variables para almacenar al receptor y emisor
        val receptorUid = modeloChats.receptorUid
        val emisorUid = modeloChats.emisorUid

        var uidRecibimos = ""
        //Si el emisorUid del mensaje que se lee actualmente == al usuario actual entonces se recibe el uid del receptor, sino se recibe el emisor (uno mismo)
        if (emisorUid == miUid) {
            uidRecibimos = receptorUid
        } else {
            uidRecibimos = emisorUid
        }
        modeloChats.uidRecibimos = uidRecibimos

        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        val refNoUsuarios = FirebaseDatabase.getInstance().getReference("NoUsuarios")

        refUsuarios.child(uidRecibimos).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    mostrarInfoUsuario(snapshot, modeloChats, holder)
                } else {
                    refNoUsuarios.child(uidRecibimos).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                mostrarInfoUsuario(snapshot, modeloChats, holder)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Manejo del error si es necesario
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo del error si es necesario
            }
        })
    }

    private fun mostrarInfoUsuario(snapshot: DataSnapshot, modeloChats: Chats, holder: AdaptadorChats.HolderChats) {
        //Obtener nombre e imagen del usuario
        val nombres = "${snapshot.child("nombres").value}"
        val imagen = "${snapshot.child("imagen").value}"

        modeloChats.nombres = nombres
        modeloChats.imagen = imagen
        //Poner los nombres y la imagen
        holder.tvNombres.text = nombres
        try {
            Glide.with(context.applicationContext)
                .load(imagen)
                .placeholder(R.drawable.ic_imagen_perfil)
                .into(holder.IvPerfil)
        } catch (e: Exception) {
            // Manejo del error si es necesario
        }
    }

    inner class HolderChats(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Declarar las vistas del item_chats
        var IvPerfil = binding.IvPerfil
        var tvNombres = binding.tvNombres
        var tvUltimoMensaje = binding.tvUltimoMensaje
        var tvFecha = binding.tvFecha
    }

    /*override fun getFilter(): Filter {
        if (filtro == null){
            filtro = BuscarChat(this, filtroLista)
        }
        return filtro!!
    }*/
}
