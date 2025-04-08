package fr.ceri.amiibo.activity

import android.app.Activity
import fr.ceri.amiibo.utils.MusicService
import android.content.Intent
import io.realm.Realm
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.R
import fr.ceri.amiibo.utils.UserPreference
import fr.ceri.amiibo.databinding.ActivityRewardBinding
import fr.ceri.amiibo.utils.Display
import fr.ceri.amiibo.webService.User


// --------------------------- Activité des récompenses et personnalisation ---------------------------
// RewardActivity est permet de changer de nom, de thème, de musique, de gerer l'etat de la musique et changer d'icone et réinitialiser ses données
class RewardActivity : AppCompatActivity() {
    private lateinit var ui: ActivityRewardBinding
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreference.loadUserTheme(this)
        UserPreference.enableEdgeToEdgeTop(window)
        super.onCreate(savedInstanceState)
        ui = ActivityRewardBinding.inflate(layoutInflater)   // Lier le layout
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)

        // Initialisation Realm
        realm = Realm.getDefaultInstance()

        // Chargement des infos utilisateur
        UserPreference.loadUserIcon(this, ui.iconImageView)
        UserPreference.loadUserName(this, ui.user)
        UserPreference.loadUserStat("level", ui.level)
        UserPreference.loadUserName(this, ui.labelName)

        // Mise en place des actions
        setupThemeButtons()
        setupIconButtons()
        setupMusicButtons()

        // Gestion du bouton musique principal
        ui.btnMusic.setOnClickListener(this::onOffMusic)

        // Mise à jour du nom
        ui.validName.setOnClickListener {
            validateAndUpdateUserName(ui.labelName.text.toString(), ui.user)
        }

        // Réinitialisation de Realm
        ui.btnResetAll.setOnClickListener {
            onDelete(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()

    }

    // --------------------------- Menu ---------------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_help_reward, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnBackAct -> {
                backToPreviousActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backToPreviousActivity() {
        finish()
    }

    // --------------------------- Thèmes ---------------------------

    private fun setupThemeButtons() {
        ui.btnMario.setOnClickListener { UserPreference.updateUserTheme(this, "MarioTheme") }
        ui.btnLuigi.setOnClickListener { UserPreference.updateUserTheme(this, "LuigiTheme") }
        ui.btnPeach.setOnClickListener { UserPreference.updateUserTheme(this, "PeachTheme") }
        ui.btnBowser.setOnClickListener { UserPreference.updateUserTheme(this, "BowserTheme") }
    }

    // --------------------------- Nom ---------------------------
    private fun validateAndUpdateUserName(userNameInput: String, user: TextView) {
        val userName = userNameInput.trim()
        val regex = "^[a-zA-Z0-9 ]{2,12}\$".toRegex() // Lettres, chiffres, espaces, entre 2 et 12 caractères

        runOnUiThread {
            when {
                userName.isBlank() -> {
                    Display.showToast(this,getString(R.string.error_blank_name))
                }
                !userName.matches(regex) -> {
                    Display.showToast(this,getString(R.string.error_invalid_name))
                }
                else -> {
                    UserPreference.updateUserName(userName, user)
                    Display.showToast(this,getString(R.string.success_update_name))
                }
            }
        }
    }

    // --------------------------- Icônes utilisateur ---------------------------

    private fun setupIconButtons() {
        val level = UserPreference.loadUserStat("level", ui.level)
        // Icône par défaut
        ui.iconNone.setOnClickListener { updateUserIcon("circular_bg") }
        // Icônes déblocables avec niveaux requis
        setupUnlockableIcon(ui.iconMario, 0, level, "mario_icon", ui.labelMario)
        setupUnlockableIcon(ui.iconPeach, 0, level, "peach_icon", ui.labelPeach)
        setupUnlockableIcon(ui.iconYoshi, 2, level, "yoshi_icon", ui.labelYoshi)
        setupUnlockableIcon(ui.iconWario, 3, level, "wario_icon", ui.labelWario)
        setupUnlockableIcon(ui.iconLuigi, 4, level, "luigi_icon", ui.labelLuigi)
        setupUnlockableIcon(ui.iconDaisy, 5, level, "daisy_icon", ui.labelDaisy)
        setupUnlockableIcon(ui.iconToadBleu, 6, level, "toad_bleu_icon", ui.labelToadBleu)
        setupUnlockableIcon(ui.iconBowser, 7, level, "bowser_icon", ui.labelBowser)
        setupUnlockableIcon(ui.iconToadRose, 8, level, "toad_rose_icon", ui.labelToadRose)
        setupUnlockableIcon(ui.iconToadRouge, 9, level, "toad_rouge_icon", ui.labelToadRouge)
        setupUnlockableIcon(ui.iconWaluigi, 10, level, "waluigi_icon", ui.labelWaluigi)

        //permet de mettre à jour si la musique est en pause quand on ouvre l'application
        val sharedPreferences = getSharedPreferences("MusicPrefs", MODE_PRIVATE)
        val isMusicOn = sharedPreferences.getBoolean("isMusicOn", false)
        if (isMusicOn) {
            ui.btnMusic.setImageResource(R.drawable.sound_on_icon)
        } else {
            ui.btnMusic.setImageResource(R.drawable.sound_off_icon)
        }
    }

    private fun setupUnlockableIcon(
        iconView: ImageView,
        requiredLevel: Int,
        currentLevel: Int,
        iconName: String,
        labelView: TextView
    ) {
        if (currentLevel >= requiredLevel) {
            iconView.setOnClickListener { updateUserIcon(iconName) }
            labelView.text = ""
        } else {
            iconView.setImageResource(R.drawable.circular_bg)
            labelView.text = getString(R.string.level_label, requiredLevel)
            iconView.setOnClickListener(null)
        }
    }

    private fun updateUserIcon(iconName: String) {
        UserPreference.updateUserIcon(this, iconName, ui.iconImageView)
    }

    // --------------------------- Musique ---------------------------

    private fun setupMusicButtons() {
        val musicMap = mapOf(
            ui.btnMusic1 to "lost_woods_zelda",
            ui.btnMusic2 to "pokemon_nb",
            ui.btnMusic3 to "mario_bros",
            ui.btnMusic4 to "sonic"
        )

        for ((button, musicFile) in musicMap) {
            button.setOnClickListener {
                // Stopper l'ancienne musique
                val stopIntent = Intent(this, MusicService::class.java).apply { action = "STOP" }
                startService(stopIntent)
                // Jouer la nouvelle musique
                playMusic(musicFile)
                ui.btnMusic.setImageResource(R.drawable.sound_on_icon)// Changer icône
            }
        }
    }

    private fun playMusic(musicFile: String) {
        val intent = Intent(this, MusicService::class.java).apply {
            action = "PLAY"
            putExtra("MUSIC_FILE", musicFile)
        }
        startService(intent)
    }

    private fun onOffMusic(view: View) {
        val sharedPreferences = getSharedPreferences("MusicPrefs", MODE_PRIVATE)
        var currentMusic = sharedPreferences.getString("currentMusicFile", "lost_woods_zelda") ?: "lost_woods_zelda"
        val isMusicOn = sharedPreferences.getBoolean("isMusicOn", false)

        val icon = if (!isMusicOn) {
            val playIntent = Intent(this, MusicService::class.java).apply {
                action = "PLAY"
                putExtra("MUSIC_FILE", currentMusic)
            }
            startService(playIntent)
            R.drawable.sound_on_icon
        } else {
            val stopIntent = Intent(this, MusicService::class.java).apply { action = "STOP" }
            startService(stopIntent)
            R.drawable.sound_off_icon
        }
        ui.btnMusic.setImageResource(icon)
    }

    // --------------------------- Réinitialisation des données ---------------------------

    private fun onDelete(activity: Activity) {
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.conf_supp))
            //le bouton "oui" supprime vraiment
            .setPositiveButton(android.R.string.ok) { dialog, idbtn ->
                resetRealmData(this)
            }
            //le bouton "non" ne fait rien
            .setNegativeButton(android.R.string.cancel, null)
            //affichage du dialogue
            .show()
    }
    private fun resetRealmData(activity: Activity) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransactionAsync({ transactionRealm ->
            transactionRealm.deleteAll()
        }, {
            runOnUiThread {
                Display.showToast(this,getString(R.string.data_reset))
            }
            activity.recreate()
        }, {
            runOnUiThread {
                Display.showToast(this,getString(R.string.data_reset_err))
            }
        })
        // Recréer un utilisateur par défaut
        realm.executeTransactionAsync {
            User.create(it, "user1", getString(R.string.name), "circular_bg", "MarioTheme")
        }
        realm.close()
    }
}
