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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import martinez.javier.chat.Fragmentos.FragmentChats
import martinez.javier.chat.Fragmentos.FragmentPerfil
import martinez.javier.chat.Fragmentos.FragmentUsuarios
import martinez.javier.chat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private lateinit var biding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        biding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(biding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        comprobarSesion()

        //Fragmento por defecto
        verFragmentosUsuarios()

        biding.bottomNV.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_perfil->{
                    //Visualizar el fragmento Perfil
                    verFragmentoPerfil()
                    true
                }
                R.id.item_usuarios->{
                    //Visualizar el fragmento Usuarios
                    verFragmentosUsuarios()
                    true
                }
                R.id.item_chats->{
                    //Visualizar el fragmento Chats
                    verFragmentosChats()
                    true
                }
                else->{
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
    //Permite redirigir e3l mqinqdtivity hacia la actividad de login en el caso de que el usuario sea igual a null
    //Comprobar si el usuario esta logueado o sea igual null
    //Si el usuario no esta logueado, no va aceder al mainactivitu
    //Sino que lo va a dirigir a OpcionesLoginActivity
    private fun comprobarSesion() {
        //Si el usuario no esta logueado, no va aceder al mainactivitu, va a "OpcionesLoginActivity"
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
            finishAffinity() //La actividad maainactivity se cierra
        //Pero si el usuario esta conectado se agregara automaticamente el token
        }else{
            agregarToken()
            solicitarNotificacion()
        }

    }

    private fun verFragmentoPerfil(){
        biding.tvTitulo.text = "Perfil"

        val fragment = FragmentPerfil()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(biding.fragmentoFL.id, fragment, "Fragment Perfil")
        fragmentTransaction.commit()
    }

    private fun verFragmentosUsuarios(){
        biding.tvTitulo.text = "Usuarios"

        val fragment = FragmentUsuarios()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(biding.fragmentoFL.id, fragment, "Fragment Usuarios")
        fragmentTransaction.commit()
    }

    private fun verFragmentosChats(){
        biding.tvTitulo.text = "Chats"

        val fragment = FragmentChats()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(biding.fragmentoFL.id, fragment, "Fragment Chats")
        fragmentTransaction.commit()
    }

    //Funcion para actualizar estado "Online" u "Offline
    private fun actualizarEstado(estado : String){
        //Referencia a la BD "Usuarios" y el uid del usuario actual
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseAuth.uid!!)
        val hashMap = HashMap<String,Any>()
        hashMap["estado"] = estado
        ref!!.updateChildren(hashMap)
    }
    //Si se esta dentro de la app = ONLINE
    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser!=null){
            actualizarEstado("Online")
        }

    }
    //Si se esta fuera de la app = OFFLINE
    override fun onPause() {
        super.onPause()
        if (firebaseAuth.currentUser!=null){
            actualizarEstado("Offline")
        }

    }
    //Obtener token cuando esteme en el apartado principal
    private fun agregarToken(){
        val miUid = "${firebaseAuth.uid}"
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcmToken->
                //Hashmap para poder actualiza en tiempo real el nuevo dato en el usuario actual
                val hashMap = HashMap<String,Any>()
                hashMap["fcmToken"] = "${fcmToken}"
                //Referencia a la BD
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
                ref.child(miUid)
                    .updateChildren(hashMap)
                    //EL token se agrego correctamente
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {e->
                        Toast.makeText(this,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


            }
            .addOnFailureListener {e->
                Toast.makeText(this,
                    "${e.message}",
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