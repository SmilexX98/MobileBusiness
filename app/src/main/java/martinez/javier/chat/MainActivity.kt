package martinez.javier.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
        //Comprobar si el usuario esta logueado o sea igual null
        //Si el usuario no esta logueado, no va aceder al mainactivitu
        //Sino que lo va a dirigir a OpcionesLoginActivity
        if (firebaseAuth.currentUser == null){
            irOpcionesLogin()
        }

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
    private fun irOpcionesLogin() {
        startActivity(Intent(applicationContext, OpcionesLoginActivity::class.java))
        finishAffinity() //La actividad maainactivity se cierra
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

}