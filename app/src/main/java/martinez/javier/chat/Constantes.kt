package martinez.javier.chat

import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

object Constantes {
    fun obtenerTiempoD() : Long{
        return System.currentTimeMillis()
    }

    //Convertir en fecha el tiempo obtenido del dispositivo
    fun formatoFecha(tiempo : Long) :String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = tiempo

        return android.text.format.DateFormat.format("dd/MM/yyyy", calendar).toString()
    }
}