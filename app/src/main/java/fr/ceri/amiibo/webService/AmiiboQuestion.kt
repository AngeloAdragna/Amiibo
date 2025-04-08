package fr.ceri.amiibo.webService

// --------------------------- Classe AmiiboQuestion ---------------------------
data class AmiiboQuestion(
    val propositions: List<String>, // Liste des propositions pour la question
    val imageUrl: String, // URL de l'image associée à la question
    val correctAnswer: String, // Réponse correcte à la question
    val questionType: QuestionType // Type de la question (nom ou série de jeu)
) {
    // --------------------------- Enumération des types de questions ---------------------------
    enum class QuestionType {
        NAME, // Question sur le nom de l'Amiibo
        GAMESERIES // Question sur la série de jeux de l'Amiibo
    }
}
