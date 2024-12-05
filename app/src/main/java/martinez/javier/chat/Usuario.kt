package martinez.javier.chat

//Declarar atributos del usuario
//Objeto en el cual almacena la informacion de cada usuario que existe en la base de datos
class Usuario {
    var uid : String = ""
    var email : String = ""
    var nombres : String = ""
    var imagen : String = ""

    constructor()

    constructor(uid: String, email: String, nombers: String, imagen: String) {
        this.uid = uid
        this.email = email
        this.nombres = nombers
        this.imagen = imagen
    }

}