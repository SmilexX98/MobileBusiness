package martinez.javier.chat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import martinez.javier.chat.databinding.ActivityRegistroEmailBinding

class RegistroEmailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegistroEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegistroEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnRegistrar.setOnClickListener {
            validarInformacion()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private var nombres = ""
    private var email = ""
    private var password = ""
    private var r_password = ""
    private fun validarInformacion() {
        nombres = binding.etNombres.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        r_password = binding.etRPassword.text.toString().trim()

        if (nombres.isEmpty()){
            binding.etNombres.error = "Ingrese nombre"
            binding.etNombres.requestFocus()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Correo invalido"
            binding.etEmail.requestFocus()
        }
        else if (email.isEmpty()){
            binding.etEmail.error = "Ingrese correo"
            binding.etEmail.requestFocus()
        }
        else if(password.isEmpty()){
            binding.etPassword.error = "Ingrese contraseña"
            binding.etPassword.requestFocus()
        }
        else if(r_password.isEmpty()){
            binding.etRPassword.error = "Repita contraseña"
            binding.etRPassword.requestFocus()
        }
        else if (password != r_password){
            binding.etRPassword.error = "No coinciden las contraseñas"
            binding.etRPassword.requestFocus()
        }
        else{
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                actualizarInformacion()

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

    private fun actualizarInformacion() {
        progressDialog.setMessage("Guardando información")

        // Identificador del usuario
        val uidU = firebaseAuth.uid
        val nombresU = nombres
        val emailU = firebaseAuth.currentUser!!.email
        val tiempoR = Constantes.obtenerTiempoD()

        // Enviar la información a Firebase
        val datosUsuario = HashMap<String, Any>()
        datosUsuario["uid"] = "$uidU"
        datosUsuario["nombres"] = "$nombresU"
        datosUsuario["email"] = "$emailU"
        datosUsuario["tiempoR"] = "$tiempoR"
        datosUsuario["proveedor"] = "Email"
        datosUsuario["estado"] = "Online"
        datosUsuario["imagen"] = ""

        // Verificar si el CheckBox está marcado
        val reference = if (binding.checkBoxVendedor.isChecked) {
            // Si el checkbox está marcado, registrar en "Usuarios"
            FirebaseDatabase.getInstance().getReference("Usuarios")
        } else {
            // Si el checkbox no está marcado, registrar en "NoUsuarios"
            FirebaseDatabase.getInstance().getReference("NoUsuarios")
        }

        // Guardar los datos del usuario en la base de datos
        reference.child(uidU!!).setValue(datosUsuario)
            .addOnSuccessListener {
                progressDialog.dismiss()

                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Falló la creación de la cuenta debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}