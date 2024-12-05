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

    private lateinit var binding: FragmentUsuariosBinding

    private lateinit var mContext: Context
    private var usuarioAdaptador: AdaptadorUsuario? = null
    private var usuarioLista: MutableList<Usuario> = ArrayList()

    // Inicializar contexto
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inicialización del binding
        binding = FragmentUsuariosBinding.inflate(layoutInflater, container, false)

        binding.RVUsuarios.setHasFixedSize(true)
        binding.RVUsuarios.layoutManager = LinearLayoutManager(mContext)

        // Inicializar lista de usuarios
        listarUsuarios()

        binding.etBuscarUsuario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(usuario: CharSequence?, p1: Int, p2: Int, p3: Int) {
                buscarUsuarios(usuario.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        return binding.root
    }

    private fun listarUsuarios() {
        // Obtener uid del usuario actual
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        // Referencia a la base de datos
        val reference = FirebaseDatabase.getInstance().reference.child("Usuarios").orderByChild("nombres") // Nombre de la base de datos y listarlos con respecto a su nombre
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Primero limpiar la lista antes de llenarla
                usuarioLista.clear()
                // Recorrido a la base de datos
                for (sn in snapshot.children) {
                    val usuario: Usuario? = sn.getValue(Usuario::class.java) // Obtener información dentro del modelo
                    // Condición para listar a todos los usuarios, excepto a nosotros
                    if (!(usuario!!.uid).equals(firebaseUser)) { // Si el uid recuperado no es igual al uid del usuario actual es ahí cuando se va a llenar la lista
                        usuarioLista.add(usuario)
                    }
                }
                usuarioAdaptador = AdaptadorUsuario(mContext, usuarioLista)
                binding.RVUsuarios.adapter = usuarioAdaptador
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Búsqueda local en la lista ya descargada
    private fun buscarUsuarios(texto: String) {
        val resultado = usuarioLista.filter {
            it.nombres.contains(texto, ignoreCase = true)
        }
        usuarioAdaptador = AdaptadorUsuario(mContext, resultado)
        binding.RVUsuarios.adapter = usuarioAdaptador
    }
}
