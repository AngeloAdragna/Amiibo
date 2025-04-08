package fr.ceri.amiibo.webService

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // --------------------------- Récupérer les Amiibo par série de jeux ---------------------------
    @GET("/api/amiibo/")
    // Faire une requête comme vu en cours est vraiment plus long
    //suspend fun getAmiiboByGameSeries(@Query("gameseries") gameSeries: String): Response<AmiiboResponse>
    suspend fun getAmiiboByGameSeries(): Response<AmiiboResponse>


    // --------------------------- Récupérer toutes les séries de jeux ---------------------------
    @GET("/api/gameseries/")
    suspend fun getAllGameSeries(): Response<AmiiboHeader>
}
