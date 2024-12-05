package martinez.javier.chat

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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.FirebaseStorageKtxRegistrar
import kotlinx.coroutines.selects.whileSelect
import martinez.javier.chat.databinding.ActivityEditarInformacionBinding

class EditarInformacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarInformacionBinding //Acceso a los datos de la interfaz
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    // Almacenar imagen seleccionada de la galería
    private var imagUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarInformacionBinding.inflate(layoutInflater) //Acceso a los datos de la interfaz
        enableEdgeToEdge()
        setContentView(binding.root) //Acceso a los datos de la interfaz

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor...")
        progressDialog.setCanceledOnTouchOutside(false)

        // Lectura de la información en tiempo real
        cargarInformacion()

        // Evento para la flecha de regreso (botón)
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Evento para el botón de imagen de perfil
        binding.IvEditarImg.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Versión tiramisu o superior no se necesita obligatoriamente el permiso de almacenamiento
                abrirGaleria()
            } else { // Obligatoriamente pedir permiso (Típico mensaje de aceptar permisos)
                solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // Evento para el botón actualizar
        binding.btnActualizar.setOnClickListener {
            validarInformacion()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun abrirGaleria() {
        // Iniciar el selector de imagen
        val intent = Intent(Intent.ACTION_PICK)
        // Visualizar solo archivos tipo imagen
        intent.type = "image/*" // Al abrir los archivos de la galería solo mostrará imágenes
        galeriaARL.launch(intent)
    }

    // Para obtener la imagen seleccionada de la galería
    private var galeriaARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) { // Comprobar si la imagen fue seleccionada
                val data = resultado.data
                imagUri = data!!.data
                subirImagenStorage(imagUri)
            } else {
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun subirImagenStorage(imagUri: Uri?) {
        progressDialog.setMessage("Subiendo imagen a Storage")
        progressDialog.show()

        // Nombre y ruta de la carpeta donde se va a guardar esa imagen
        val rutaimagen = "imagenesPerfil/" + firebaseAuth.uid // Nombre de la carpeta y nombre de la imagen en sí será el uid del usuario
        val ref = FirebaseStorage.getInstance().getReference(rutaimagen)
        ref.putFile(imagUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlImagenCargada = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    actualizarInfoBD(urlImagenCargada)
                }

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun actualizarInfoBD(urlImagenCargada: String) {
        progressDialog.setMessage("Actualizando imagen")
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        if (imagUri != null) {
            hashMap["imagen"] = urlImagenCargada // Nombre del atributo de la base de datos "imagen"
        }

        val uid = firebaseAuth.uid!!
        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        val refNoUsuarios = FirebaseDatabase.getInstance().getReference("NoUsuarios")

        refUsuarios.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    refUsuarios.child(uid).updateChildren(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                "Se actualizó la imagen correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    refNoUsuarios.child(uid).updateChildren(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                "Se actualizó la imagen correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(
                    this@EditarInformacionActivity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Solicitud del permiso de almacenamiento
    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            // Si el permiso es concedido
            if (esConcedido) {
                abrirGaleria()
            } else {
                Toast.makeText(
                    this,
                    "Permiso de almacenamiento denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private var nombres = ""
    private fun validarInformacion() {
        nombres = binding.etNombres.text.toString().trim()

        if (nombres.isEmpty()) {
            binding.etNombres.error = "Ingrese sus nombres" // Aquí el dato "etNombres" que se habla en "actualizarInfo"
            binding.etNombres.requestFocus()
        } else {
            actualizarInfo()
        }
    }

    private fun actualizarInfo() {
        progressDialog.setMessage("Actualizando informacion")
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        // Dato a actualizar en firebase
        hashMap["nombres"] = nombres // Pasamos el nuevo dato que se está obteniendo al escribir en el "etNombres"

        val uid = firebaseAuth.uid!!
        val refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        val refNoUsuarios = FirebaseDatabase.getInstance().getReference("NoUsuarios")

        refUsuarios.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    refUsuarios.child(uid).updateChildren(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                "Se actualizó la información correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    refNoUsuarios.child(uid).updateChildren(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                "Se actualizó la información correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@EditarInformacionActivity,
                                e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(
                    this@EditarInformacionActivity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
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
                            Toast.makeText(
                                applicationContext,
                                error.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun mostrarInformacion(snapshot: DataSnapshot) {
        val nombres = "${snapshot.child("nombres").value}"
        val imagen = "${snapshot.child("imagen").value}"

        binding.etNombres.setText(nombres)

        try {
            Glide.with(applicationContext)
                .load(imagen)
                .placeholder(R.drawable.ic_img_perfil)
                .into(binding.IvPerfil)
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
