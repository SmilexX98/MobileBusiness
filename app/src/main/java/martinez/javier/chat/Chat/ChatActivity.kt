package martinez.javier.chat.Chat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import martinez.javier.chat.R
import martinez.javier.chat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private var uid = "" //Guardar uid que se pasa como parametro desde el adaptadpr

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        //Obtener dato desde el adaptador
        uid = intent.getStringExtra("uid")!!//se puede obtener boolean, char etc.

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}