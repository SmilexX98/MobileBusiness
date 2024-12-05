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
import martinez.javier.chat.databinding.ActivityLoginEmailBinding

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginEmailBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnIngresar.setOnClickListener {
            validarInformacion()
        }


        binding.tvRegistrarme.setOnClickListener {
            startActivity(Intent(applicationContext, RegistroEmailActivity::class.java))

        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //Almacenar los datos en variables (email y contraseña)
    private var email = ""
    private var password = ""
    private fun validarInformacion() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()

        //Validar campos
        if (email.isEmpty()){
            binding.etEmail.error = "Ingrese email"
            binding.etEmail.requestFocus() //Matener el puntero en la vista
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){ //Validar que un correo sea valido... vaya
            binding.etEmail.error = "Email no válido"
            binding.etEmail.requestFocus()
        }
        else if (password.isEmpty()){
            binding.etPassword.error = "Ingrese contraseña"
            binding.etPassword.requestFocus()
        }
        else {
            logearUsuario()
        }


    }

    private fun logearUsuario() {
        progressDialog.setMessage("Ingresando...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se realizó el login debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}