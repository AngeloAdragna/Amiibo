package fr.ceri.amiibo.webService

import android.util.Log
import io.realm.Realm
import kotlin.random.Random

class AmiiboApplication {
    // Méthode pour récupérer un trio de propositions avec un type de question aléatoire ou spécifié
    fun getAmiiboQuestionFromRealm(questionType: AmiiboQuestion.QuestionType? = null, callback: (AmiiboQuestion?) -> Unit) {
        val realm = Realm.getDefaultInstance()

        realm.executeTransactionAsync { transactionRealm ->
            val amiibo = transactionRealm.where(Amiibo::class.java).findAll().randomOrNull()

            Log.d("AmiiboDebug", "Amiibo trouvé: ${amiibo?.name ?: "Aucun amiibo trouvé"} et prop ")

            amiibo?.let {
                val propositions = mutableListOf<String>()
                val correctAnswer: String
                val selectedQuestionType: AmiiboQuestion.QuestionType

                // Si aucun type de question n'est spécifié, choisir aléatoirement entre NAME et GAMESERIES
                selectedQuestionType = questionType ?: if (Random.nextBoolean()) {
                    AmiiboQuestion.QuestionType.NAME
                } else {
                    AmiiboQuestion.QuestionType.GAMESERIES
                }

                // Charger la question selon le type sélectionné
                if (selectedQuestionType == AmiiboQuestion.QuestionType.NAME) {
                    correctAnswer = amiibo.name ?: "Nom inconnu"
                    propositions.add(correctAnswer)
                    propositions.addAll(getRandomIncorrectPropositions(amiibo.name ?: "Nom inconnu"))
                } else {  // GAMESERIES
                    correctAnswer = amiibo.gameSeries ?: "Série inconnue"
                    propositions.add(correctAnswer)
                    propositions.addAll(getRandomIncorrectPropositions(amiibo.gameSeries ?: "game inconnu"))
                }

                propositions.shuffle()
                val imageUrl = amiibo.image ?: ""

                val amiiboQuestion = AmiiboQuestion(propositions, imageUrl, correctAnswer, selectedQuestionType)

                // Retourner la question via le callback
                callback(amiiboQuestion)
            } ?: callback(null)
        }
        realm.close()
    }

    private fun getRandomIncorrectPropositions(correctAnswer: String): List<String> {
        val realm = Realm.getDefaultInstance()
        val allAmiibos = realm.where(Amiibo::class.java).findAll()

        val allAmiibosList = allAmiibos.toMutableList()
        allAmiibosList.shuffle()

        val incorrectPropositions = mutableListOf<String>()

        for (amiibo in allAmiibosList) {
            if (amiibo.name != correctAnswer) {
                incorrectPropositions.add(amiibo.name ?: "")
            }
            if (incorrectPropositions.size >= 2) break
        }

        realm.close()
        return incorrectPropositions
    }
}
