package fr.ceri.amiibo.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import fr.ceri.amiibo.R
import fr.ceri.amiibo.utils.UserPreference
import fr.ceri.amiibo.databinding.ActivityGameBinding
import fr.ceri.amiibo.utils.Display
import fr.ceri.amiibo.utils.OnSwipeTouchListener
import fr.ceri.amiibo.webService.Amiibo
import fr.ceri.amiibo.webService.AmiiboApplication
import fr.ceri.amiibo.webService.AmiiboQuestion
import io.realm.Realm

// --------------------------- Activity de Jeu ---------------------------
// Gère le quiz de l'application, incluant la gestion des questions, des réponses et du score
class GameActivity : AppCompatActivity() {
    lateinit var ui: ActivityGameBinding // Interface utilisateur de l'activité
    private lateinit var realm: Realm // Instance de Realm pour la gestion de la base de données
    private lateinit var amiiboApplication: AmiiboApplication // Application Amiibo pour récupérer les questions
    private lateinit var currentQuestion: AmiiboQuestion // Question courante à afficher

    private var questionCount = 0  // Nombre de questions posées
    private var nbCorrectAnswers = 0 // Nombre de bonnes réponses
    private var nbWrongAnswers = 0  // Nombre de mauvaises réponses
    private val totalQuestions = 10  // Nombre total de questions (ici, 10 questions)

    // --------------------------- Méthode onCreate ---------------------------
    // Initialisation de l'activité, chargement des préférences utilisateur, et configuration des éléments
    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreference.loadUserTheme(this)  // Charger le thème utilisateur
        UserPreference.enableEdgeToEdgeTop(window)  // Activer le mode plein écran en haut de l'écran
        super.onCreate(savedInstanceState)
        ui = ActivityGameBinding.inflate(layoutInflater) // Lier le layout avec l'interface utilisateur
        setContentView(ui.root) // Définir la vue de l'activité
        setSupportActionBar(ui.toolbar) // Initialiser la barre d'outils

        // Charger les préférences utilisateur
        UserPreference.loadUserIcon(this, ui.iconImageView)
        UserPreference.loadUserName(ui.user)
        UserPreference.updateUserStat(0, "score", ui.labelLevel)
        UserPreference.loadUserStat("score", ui.labelLevel)

        // Initialisation de l'application Amiibo et Realm
        amiiboApplication = AmiiboApplication()
        initializeRealm()

        // Initialisation du détecteur de gestes pour les swipes
        val gestureListener = OnSwipeTouchListener.GestureListener(this, this)
        val gestureDetector = GestureDetector(this, gestureListener)
        ui.root.setOnTouchListener { v, event -> gestureDetector.onTouchEvent(event) } // Attacher l'écouteur au swipe

        // Charger la première question
        loadNewQuestion()

        // Gérer les clics sur les boutons de réponses
        ui.btnGameAct.setOnClickListener(this::openHomeActivity)
        ui.option1.setOnClickListener(this::chooseAnswer)
        ui.option2.setOnClickListener(this::chooseAnswer)
        ui.option3.setOnClickListener(this::chooseAnswer)

