package fr.ceri.amiibo.webService

data class AmiiboHeader(
    // --------------------------- Liste des AmiiboGames ---------------------------
    var amiibo: MutableList<AmiiboGame>? = null
)
