package martinez.javier.chat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import martinez.javier.chat.Chat.ChatActivity

class AdaptadorUsuario(//Implementar miembros
    //Parametros
    context : Context,
    //Modelo Usuario
    listaUsuarios : List<Usuario>) : RecyclerView.Adapter<AdaptadorUsuario.ViewHolder?>() {

        private val context : Context
        private val listaUsuarios : List<Usuario>//Modelo Usuario

        init {
            this.context = context
            this.listaUsuarios = listaUsuarios
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Conexion del adaptador con el diseño item_usuario
        val view : View = LayoutInflater.from(context).inflate(R.layout.item_usuario,parent,false)//Ruta donde se encuentra el diseño
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        //Obtener el tamaño total de la lista de usuarios
        return listaUsuarios.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Obtener y mostrar informacion dentro de las vistas (dentro de los texview y el imageview)
        val usuario : Usuario = listaUsuarios[position]
        holder.uid.text = usuario.uid
        holder.email.text = usuario.email
        holder.nombres.text = usuario.nombres
        Glide.with(context).load(usuario.imagen).placeholder(R.drawable.ic_imagen_perfil).into(holder.imagen)

        //Para al sleccionar un usuario se vaya a chat
        holder.itemView.setOnClickListener {
            //item que permite dirigir de este adaptador a chatActivity
            val intent = Intent(context, ChatActivity::class.java)
            //Se pasa como parametro el uid del usuario
            intent.putExtra("uid", holder.uid.text)
            //Comprobacion
            Toast.makeText(context, "Se ha seleccionado al usuario ${holder.nombres.text}", Toast.LENGTH_SHORT).show()
            context.startActivity(intent)
        }
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        //Declarar vistas del diseño item_usuario para posteriormente inicializarlas
        var uid : TextView
        var email : TextView
        var nombres : TextView
        var imagen : ImageView

        init {
            uid = itemView.findViewById(R.id.item_uid)
            email = itemView.findViewById(R.id.item_email)
            nombres = itemView.findViewById(R.id.item_nombre)
            imagen = itemView.findViewById(R.id.item_imagen)
        }
    }
}