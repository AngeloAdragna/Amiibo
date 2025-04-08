package fr.ceri.amiibo.webService

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

// --------------------------- Application de l'AmiiboGame ---------------------------
// Cette classe hérite de `Application` et est utilisée pour initialiser Realm au démarrage de l'application.
class AmiiboGameApplication : Application() {

    // --------------------------- Méthode onCreate ---------------------------
    // Cette méthode est appelée lors de la création de l'application.
    override fun onCreate() {
        super.onCreate()

        // --------------------------- Initialisation de Realm ---------------------------
        // Initialisation de Realm, nécessaire pour pouvoir l'utiliser dans l'application.
        Realm.init(this)

        // --------------------------- Configuration de la base de données Realm ---------------------------
        // Création d'une configuration personnalisée pour la base de données Realm.
        val config = RealmConfiguration.Builder()
            .name("my-realm") // Nom du fichier de la base de données Realm.
            .deleteRealmIfMigrationNeeded() // Si une migration est nécessaire, supprime la base de données existante.
            .compactOnLaunch() // Compacte la base de données à chaque lancement pour libérer de l'espace si nécessaire.
            .build()

        // --------------------------- Définition de la configuration par défaut ---------------------------
        // Associe la configuration créée à la base de données Realm par défaut.
        Realm.setDefaultConfiguration(config)
    }
}
