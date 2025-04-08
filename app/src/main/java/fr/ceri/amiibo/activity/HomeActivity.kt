package fr.ceri.amiibo.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.R
import fr.ceri.amiibo.utils.UserPreference
import fr.ceri.amiibo.databinding.ActivityHomeBinding
import io.realm.Realm

// --------------------------- Activity d'Accueil ---------------------------
// Affiche les informations principales de l'application, gère les boutons et le thème.
class HomeActivity : AppCompatActivity() {
    private val TAG = "AmiiboHome" // Tag pour les logs
    private lateinit var ui: ActivityHomeBinding // Interface utilisateur de l'activité
    private lateinit var realm: Realm // Instance Realm pour la base de données
    private var currentTheme: String? = null // Thème actuel

    // --------------------------- Méthode onCreate ---------------------------
    // Initialisation de l'activité, de Realm et des éléments UI
    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreference.enableEdgeToEdgeTop(window) // Activer le mode plein écran en haut
        UserPreference.loadUserTheme(this) // Charger le thème utilisateur
        super.onCreate(savedInstanceState)
        ui = ActivityHomeBinding.inflate(layoutInflater) // Lier le layout avec l'interface utilisateur
        setContentView(ui.root) // Définir la vue de l'activité
        setSupportActionBar(ui.toolbar) // Initialiser la barre d'outils

        initializeRealm() // Initialiser Realm

        // --------------------------- Récupération du thème actuel ---------------------------
        // Stocke le thème actuel dans SharedPreferences
        val sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        currentTheme = sharedPreferences.getString("selectedTheme", "MarioTheme")

        // --------------------------- Gestion des actions des boutons ---------------------------
        ui.btnMainAct.setOnClickListener(this::openMainActivity) // Ouvrir l'activité principale
        ui.btnRewardAct.setOnClickListener(this::openRewardActivity) // Ouvrir l'activité des récompenses
        Log.i(TAG, "Activité Home créée")
    }

    // --------------------------- Initialisation de Realm ---------------------------
    // Configure Realm pour accéder à la base de données
    private fun initializeRealm() {
        realm = Realm.getDefaultInstance() // Initialiser l'instance Realm
    }

    // --------------------------- Méthode onCreateOptionsMenu ---------------------------
    // Crée le menu d'options de l'activité
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu) // Charger le menu de l'activité
        return true
    }

    // --------------------------- Méthode onOptionsItemSelected ---------------------------
    // Gère la sélection des éléments du menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "dans 1 fonction")
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
            // Redémarrer l'activité pour appliquer le nouveau thème
            recreate()
        } else {
            // Recharger uniquement l'icône et les informations utilisateur
            UserPreference.loadUserIcon(this, ui.iconImageView)
            UserPreference.loadUserName(ui.user)
            UserPreference.loadUserStat("level", ui.level)
        }
    }

    // --------------------------- Ouvrir l'activité principale ---------------------------
    // Lance l'activité principale
    private fun openMainActivity(view: View?) {
        val intentMainActivity = Intent(this, MainActivity::class.java)
        startActivity(intentMainActivity)
    }

    // --------------------------- Ouvrir l'activité des récompenses ---------------------------
    // Lance l'activité des récompenses
    private fun openRewardActivity(view: View?) {
        val intentRewardActivity = Intent(this, RewardActivity::class.java)
        startActivity(intentRewardActivity)
    }

    // --------------------------- Ouvrir l'activité d'aide ---------------------------
    // Lance l'activité d'aide
    private fun openHelpActivity() {
        val intentHelpActivity = Intent(this, HelpActivity::class.java)
        startActivity(intentHelpActivity)
    }

    // --------------------------- Méthode onDestroy ---------------------------
    // Ferme l'instance Realm lors de la destruction de l'activité
    override fun onDestroy() {
        super.onDestroy()
        realm.close() // Fermer Realm
    }
}
