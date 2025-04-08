package fr.ceri.amiibo.webService

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class User(
    @PrimaryKey
    var key: String? = null,
    var name: String? = null,
    var icon: String? = null,
    var theme: String? = null,
    var stats: Stats? = Stats() // stats nullable
) : RealmObject() {

    companion object {
        const val KEY = "key"

        // --------------------------- Créer un utilisateur s'il n'existe pas déjà ---------------------------
        fun create(realm: Realm, key: String, name: String, icon: String, theme: String? = null) {
            // Vérifier si l'utilisateur existe déjà
            val existingUser = realm.where(User::class.java).equalTo(KEY, key).findFirst()

            if (existingUser == null) {
                // Créer un nouvel utilisateur avec les données fournies
                val user = realm.createObject(User::class.java, key)
                user.name = name
                user.icon = icon
                user.theme = theme
                user.stats = realm.createObject(Stats::class.java) // Initialisation des stats par défaut
            }
        }
    }
}
