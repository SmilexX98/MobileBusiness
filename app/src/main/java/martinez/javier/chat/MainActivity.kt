package martinez.javier.chat

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import martinez.javier.chat.Fragmentos.FragmentChats
import martinez.javier.chat.Fragmentos.FragmentPerfil
import martinez.javier.chat.Fragmentos.FragmentUsuarios
import martinez.javier.chat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        comprobarSesion()

        // Fragmento default
        verFagmentoUsuarios()

        binding.bottomNV.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_perfil -> {
                    // Visualizar el fragmento Perfil
                    verFagmentoPerfil()
                    true
                }
                R.id.item_usuarios -> {
                    // Visualizar el fragmento Usuarios
                    verFagmentoUsuarios()
                    true
                }
                R.id.item_chats -> {
                    // Visualizar el fragmento Chats
                    verFagmentoChats()
                    true
                }
                else -> {
                    false
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun comprobarSesion() {
        // Si el usuario no esta logueado, no va a acceder a MainActivity, va a "OpcionesLoginActivity"
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
            finishAffinity() // La actividad MainActivity se cierra
        } else {
            agregarToken()
            solicitarNotificacion()
        }
    }

    private fun verFagmentoPerfil() {
        binding.tvTitulo.text = "Perfil"

        val fragment = FragmentPerfil()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment, "Fragment Perfil")
        fragmentTransaction.commit()
    }

    private fun verFagmentoUsuarios() {
        binding.tvTitulo.text = "Usuarios"

        val fragment = FragmentUsuarios()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment, "Fragment Usuarios")
        fragmentTransaction.commit()
    }

    private fun verFagmentoChats() {
        binding.tvTitulo.text = "Chats"

        val fragment = FragmentChats()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment, "Fragment Chats")
        fragmentTransaction.commit()
    }

    // Funcion para actualizar estado "Online" u "Offline"
    private fun actualizarEstado(estado: String) {
        val uid = firebaseAuth.uid ?: return
        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        val refNoUsuarios = FirebaseDatabase.getInstance().getReference("NoUsuarios")
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = estado

        refUsuarios.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    refUsuarios.child(uid).updateChildren(hashMap)
                } else {
                    refNoUsuarios.child(uid).updateChildren(hashMap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo del error si es necesario
            }
        })
    }

    // Si se esta dentro de la app = ONLINE
    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser != null) {
            actualizarEstado("Online")
        }
    }

    // Si se esta fuera de la app = OFFLINE
    override fun onPause() {
        super.onPause()
        if (firebaseAuth.currentUser != null) {
            actualizarEstado("Offline")
        }
    }

    // Obtener token cuando estemos en el apartado principal
    private fun agregarToken() {
        val miUid = firebaseAuth.uid ?: return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcmToken ->
                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = fcmToken

                val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
                val refNoUsuarios = FirebaseDatabase.getInstance().getReference("NoUsuarios")

                refUsuarios.child(miUid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Actualizar token en "Usuarios"
                            refUsuarios.child(miUid).updateChildren(hashMap)
                        } else {
                            // Actualizar token en "NoUsuarios"
                            refNoUsuarios.child(miUid).updateChildren(hashMap)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error al verificar el usuario: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al obtener el token: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun solicitarNotificacion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Si el permiso en primera instancia es denegado, o sea el USUARIO abre la aplicacion por primera vez
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED){
                darPermiso.launch(android.Manifest.permission.POST_NOTIFICATIONS)

            }

        }

    }
    private val darPermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){esConcedido->
            //El permiso fue aceptado
        }



}
