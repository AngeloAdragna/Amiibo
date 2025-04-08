package fr.ceri.amiibo.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.R
import fr.ceri.amiibo.utils.UserPreference
import fr.ceri.amiibo.databinding.ActivityHomeBinding
import io.realm.Realm

// --------------------------- Activity d'Accueil ---------------------------
// Hub principale de l'utilisateur, depuis lequel on accède aux autres activtés
class HomeActivity : AppCompatActivity() {
    private lateinit var ui: ActivityHomeBinding
    private lateinit var realm: Realm
    private var currentTheme: String? = null // Thème actuel

    // --------------------------- Méthode onCreate ---------------------------
    // Initialisation de l'activité, de Realm et des éléments UI
    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreference.enableEdgeToEdgeTop(window)
        UserPreference.loadUserTheme(this)
        super.onCreate(savedInstanceState)
        ui = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)
        initializeRealm() // Initialiser Realm

        // --------------------------- Récupération du thème actuel ---------------------------
        // Stocke le thème actuel dans SharedPreferences
        // Nous sommes obligé car le home doit être recréer pour remettre le nouveau thème,
        // sachant qu'on dot toujours pouvoir revenir sur home
        val sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        currentTheme = sharedPreferences.getString("selectedTheme", "MarioTheme")

        // --------------------------- Gestion des actions des boutons ---------------------------
        ui.btnMainAct.setOnClickListener(this::openMainActivity) // Ouvrir l'activité principale
        ui.btnRewardAct.setOnClickListener(this::openRewardActivity) // Ouvrir l'activité des récompenses
    }
    // --------------------------- Initialisation de Realm ---------------------------
    private fun initializeRealm() {
        realm = Realm.getDefaultInstance()
    }

    // --------------------------- Méthode onCreateOptionsMenu ---------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    // --------------------------- Méthode onOptionsItemSelected ---------------------------
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnHelpAct -> {
                openHelpActivity() // Ouvrir l'activité d'aide
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --------------------------- Méthode onResume ---------------------------
    // Recharger les informations utilisateur et vérifier le changement de thème
    override fun onResume() {
        super.onResume()
        // Vérifier si le thème a changé
        val sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        val newTheme = sharedPreferences.getString("selectedTheme", "MarioTheme")
        if (newTheme != currentTheme) {
            recreate()// Redémarrer l'activité pour appliquer le nouveau thème
        } else {
            // Recharger uniquement l'icône et les informations utilisateur
            UserPreference.loadUserIcon(this, ui.iconImageView)
            UserPreference.loadUserName(this, ui.user)
            UserPreference.loadUserStat("level", ui.level)
        }
    }

    // --------------------------- Ouvrir l'activité principale ---------------------------
    private fun openMainActivity(view: View?) {
        val intentMainActivity = Intent(this, MainActivity::class.java)
        startActivity(intentMainActivity)
    }

    // --------------------------- Ouvrir l'activité des récompenses ---------------------------
    private fun openRewardActivity(view: View?) {
        val intentRewardActivity = Intent(this, RewardActivity::class.java)
        startActivity(intentRewardActivity)
    }

    // --------------------------- Ouvrir l'activité d'aide ---------------------------
    private fun openHelpActivity() {
        val intentHelpActivity = Intent(this, HelpActivity::class.java)
        startActivity(intentHelpActivity)
    }

    // --------------------------- Méthode onDestroy ---------------------------
    override fun onDestroy() {  // Ferme l'instance Realm lors de la destruction de l'activité
        super.onDestroy()
        realm.close()
    }
}