        // Gérer les swipes pour changer de type de question
        ui.btnLeft.setOnClickListener { loadNewQuestion(AmiiboQuestion.QuestionType.GAMESERIES) }
        ui.btnRight.setOnClickListener { loadNewQuestion(AmiiboQuestion.QuestionType.NAME) }
    }

    // --------------------------- Charger une nouvelle question ---------------------------
    // Récupère une nouvelle question depuis l'application Amiibo
    fun loadNewQuestion(questionType: AmiiboQuestion.QuestionType? = null) {
        if (questionCount < totalQuestions) {
            amiiboApplication.getAmiiboQuestionFromRealm(questionType) { amiiboQuestion ->
                if (amiiboQuestion != null) {
                    currentQuestion = amiiboQuestion

                    // Récupérer la couleur accent du thème
                    val typedValue = TypedValue()
                    theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
                    val colorAccent = typedValue.data

                    // Mettre à jour l'UI
                    runOnUiThread {
                        // Si le joueur a swipé, ajuster le score
                        if (questionType != null) {
                            var scoreValue = UserPreference.loadUserStat("score")
                            val newScore = scoreValue - 1
                            UserPreference.updateUserStat(newScore, "score", ui.labelLevel)
                            updateScoreColor(newScore)
                        }

                        // Réinitialiser les couleurs de fond des options avec la couleur d'accent
                        ui.option1.setBackgroundColor(colorAccent)
                        ui.option2.setBackgroundColor(colorAccent)
                        ui.option3.setBackgroundColor(colorAccent)
                        when (currentQuestion.questionType) {
                            AmiiboQuestion.QuestionType.NAME -> {
                                ui.typeQuestion.text = getString(R.string.type_name)
                            }
                            AmiiboQuestion.QuestionType.GAMESERIES -> {
                                ui.typeQuestion.text = getString(R.string.type_gameseries)
                            }
                        }
                        ui.option1.text = currentQuestion.propositions[0]
                        ui.option2.text = currentQuestion.propositions[1]
                        ui.option3.text = currentQuestion.propositions[2]

                        // Réactiver les boutons de réponse
                        ui.option1.isEnabled = true
                        ui.option2.isEnabled = true
                        ui.option3.isEnabled = true

                        // Charger l'image avec Glide
                        Glide.with(this)
                            .load(currentQuestion.imageUrl)
                            .into(ui.quizImage)
                    }

                    questionCount++
                } else {
                    runOnUiThread {
                        Display.showToast(this, getString(R.string.error_loading_question))
                    }
                }
            }
        } else {
            showQuizFinished() // Si toutes les questions sont posées, afficher la fin du quiz
        }
    }

    // --------------------------- Choisir une réponse ---------------------------
    // Vérifie si la réponse sélectionnée est correcte
    private fun chooseAnswer(view: View) {
        if (::currentQuestion.isInitialized) {
            // Désactiver tous les boutons de réponse pour empêcher plusieurs clics
            ui.option1.isEnabled = false
            ui.option2.isEnabled = false
            ui.option3.isEnabled = false
            when (view.id) {
                ui.option1.id -> checkAnswer(ui.option1.text.toString())
                ui.option2.id -> checkAnswer(ui.option2.text.toString())
                ui.option3.id -> checkAnswer(ui.option3.text.toString())
            }
        } else {
            Display.showToast(this, getString(R.string.question_not_loaded))
        }
    }

    // --------------------------- Vérifier la réponse ---------------------------
    // Met à jour le score et le feedback utilisateur en fonction de la réponse sélectionnée
    private fun checkAnswer(selectedAnswer: String) {
        val correctAnswer = currentQuestion.correctAnswer
        var scoreValue = UserPreference.loadUserStat("score")

        if (selectedAnswer == correctAnswer) {
            runOnUiThread {
                Display.showToast(this, getString(R.string.correct_answer))
            }
            scoreValue += 2 // Ajoute 2 points pour une bonne réponse
            nbCorrectAnswers += 1
        } else {
            runOnUiThread {
                Display.showToast(this, getString(R.string.wrong_answer))
            }
            scoreValue -= 2 // Retire 2 points pour une mauvaise réponse
            nbWrongAnswers += 1
        }

        // Met à jour la couleur du texte en fonction du score
        updateScoreColor(scoreValue)

        // Met à jour le score
        UserPreference.updateUserStat(scoreValue, "score", ui.labelLevel)

        highlightCorrectAnswer() // Mettre en évidence la bonne réponse

        // Charger la prochaine question après un délai
        Handler(mainLooper).postDelayed({ loadNewQuestion() }, 1000)
    }

    // --------------------------- Mettre en évidence la bonne réponse ---------------------------
    // Change la couleur de fond de la réponse correcte en vert
    private fun highlightCorrectAnswer() {
        if (currentQuestion.propositions[0] == currentQuestion.correctAnswer) {
            ui.option1.setBackgroundColor(getColor(R.color.greenLetterTitleColor))
        }
        if (currentQuestion.propositions[1] == currentQuestion.correctAnswer) {
            ui.option2.setBackgroundColor(getColor(R.color.greenLetterTitleColor))
        }
        if (currentQuestion.propositions[2] == currentQuestion.correctAnswer) {
            ui.option3.setBackgroundColor(getColor(R.color.greenLetterTitleColor))
        }
    }

    // --------------------------- Afficher la fin du quiz ---------------------------
    // Met à jour les statistiques utilisateur et redirige vers l'écran d'accueil
    private fun showQuizFinished() {
        var score = UserPreference.loadUserStat("score")
        var points = UserPreference.loadUserStat("nbPoints")
        var pointsLevel = UserPreference.loadUserStat("pointsLevel")
        var gamesPlayed = UserPreference.loadUserStat("gamesPlayed")
        var correctAnswers = UserPreference.loadUserStat("correctAnswers")
        var wrongAnswers = UserPreference.loadUserStat("wrongAnswers")

        // Vérifie que le nombre de points par niveau est bien défini
        if (pointsLevel != 0) {
            val level = UserPreference.loadUserStat("level") //niveau actuel
            val result = score + (points % pointsLevel)  // Calcule le score relatif à ce niveau
            // Détermine le nouveau niveau selon les règles suivantes :
            // - Si le résultat dépasse le seuil, on monte d'un niveau
            // - Si le résultat est négatif et le niveau > 1, on descend d'un niveau
            // - Sinon, on reste au même niveau
            val newLevel = when {
                result >= pointsLevel -> level + 1
                result < 0 && level > 1 -> level - 1
                else -> level
            }
            if (newLevel != level) {
                UserPreference.updateUserStat(newLevel, "level")
            }
        }

        points += score
        points = maxOf(points, 0)
        UserPreference.updateUserStat(points, "nbPoints")
        UserPreference.updateUserStat(gamesPlayed + 1, "gamesPlayed")
        UserPreference.updateUserStat(correctAnswers + nbCorrectAnswers, "correctAnswers")
        UserPreference.updateUserStat(wrongAnswers + nbWrongAnswers, "wrongAnswers")

        Display.showToast(this, getString(R.string.quiz_end_message, questionCount))

        Handler(mainLooper).postDelayed({
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("score", questionCount)
            startActivity(intent)
            finish()
        }, 2000)
    }

    // --------------------------- Initialiser Realm ---------------------------
    // Configure l'instance de Realm et charge les Amiibos sélectionnés
    private fun initializeRealm() {
        realm = Realm.getDefaultInstance()
        getAllAmiiboSelectedFromRealm(realm)
    }

    // --------------------------- Charger les Amiibos sélectionnés ---------------------------
    // Récupère tous les Amiibos sélectionnés depuis Realm
    private fun getAllAmiiboSelectedFromRealm(realm: Realm) {
        realm.executeTransactionAsync { transactionRealm ->
            val amiibos = transactionRealm.where(Amiibo::class.java).findAll()
            for (amiibo in amiibos) {
                Display.showLog("On a importé  ${amiibo.name} ${amiibo.gameSeries}  ${amiibo.image}.")
            }
        }
    }

    // --------------------------- Menu Options ---------------------------
    // Gère les options du menu de l'activité
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_game, menu)
        return true
    }

    // --------------------------- Sélectionner une option ---------------------------
    // Gère la sélection des éléments du menu (réinitialisation du score, aide, etc.)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.resetScore -> {
                UserPreference.updateUserStat(0, "score", ui.labelLevel)
                nbCorrectAnswers = 0
                nbWrongAnswers = 0
                ui.labelLevel.setTextColor(getColor(R.color.whiteTextAndButtonColor))
                true
            }
            R.id.btnHelpAct -> {
                openHelpActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --------------------------- Mettre à jour la couleur du score ---------------------------
    // Change la couleur du score en fonction de sa valeur
    private fun updateScoreColor(score: Int) {
        if (score < 0) {
            ui.labelLevel.setTextColor(getColor(R.color.redLetterTitleColor))  // Couleur rouge pour score négatif
        } else if (score > 0) {
            ui.labelLevel.setTextColor(getColor(R.color.greenLetterTitleColor))  // Couleur verte pour score positif
        } else {
            ui.labelLevel.setTextColor(getColor(R.color.whiteTextAndButtonColor))  // Couleur blanche pour score égal à zéro
        }
    }

    // --------------------------- Ouvrir l'activité d'aide ---------------------------
    // Lance l'activité d'aide
    private fun openHelpActivity() {
        val intentHelpActivity = Intent(this, HelpActivity::class.java)
        startActivity(intentHelpActivity)
    }

    // --------------------------- Ouvrir l'activité d'accueil ---------------------------
    // Lance l'activité d'accueil (HomeActivity)
    private fun openHomeActivity(view: View) {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()  // Fermer l'activité en cours
    }

    // --------------------------- Méthode onPause ---------------------------
    // Ferme l'instance Realm à la pause de l'activité pour éviter les fuites de mémoire
    override fun onPause() {
        super.onPause()
        if (this::realm.isInitialized) {
            realm.close()
        }
    }
}
