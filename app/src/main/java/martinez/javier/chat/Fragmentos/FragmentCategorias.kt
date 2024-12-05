package martinez.javier.chat.Fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import martinez.javier.chat.R
import martinez.javier.chat.databinding.FragmentCategoriasBinding

class FragmentCategorias : Fragment() {

    private lateinit var binding: FragmentCategoriasBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoriasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set OnClickListeners for each button
        binding.button1.setOnClickListener {
            navigateToUsuariosFragment()
        }

        binding.button2.setOnClickListener {
            navigateToUsuariosFragment()
        }

        binding.button3.setOnClickListener {
            navigateToUsuariosFragment()
        }

        binding.button4.setOnClickListener {
            navigateToUsuariosFragment()
        }
    }

    private fun navigateToUsuariosFragment() {
        val fragmentUsuarios = FragmentUsuarios()
        val fragmentTransaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentoFL, fragmentUsuarios)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}


