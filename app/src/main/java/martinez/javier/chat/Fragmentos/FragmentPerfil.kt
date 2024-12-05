package martinez.javier.chat.Fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import martinez.javier.chat.CambiarPasswordActivity
import martinez.javier.chat.Constantes
import martinez.javier.chat.EditarInformacionActivity
import martinez.javier.chat.OpcionesLoginActivity
import martinez.javier.chat.R
import martinez.javier.chat.databinding.FragmentPerfilBinding


class FragmentPerfil : Fragment() {

    private lateinit var binding: FragmentPerfilBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPerfilBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        cargarInformacion()

        binding.btnActualizarInfo.setOnClickListener {
            //mContext = de este fragmento a....
            startActivity(Intent(mContext, EditarInformacionActivity::class.java))
        }

        binding.btnCambiarPass.setOnClickListener {
            startActivity(Intent(mContext, CambiarPasswordActivity::class.java))
        }

        binding.btnCerrarsesion.setOnClickListener {
            firebaseAuth.signOut() //Cerrar sesión
            startActivity(Intent(mContext, OpcionesLoginActivity::class.java))
            activity?.finishAffinity()
        }
    }

    private fun cargarInformacion() {
        val uid = firebaseAuth.uid!!
        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        val refNoUsuarios = FirebaseDatabase.getInstance().getReference("NoUsuarios")

        refUsuarios.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    mostrarInformacion(snapshot)
                } else {
                    refNoUsuarios.child(uid).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                mostrarInformacion(snapshot)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(mContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(mContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarInformacion(snapshot: DataSnapshot) {
        val nombres = "${snapshot.child("nombres").value}"
        val email = "${snapshot.child("email").value}"
        val proveedor = "${snapshot.child("proveedor").value}"
        var t_registro = "${snapshot.child("tiempoR").value}"
        val imagen = "${snapshot.child("imagen").value}"

        if (t_registro == "null") {
            t_registro = "0"
        }

        // Conversión a fecha
        val fecha = Constantes.formatoFecha(t_registro.toLong())

        // Poner la información en las vistas o sea en los TextViews
        binding.tvNombres.text = nombres
        binding.tvEmail.text = email
        binding.tvProveedor.text = proveedor
        binding.tvTRegistro.text = fecha

        // Poner la imagen en el ImageView (Iv)
        try {
            Glide.with(mContext.applicationContext)
                .load(imagen)
                .placeholder(R.drawable.ic_img_perfil) // Se muestra esta imagen mientras se carga la otra (Servidor)
                .into(binding.ivPerfil)
        } catch (e: Exception) {
            Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
        }

        // Eliminar si se decide no ingresar por email
        if (proveedor == "Email") {
            binding.btnCambiarPass.visibility = View.VISIBLE
        }
    }
}