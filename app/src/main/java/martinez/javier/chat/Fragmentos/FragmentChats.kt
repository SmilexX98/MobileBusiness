package martinez.javier.chat.Fragmentos

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import martinez.javier.chat.Adaptadores.AdaptadorChats
import martinez.javier.chat.Modelos.Chats
import martinez.javier.chat.R
import martinez.javier.chat.databinding.FragmentChatsBinding


class FragmentChats : Fragment() {
    private lateinit var binding : FragmentChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var miUid = ""//Almacenar usuario actual como en el adaptador
    private lateinit var chatsArrayList :ArrayList<Chats>
    private lateinit var adaptadorChats : AdaptadorChats
    private lateinit var mContext : Context
    //Inicializar contexto
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth = FirebaseAuth.getInstance()//Crear instancia firebase
        miUid = "${firebaseAuth.uid}"//Uid del usuario actual
        cargarChats()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun cargarChats() {
        chatsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //Limpiar la lista
                chatsArrayList.clear()
                //Recorrer la BD
                for (ds in snapshot.children){
                    val chatKey = "${ds.key}"
                    //Comporbar si dentro del chatKey se encuentra el UID del usuario actual
                    if (chatKey.contains(miUid)){
                        val modeloChats = Chats()
                        modeloChats.keyChat = chatKey
                        chatsArrayList.add(modeloChats)
                    }
                }
                adaptadorChats = AdaptadorChats(mContext, chatsArrayList)
                binding.chatsRV.adapter = adaptadorChats
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}