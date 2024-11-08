package martinez.javier.chat

import java.text.DateFormat
import java.util.Arrays
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

    fun rutaChat(receptorUid : String, emisorUid : String) : String{
        //Arreglo de Uid's
        var arrayUid = arrayOf(receptorUid, emisorUid)
        //Ordenarlos
        Arrays.sort(arrayUid)
        //Concatenar ambos Uid's
        return "${arrayUid[0]}_${arrayUid[1]} "
        //Uid del usuario con el que se esta hablando (receptor) = A
        //Uid del usuario actual (emisor) = B
        //La ruta = B_A
    }
}