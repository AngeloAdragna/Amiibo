package fr.ceri.amiibo.webService

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// --------------------------- Entité AmiiboGame ---------------------------
// Entité récupérée depuis l'API et stockée dans la base de données Realm
open class AmiiboGame (
    @PrimaryKey
    var key: String? = null, // Clé primaire de l'AmiiboGame
    var name: String? = null, // Nom de l'AmiiboGame
    var isSelected: Boolean = false // État de sélection de l'AmiiboGame
) : RealmObject() {

    companion object {
        // --------------------------- Noms des champs statiques ---------------------------
        val NAME = "name" // Nom du champ "name"

        // --------------------------- Création d'un AmiiboGame ---------------------------
        // Fonction pour créer un AmiiboGame dans la base de données
        fun create(realm: Realm, key: String, name: String, isSelected: Boolean) {
            val amiiboGame = realm.createObject(AmiiboGame::class.java, key)
            amiiboGame.name = name
            amiiboGame.isSelected=isSelected
        }
    }
}
