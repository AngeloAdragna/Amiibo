package fr.ceri.amiibo.webService

data class AmiiboResponse(
    // --------------------------- Liste des Amiibo récupérés ---------------------------
    var amiibo: MutableList<Amiibo>? = null
)
