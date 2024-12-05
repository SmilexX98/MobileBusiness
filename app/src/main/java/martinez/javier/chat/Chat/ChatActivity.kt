package martinez.javier.chat.Chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import martinez.javier.chat.Adaptadores.AdaptadorChat
import martinez.javier.chat.Constantes
import martinez.javier.chat.Modelos.Chat
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

    //private var miNombre = ""
    //private var recibimosToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere...")
        progressDialog.setCanceledOnTouchOutside(false)

        //Obtener dato desde el adaptador, almacenar el uid que se recibe del adaptador
        uid = intent.getStringExtra("uid")!!//se puede obtener boolean, char etc.

        miUid = firebaseAuth.uid!!
        chatRuta = Constantes.rutaChat(uid, miUid)

        //cargarMiInfo()

        binding.adjuntarFAB.setOnClickListener {
            //Verificar version android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                imagenGaleria() //No necesario conceder el perimiso
            }else {
                solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        //Funcionalidad del boton de regresar, para regresar a la actividad anterior vaya...
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //Evento
        binding.enviarFAB.setOnClickListener {
            validarMensaje()
        }

        //Llamar funcion */
        cargarInfo()
        cargarMensaje()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /*private fun cargarMiInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    miNombre = "${snapshot.child("nombres").value}"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }*/

    private fun cargarMensaje() {
        //Iniciar el arraylist
        val mensajesArrayList = ArrayList<Chat>()
        //Referencia a la BD de los chats
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatRuta)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Limpiear la lista
                    mensajesArrayList.clear()
                    //Recorrido de la BD
                    for (ds : DataSnapshot in snapshot.children){
                        try {
                            //Preparar modeo chat para recuperar toda la informacion
                            val chat = ds.getValue(Chat::class.java)
                            mensajesArrayList.add(chat!!)

                        }catch (e:Exception){

                        }
                    }
                    val adaptadorChat = AdaptadorChat(this@ChatActivity, mensajesArrayList)
                    binding.chatsRV.adapter = adaptadorChat
                    //Configuracion para ver los mensajes dede la parte inferior
                    binding.chatsRV.setHasFixedSize(true)
                    var linearLayoutManager = LinearLayoutManager(this@ChatActivity)
                    linearLayoutManager.stackFromEnd = true
                    binding.chatsRV.layoutManager = linearLayoutManager
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    private fun validarMensaje() {
        val mensaje = binding.etMensajeChat.text.toString().trim()
        val tiempo = Constantes.obtenerTiempoD()
        if (mensaje.isEmpty()){
            Toast.makeText(
                this,
                "Ingrese mensaje",
                Toast.LENGTH_SHORT
            ).show()
        }else{
            enviarMensaje(Constantes.MENSAJE_TEXTO, mensaje, tiempo)//Tipo de mensaje, mensaje(TEXTO) y tiempo
        }
    }


    private fun cargarInfo() {
        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        val refNoUsuarios = FirebaseDatabase.getInstance().getReference("NoUsuarios")

        refUsuarios.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    mostrarInfoUsuario(snapshot)
                } else {
                    refNoUsuarios.child(uid).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                mostrarInfoUsuario(snapshot)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Manejo del error si es necesario
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo del error si es necesario
            }
        })
    }

    private fun mostrarInfoUsuario(snapshot: DataSnapshot) {
        // Obtener información del usuario
        val nombres = "${snapshot.child("nombres").value}"
        val imagen = "${snapshot.child("imagen").value}"
        val estado = "${snapshot.child("estado").value}"
        // recibimosToken = "${snapshot.child("fcmToken").value}"

        // Poner información dentro de la vista Nueva
        binding.txtEstadoChat.text = estado
        // Poner información dentro de la vista
        binding.txtNombreUsuario.text = nombres

        try {
            Glide.with(applicationContext)
                .load(imagen) // Cargar imagen de firebase
                .placeholder(R.drawable.perfil_usuario)
                .into(binding.toolbarIv)
        } catch (e: Exception) {
            // Manejo del error si es necesario
        }
    }


    private fun imagenGaleria(){
        //Abrir actividad para selecionar una imagen de la galeria
        val intent  = Intent(Intent.ACTION_PICK)
        //Filtrar imagenes solamente
        intent.type = "image/*"
        resultadoGaleriaARL.launch(intent)
    }
    //Activity Result Launcher
    private val resultadoGaleriaARL=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ resultado->
            //Comprobar con resultado si la imagen a sido seleccionada correctamnet de la galeria
            if (resultado.resultCode == Activity.RESULT_OK){//Si la imagen a sido seleccionadad...
                val data = resultado.data
                imagenUri = data!!.data //Almacenar dentro del URI la imagen seleccionada
                subirImagenStorage()
            }else{
                //Si el usuario cancela la seleccion de la imagen...
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    //Solicidtud permiso de almacenamiento
    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ Concedido->
            //Habilitado el permiso¿?
            if (Concedido){
                imagenGaleria()
            }else{
                Toast.makeText(
                    this,
                    "Permiso no concedido",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    private fun subirImagenStorage(){
        progressDialog.setMessage("Subiendo imagen...")
        progressDialog.show()
        val tiempo = Constantes.obtenerTiempoD()
        //Configurar nombre y ruta de la carpeta donde almacenar las imagenes enviadas por los usuarios en firebase
        val nombreRutaImg = "ImagenesChat/$tiempo" //Practicamente tiempo = ID de las imagenes
        //Ref BD
        val storageRef = FirebaseStorage.getInstance().getReference(nombreRutaImg)
        storageRef.putFile(imagenUri!!)//En Uri se alamcena la imagen seleccionada de la galeria
            .addOnSuccessListener {taskSnapshot->
                //Obtener url de la imagen para ponerla en la BD
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                //Convertir a string esa url
                val urlImagen = uriTask.result.toString()
                if (uriTask.isSuccessful){
                    enviarMensaje(Constantes.MENSAJE_IMAGEN, urlImagen, tiempo)//Tipo de mensaje, mensaje(IMAGEN) y tiempo
                }
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "Error al enviar imagen: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
    private fun enviarMensaje(tipoMensaje: String, mensaje: String, tiempo: Long) {
        progressDialog.setMessage("Enviando mensaje...")
        progressDialog.show()
        //Referencia a la BD
        val refChat = FirebaseDatabase.getInstance().getReference("Chats")//La BD se llamara "Chats"
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String,Any>()
        hashMap["idMensaje"] = "${keyId}"
        hashMap["tipoMensaje"] = "${tipoMensaje}"
        hashMap["mensaje"] = "${mensaje}"
        hashMap["emisorUid"] = "${miUid}"
        hashMap["receptorUid"] = "${uid}"
        hashMap["tiempo"] = tiempo //Dato tipo long

        refChat.child(chatRuta)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.etMensajeChat.setText("")//Si se eniva exitosamente el mensaje se limpia el campo de texto

                /*if (tipoMensaje == Constantes.MENSAJE_TEXTO){
                    prepararNotificacion(mensaje)
                }else{
                    prepararNotificacion("Imagen enviada")
                } */
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al enviar mensaje: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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


}