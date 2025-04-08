package fr.ceri.amiibo.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.utils.UserPreference
import fr.ceri.amiibo.databinding.ActivityLoadingBinding
import fr.ceri.amiibo.utils.MusicService
import fr.ceri.amiibo.utils.Display
import fr.ceri.amiibo.webService.User
import io.realm.Realm

class LoadingActivity : AppCompatActivity() {
    private lateinit var ui: ActivityLoadingBinding

    // --------------------------- Initialisation de l'activité ---------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        // Appliquer les préférences de thème et les paramètres d'affichage
        UserPreference.loadUserTheme(this)
        UserPreference.enableEdgeToEdgeTop(window)
        super.onCreate(savedInstanceState)
        ui = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // --------------------------- Vérification et démarrage de la musique ---------------------------
        checkAndStartMusic()

        // Initialisation de l'utilisateur dans la base de données
        val realm = Realm.getDefaultInstance()
        initUser(realm)

        // Lancer HomeActivity après un délai de 3 secondes
        Handler().postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, 3000)
    }

    // --------------------------- Vérification de la musique ---------------------------
    private fun checkAndStartMusic() {
        val sharedPreferences = getSharedPreferences("MusicPrefs", MODE_PRIVATE)
        val isMusicOn = sharedPreferences.getBoolean("isMusicOn", false)
        val allPreferences = sharedPreferences.all
        Display.showLog("loading "+allPreferences.toString())

        if (isMusicOn) {
            val currentMusicFile = sharedPreferences.getString("currentMusicFile", "lost_woods_zelda") ?: "lost_woods_zelda"
            startMusicService("PLAY", currentMusicFile)
        }
    }

    // --------------------------- Démarrer le service de musique ---------------------------
    private fun startMusicService(action: String, musicFile: String) {
        val playIntent = Intent(this, MusicService::class.java).apply {
            this.action = action
            putExtra("MUSIC_FILE", musicFile)
        }
        startService(playIntent)
    }

    // --------------------------- Initialisation de l'utilisateur ---------------------------
    private fun initUser(realm: Realm) {
        realm.executeTransactionAsync {
            User.create(it, "user1", "default", "circular_bg", "MarioTheme")
        }
    }

    // --------------------------- Nettoyage des ressources ---------------------------
    override fun onDestroy() {
        super.onDestroy()
        Realm.getDefaultInstance().close()  // Fermeture de l'instance de Realm
    }
}
