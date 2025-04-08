package fr.ceri.amiibo.webService

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

// --------------------------- Entité Amiibo ---------------------------
// Entité récupérée depuis l'API et stockée dans la base de données Realm
@RealmClass
open class Amiibo(
    @PrimaryKey
    var head: String? = null, // Clé primaire de l'Amiibo
    var amiiboSeries: String? = null, // Série d'Amiibo
    var character: String? = null, // Personnage de l'Amiibo
    var gameSeries: String? = null, // Série de jeux liée à l'Amiibo
    var image: String? = null, // Image de l'Amiibo
    var name: String? = null // Nom de l'Amiibo
) : RealmObject() {
    companion object {
        // --------------------------- Création d'un Amiibo en base de donnée ---------------------------
        fun create(realm: Realm, head: String, name: String, gameSeries: String, image: String) {
            val amiibo = realm.createObject(Amiibo::class.java, head)
            amiibo.name = name
            amiibo.gameSeries = gameSeries
            amiibo.image = image
        }

    }
}
