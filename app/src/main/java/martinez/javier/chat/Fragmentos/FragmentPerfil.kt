package martinez.javier.chat.Fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import martinez.javier.chat.OpcionesLoginActivity
import martinez.javier.chat.R
import martinez.javier.chat.databinding.FragmentPerfilBinding


class FragmentPerfil : Fragment() {

    private lateinit var binding : FragmentPerfilBinding
    private lateinit var mContext : Context
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPerfilBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    //Para realizar instancia de firebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnCerrarsesion.setOnClickListener {
            firebaseAuth.signOut() //Cerrar sesion
            startActivity(Intent(mContext, OpcionesLoginActivity::class.java))
            activity?.finishAffinity()
        }
    }

}