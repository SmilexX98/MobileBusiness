package martinez.javier.chat.Chat

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import martinez.javier.chat.Constantes
import martinez.javier.chat.R
import martinez.javier.chat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    //Uid del receptor de mensajes
    private var uid = "" //Guardar uid que se pasa como parametro desde el adaptadpr

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    //Uid del emisor de mensajes (uno mismo)
    private var miUid = ""
    private var chatRuta = ""
    //Se almacenara la imagen selecionada por el usuario de la galeria
    private var imagenUri : Uri?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere...")
        progressDialog.setCanceledOnTouchOutside(false)

        //Obtener dato desde el adaptador
        uid = intent.getStringExtra("uid")!!//se puede obtener boolean, char etc.

        miUid = firebaseAuth.uid!!
        chatRuta = Constantes.rutaChat(uid, miUid)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}