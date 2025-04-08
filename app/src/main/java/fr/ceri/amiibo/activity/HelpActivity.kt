package fr.ceri.amiibo.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.R
import fr.ceri.amiibo.utils.UserPreference
import fr.ceri.amiibo.databinding.ActivityHelpBinding

// --------------------------- Activity d'Aide ---------------------------
// Affiche les ses statistiques et les règles du jeu
class HelpActivity : AppCompatActivity() {
    private lateinit var ui: ActivityHelpBinding // Interface utilisateur de l'activité

    // --------------------------- Méthode onCreate ---------------------------
    // Initialisation de l'activité, récupération des statistiques utilisateur et affichage
    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreference.loadUserTheme(this)
        UserPreference.enableEdgeToEdgeTop(window)
        super.onCreate(savedInstanceState)
        ui = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)

        // Charger les informations utilisateur
        UserPreference.loadUserIcon(this, ui.iconImageView) // Charger l'icône de l'utilisateur
        UserPreference.loadUserName(this, ui.user) // Charger le nom de l'utilisateur
        UserPreference.loadUserStat("level", ui.levelUser) // Charger le niveau de l'utilisateur

        // Afficher les points et le prochain niveau
        val totalPoints = UserPreference.loadUserStat("nbPoints")
        ui.ptsNiveau.text = getString(R.string.points_format, totalPoints)
        val pointsLevel = UserPreference.loadUserStat("pointsLevel")
        val remaining = if (pointsLevel != 0) (pointsLevel - (totalPoints % pointsLevel)) % pointsLevel else 0
        ui.ptsToNextLevel.text = getString(R.string.points_to_next_level, remaining)

        // Afficher les statistiques de jeu
        val nbPlayed = UserPreference.loadUserStat("gamesPlayed")
        ui.nbPlayed.text = getString(R.string.games_played, nbPlayed)
        val nbGoodAnswer = UserPreference.loadUserStat("correctAnswers")
        ui.nbGoodAnswer.text = getString(R.string.correct_answers, nbGoodAnswer)
        val nbBadAnswer = UserPreference.loadUserStat("wrongAnswers")
        ui.nbBadAnswer.text = getString(R.string.wrong_answers, nbBadAnswer)
    }

    // --------------------------- Méthode onCreateOptionsMenu ---------------------------
    // Crée le menu d'options de l'activité
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_help_reward, menu)
        return true
    }

    // --------------------------- Méthode onOptionsItemSelected ---------------------------
    // Gère la sélection des éléments du menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnBackAct -> {
                backToPreviousActivity() // Retour à l'activité précédente
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --------------------------- Retour à l'activité précédente ---------------------------
    // Ferme l'activité actuelle pour revenir à l'activité précédente
    private fun backToPreviousActivity() {
        finish()
    }
}
