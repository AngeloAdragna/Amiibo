package fr.ceri.amiibo.webService

import io.realm.RealmObject

open class Stats(
    var level: Int = 0,           // --------------------------- Niveau du joueur ---------------------------
    var nbPoints : Int = 0,       // --------------------------- Nombre de points du joueur ---------------------------
    var pointsLevel: Int = 20,    // --------------------------- Nombre de points d'un niveau ---------------------------
    var score: Int = 0,           // --------------------------- Score d'une partie ---------------------------
    var gamesPlayed: Int = 0,     // --------------------------- Nombre de parties jouées ---------------------------
    var correctAnswers: Int = 0,  // --------------------------- Nombre de bonnes réponses ---------------------------
    var wrongAnswers: Int = 0    // --------------------------- Nombre de mauvaises réponses ---------------------------
) : RealmObject()
