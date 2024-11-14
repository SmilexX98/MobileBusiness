package martinez.javier.chat.Fragmentos

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import martinez.javier.chat.Adaptadores.AdaptadorUsuario
import martinez.javier.chat.Modelos.Usuario
import martinez.javier.chat.databinding.FragmentUsuariosBinding


class FragmentUsuarios : Fragment() {

    private lateinit var binding : FragmentUsuariosBinding

    private lateinit var mContext : Context
    private var usuarioAdaptador : AdaptadorUsuario?=null
    private var usuarioLista : List<Usuario>?=null

    //Inicializar contexto
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       //Inicializacion del binding
        binding = FragmentUsuariosBinding.inflate(layoutInflater, container, false)

        binding.RVUsuarios.setHasFixedSize(true)
        binding.RVUsuarios.layoutManager = LinearLayoutManager(mContext)

        //Inicializar lista de usuarios
        usuarioLista = ArrayList()

        binding.etBuscarUsuario.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(usuario: CharSequence?, p1: Int, p2: Int, p3: Int) {//p0 por usuario
                buscarUsuarios(usuario.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })//object: implementar miembros

        listarUsuarios()

        return binding.root
    }

    private fun listarUsuarios() {
        //Obtener uid del usuario actual
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        //Referencia a la base de datos
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios").orderByChild("nombres")//Nombre de la base de datos y listarlos con respecto a su nombre
        reference.addValueEventListener(object : ValueEventListener{//object: implementar miembros
            override fun onDataChange(snapshot: DataSnapshot) {
                //Primero limpiar la lista antes de llenarla
                (usuarioLista as ArrayList<Usuario>).clear() //Modelo: Usuario
                //Si el campo de busqueda esta vacio lista todos los usuarios
                //O sea cuando deje de escribir se listaran todos los usuarios
                if (binding.etBuscarUsuario.text.toString().isEmpty()){
                    //Recorrido a la base de datos
                    for (sn in snapshot.children){
                        val usuario : Usuario? = sn.getValue(Usuario::class.java)//Obtener informacion dentro del modelo
                        //condicon para listar a todos los usuario, excepto a nosotros
                        if (!(usuario!!.uid).equals(firebaseUser)){ //Si el uid recuperado no es igual al uid del usuario actual es ahi cuando se va a llenar la lista
                            (usuarioLista as ArrayList<Usuario>).add(usuario)
                        }
                    }
                }
                usuarioAdaptador = AdaptadorUsuario(mContext, usuarioLista!!)
                binding.RVUsuarios.adapter = usuarioAdaptador
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    //SOLUCIONAR ¿?
    //Busqueda de inicio a fin, o sea buscar un usuario en especifico, usando la secuencia de primer nombre...
    private fun buscarUsuarios (usuario : String){
        //Obtener uid del usuario actual
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        //Referencia a la base de datos
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios").orderByChild("nombres")//Nombre de la base de datos y bbuscarlos con respecto a su nombre
            .startAt(usuario).endAt(usuario+"\uf8ff")
        reference.addValueEventListener(object : ValueEventListener{//object: implementar miembros
            override fun onDataChange(snapshot: DataSnapshot) {
                //Limpiar la lista
                (usuarioLista as ArrayList<Usuario>).clear()
                //Recorrido a la base de datos
                for (ss in snapshot.children){
                    val usuario : Usuario?= ss.getValue(Usuario::class.java)
                    //condicon para listar a todos los usuario, excepto a nosotros
                    if (!(usuario!!.uid).equals(firebaseUser)){
                        (usuarioLista as ArrayList<Usuario>).add(usuario)
                    }
                }
                usuarioAdaptador = AdaptadorUsuario(context!! , usuarioLista!!)
                binding.RVUsuarios.adapter = usuarioAdaptador
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}