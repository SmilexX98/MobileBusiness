package martinez.javier.chat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import martinez.javier.chat.databinding.ActivityOpcionesLoginBinding

class OpcionesLoginActivity : AppCompatActivity() {

    //Para tener acceso a las vista implementadas en el diseño
    //Siendo los dos botones
    private lateinit var binding : ActivityOpcionesLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog : ProgressDialog
    private lateinit var mGoogleSingInClient : GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOpcionesLoginBinding.inflate(layoutInflater)


        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()//Crear instancia de firebaseAuth

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        //Opciones de inicio se sesion
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSingInClient = GoogleSignIn.getClient(this, gso)

        comprobarSesion()

        binding.opcionEmail.setOnClickListener {
            startActivity(Intent(applicationContext, LoginEmailActivity::class.java))
        }

        binding.opcionGoogle.setOnClickListener {
            iniciarGoogle()
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun iniciarGoogle() {
        val googleSignInIntent = mGoogleSingInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
        //Obtener la respuesta del cuadro de dialogo
    }

    private val googleSignInARL = registerForActivityResult( //Activity Result Launcher
        ActivityResultContracts.StartActivityForResult()){resultado->
        //Recuperar la informacion del usuario que ha seleccionado de la lista
        if (resultado.resultCode == RESULT_OK){
            val data = resultado.data

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val cuenta = task.getResult(ApiException::class.java)
                autenticarCuentaGoogle(cuenta.idToken)
            }catch (e : Exception){
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }else{
            Toast.makeText(
                this,
                "Cancelado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun autenticarCuentaGoogle(idToken: String?){
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener {authResultado ->
                val email = firebaseAuth.currentUser?.email
                //Si se quiere dos dominios diferentes
                //if (email != null && (email.endsWith("DOMINIO_1") || email.endsWith("DOMINIO_2")))
                if (email != null && email.endsWith("@alumnos.udg.mx")){
                    // Si el dominio es correcto, proceder normalmente
                    if (authResultado.additionalUserInfo!!.isNewUser){
                        actualizarInfoUsuario()
                    } else{
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    }
                } else{
                    // Si el dominio no es permitido, eliminar el usuario de Firebase Authentication
                    firebaseAuth.currentUser?.delete()
                        ?.addOnCompleteListener {task->
                            if (task.isSuccessful){
                                Toast.makeText(
                                    this,
                                    "Debes utilizar una cuenta insititucional",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else{
                                Toast.makeText(
                                    this,
                                    "No se pudo eliminar la cuenta. Inténtalo de nuevo.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    // Cerrar sesión en Firebase y Google
                    firebaseAuth.signOut()
                    mGoogleSingInClient.signOut()
                }
            }
            .addOnFailureListener{e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    //Practicamente es el registro de usuario de EMAIl (RegistroEmailActivity)
    private fun actualizarInfoUsuario() {
        progressDialog.setMessage("Guardando información")

        //Identificador del usuario
        val uidU = firebaseAuth.uid
        val nombresU = firebaseAuth.currentUser!!.displayName //Obtener nombre de la cuenta de google
        val emailU = firebaseAuth.currentUser!!.email
        val tiempoR =Constantes.obtenerTiempoD()

        //Enviar la informacion a firebase
        val datosUsuario = HashMap<String, Any>()
        datosUsuario["uid"] = "$uidU"
        datosUsuario["nombres"] = "$nombresU"
        datosUsuario["email"] = "$emailU"
        datosUsuario["tiempoR"] = "$tiempoR"
        datosUsuario["proveedor"] = "Google"
        datosUsuario["estado"] = "Online"

        datosUsuario["imagen"] = ""

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uidU!!)
            .setValue(datosUsuario)
            .addOnSuccessListener {
                progressDialog.dismiss()

                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()


            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Falló la cracion de la cuenta debudo a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}