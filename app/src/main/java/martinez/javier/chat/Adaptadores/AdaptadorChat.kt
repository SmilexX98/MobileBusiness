package martinez.javier.chat.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import martinez.javier.chat.Constantes
import martinez.javier.chat.Modelos.Chat
import martinez.javier.chat.R

class AdaptadorChat : RecyclerView.Adapter<AdaptadorChat.HolderChat> {

    private val context : Context
    private val charArray : ArrayList<Chat>
    private val firebaseAuth : FirebaseAuth

    companion object{
        private const val MENSAJE_IZQUIERDA = 0 //Referencia a los mensaje recibidos
        private const val MENSAJE_DERECHA = 1 //Referencia a los mensaje enviados
    }

    constructor(context: Context, charArray: ArrayList<Chat>) {
        this.context = context
        this.charArray = charArray
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        //Cuando se reconosca que el tipo de vista sea derecha, se mostrara el "item_chat_derecha" si no, el "item_chat_izquierda"
        if (viewType == MENSAJE_DERECHA){
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_derecha,parent,false)
            return HolderChat(view)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_izquierda,parent,false)
            return HolderChat(view)
        }
    }

    override fun getItemCount(): Int {
        return charArray.size//Obtener el tamaño de la lista
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modeloChat = charArray[position]
        //Variables para almacenar la infromacion del modelo
        val mensaje = modeloChat.mensaje
        val tipoMensaje = modeloChat.tipoMensaje
        val tiempo = modeloChat.tiempo

        val formato_fecha_hora = Constantes.obtenerFechaHora(tiempo)
        holder.tv_tiempo_mensaje.text = formato_fecha_hora
        //Si el tipo de mensaje es texto
        if (tipoMensaje == Constantes.MENSAJE_TEXTO){
            holder.tv_mensaje.visibility = View.VISIBLE
            holder.Iv_mensaje.visibility = View.GONE//Si no es tipo texto se oculta la imagen (ImageView)
            holder.tv_mensaje.text = mensaje//Mostrar mensaje
        }else{//Mensaje tipo imagen
            holder.tv_mensaje.visibility = View.GONE
            holder.Iv_mensaje.visibility = View.VISIBLE

            try {
                Glide.with(context)
                    .load(mensaje)
                    .placeholder(R.drawable.imagen_enviada)
                    .error(R.drawable.imagen_chat_falla)
                    .into(holder.Iv_mensaje)
            }catch (e:Exception){

            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        //Verificar si uno mismo es el receptor o el emisor
        if (charArray[position].emisorUid == firebaseAuth.uid){
            //Si el emisor recibido de la BD es igual al uid del usuario actual == EMISOR
            return MENSAJE_DERECHA
        }else{//Si el emisor del mensaje no es el mismo del usuario actual == RECEPTOR
            return MENSAJE_IZQUIERDA
        }
    }


    inner class HolderChat(itemView : View) : RecyclerView.ViewHolder(itemView){
        //Inicializar vistas de "item_chat_derecha" e "item_chat_izquierda"
        //Pero como tienen las mismas vistas solo inicializaremos una
        var tv_mensaje : TextView = itemView.findViewById(R.id.tv_mensaje)
        var Iv_mensaje : ShapeableImageView = itemView.findViewById(R.id.Iv_mensaje)
        var tv_tiempo_mensaje : TextView = itemView.findViewById(R.id.tv_tiempo_mensaje)
    }



}