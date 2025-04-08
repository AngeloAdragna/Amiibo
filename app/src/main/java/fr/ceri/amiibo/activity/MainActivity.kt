package fr.ceri.amiibo.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.ceri.amiibo.R
import fr.ceri.amiibo.utils.UserPreference
import fr.ceri.amiibo.databinding.ActivityMainBinding
import fr.ceri.amiibo.utils.Display
import fr.ceri.amiibo.webService.Amiibo
import fr.ceri.amiibo.webService.AmiiboGame
import fr.ceri.amiibo.webService.AmiiboHeader
import fr.ceri.amiibo.webService.ApiClient
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --------------------------- Activité principale ---------------------------
// La MainActivity est l'écran principal qui gère la liste des jeux Amiibo, avec la possibilité de sélectionner des cases à cocher.
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var realm: Realm
    private var allSelected = false // Variable pour savoir si toutes les cases sont sélectionnées

    // --------------------------- Méthode onCreate ---------------------------
    // Initialisation de l'interface et des composants de l'application
    override fun onCreate(savedInstanceState: Bundle?) {
        UserPreference.loadUserTheme(this) // Applique les préférences utilisateur
        UserPreference.enableEdgeToEdgeTop(window)
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)
        setSupportActionBar(ui.toolbar)

        // --------------------------- Initialisation de Realm ---------------------------
        initializeRealm()

        // --------------------------- Configuration du RecyclerView ---------------------------
        setupRecyclerView()
    }

    // --------------------------- Méthode initializeRealm ---------------------------
    private fun initializeRealm() {
        realm = Realm.getDefaultInstance()
        fetchAllGameSeries(realm) // Récupère toutes les séries de jeux depuis Realm
    }

    // --------------------------- Méthode setupRecyclerView ---------------------------
    // Configure le RecyclerView pour afficher les éléments de la liste des jeux Amiibo
    private fun setupRecyclerView() {
        val gameSeries = realm.where(AmiiboGame::class.java)
            .distinct("name")
            .sort("name", Sort.ASCENDING) // Trie les jeux par nom dans l'ordre croissant

        // Ecouteur pour détecter les changements dans les résultats
        gameSeries.findAllAsync().addChangeListener { results ->
            if (results.isLoaded) {
                // Créer et configurer l'adaptateur après le chargement des données
                val adapter = AmiiboGameAdapter(results)
                ui.recycler.adapter = adapter
                // --------------------------- Mise en place de l'adaptateur ---------------------------
                ui.recycler.adapter = adapter
                ui.recycler.setHasFixedSize(true) // Optimise le RecyclerView
                // --------------------------- Configuration du LayoutManager ---------------------------
                val lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
                ui.recycler.layoutManager = lm // Définit le LayoutManager pour organiser les éléments

            } else {
                Display.showLog("Les données ne sont pas encore prêtes.")
            }
        }
    }

    // --------------------------- Méthode fetchAllGameSeries ---------------------------
    // Récupère les séries de jeux depuis l'API
    private fun fetchAllGameSeries(realm: Realm) {
        Display.showLog("Fetching all game series") // Log pour afficher la récupération des séries
        launch(Dispatchers.Main) {
            try {
                // Appel API pour récupérer toutes les séries de jeux
                val response = ApiClient.apiService.getAllGameSeries()
                if (response.isSuccessful && response.body() != null) {
                    val content = response.body() ?: AmiiboHeader()
                    initGameSeries(realm, content) // Initialisation des séries de jeux dans la base de données
                } else {
                    Toast.makeText(applicationContext, "Erreur : La réponse n'est pas réussie", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Erreur lors de la récupération : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --------------------------- Méthode initGameSeries ---------------------------
    // Insère les nouvelles séries de jeux dans la base de données Realm
    private fun initGameSeries(realm: Realm, content: AmiiboHeader) {
        realm.executeTransactionAsync { transactionRealm ->
            val existingGames = transactionRealm.where(AmiiboGame::class.java).findAll()
            val existingKeys = existingGames.map { it.key }.toSet()
            val existingNames = existingGames.mapNotNull { it.name }.toSet()
            val newGamesMap = mutableMapOf<String, AmiiboGame>()

            content.amiibo?.forEach { amiibo ->
                val name = amiibo.name ?: return@forEach
                if (amiibo.key !in existingKeys && name !in newGamesMap && name !in existingNames) {
                    val newGame = transactionRealm.createObject(AmiiboGame::class.java, amiibo.key)
                    newGame.name = name
                    newGame.isSelected = false
                    newGamesMap[name] = newGame
                }
            }

            val selectedGames = existingGames.filter { it.isSelected }

            if (selectedGames.isEmpty()) {
                newGamesMap.values.shuffled().take(4).forEach { game ->
                    game.isSelected = true
                }
            } else {
                selectedGames.forEach { game ->
                    val existingGame = newGamesMap[game.name]
                    if (existingGame != null) {
                        existingGame.isSelected = true
                    }
                }
            }
        }
    }

    // --------------------------- Méthode onCreateOptionsMenu ---------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_getamiibos, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // --------------------------- Méthode onOptionsItemSelected ---------------------------
    // Gère les actions des éléments de menu (retour, sélectionner tout, valider)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.back -> {
                openHomeActivity() // Ouvre l'écran principal
                true
            }
            R.id.selectAll -> {
                allSelected = !allSelected
                item.setIcon(
                    if (allSelected) R.drawable.all_selected_icon
                    else R.drawable.none_selected_icon
                )
                // Met à jour l'état de toutes les cases à cocher
                (ui.recycler.adapter as? AmiiboGameAdapter)?.setAllCheckBoxSelected(allSelected)
                true
            }
            R.id.validSelection -> {
                // Appel de la fonction checkingSelectionIsValid et récupération des clés sélectionnées
                val selectedKeys = (ui.recycler.adapter as? AmiiboGameAdapter)?.checkingSelectionIsValid(this)
                // Affiche des messages en fonction de la sélection
                if (selectedKeys.isNullOrEmpty()) {
                    Display.showLog("Pas assez d'éléments sélectionnés.")
                } else {
                    fetchAllAmiiboSelected(selectedKeys) {
                        val intentGameActivity = Intent(this, GameActivity::class.java)
                        startActivity(intentGameActivity)
                        finish()
                        updateAmiiboGameCheckedState(selectedKeys)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --------------------------- Méthode fetchAllAmiiboSelected ---------------------------
    // Récupère tous Amiibo et les insère ceux correspondant au gameSeries sélectionnée,
    //      dans la base de données de manière optimisé pour un nombre plus ou mons conséquent
    private fun fetchAllAmiiboSelected(amiiboGameKeys: List<String>, onComplete: () -> Unit) {
        launch(Dispatchers.IO) {
            val realmInstance = Realm.getDefaultInstance()

            try {
                // Supprime tous les Amiibos existants dans la base de données
                realmInstance.executeTransaction { it.where(Amiibo::class.java).findAll().deleteAllFromRealm() }

                val response = ApiClient.apiService.getAmiiboByGameSeries()
                val allAmiibos = response.body()?.amiibo ?: emptyList()
                val listAmiiboSelected: MutableList<Amiibo> = mutableListOf()

                // Filtrer et ajouter dans listAmiiboSelected
                for (amiibo in allAmiibos) {
                    if (amiibo.gameSeries in amiiboGameKeys) {
                        listAmiiboSelected.add(amiibo)
                    }
                }
                for (amiibogame  in amiiboGameKeys) {
                    Display.showLog("Nom du jeu : ${amiibogame}")
                }
                Display.showLog("Nombre d'amiibo : ${listAmiiboSelected.size}")
                // Insère les Amiibos récupérés dans la base de données
                realmInstance.executeTransaction { transactionRealm ->
                    listAmiiboSelected.forEach { amiibo ->
                        // Insère l'amiibo seulement s'il n'existe pas déjà
                        transactionRealm.where(Amiibo::class.java)
                            .equalTo("head", amiibo.head)
                            .findFirst() ?: Amiibo.create(
                            transactionRealm,
                            amiibo.head ?: "Clé inconnue",
                            amiibo.name ?: "Nom inconnu",
                            amiibo.gameSeries ?: "Série inconnue",
                            amiibo.image ?: "Image inconnue"
                        )
                    }
                }
                //attend la fin de la requête pour ouvrir GameActivity
                withContext(Dispatchers.Main) { onComplete() }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Display.showToast(applicationContext, "Erreur : ${e.message}")
                }
            } finally {
                realmInstance.close() // Ferme Realm pour éviter les fuites de mémoire
            }
        }
    }

    // --------------------------- Méthode updateAmiiboGameCheckedState ---------------------------
    // Met à jour l'état de sélection des jeux dans Realm
    private fun updateAmiiboGameCheckedState(amiiboGameKeys: List<String>) {
        realm.executeTransactionAsync { transactionRealm ->
            // Réinitialise l'état des cases à cocher
            val allAmiiboGames = transactionRealm.where(AmiiboGame::class.java).findAll()
            allAmiiboGames.forEach { amiiboGame ->
                amiiboGame.isSelected = false
            }
            // Met à jour les éléments spécifiés dans amiiboGameKeys
            amiiboGameKeys.forEach { key ->
                val amiiboGame = transactionRealm.where(AmiiboGame::class.java)
                    .equalTo(AmiiboGame.NAME, key)
                    .findFirst()
                amiiboGame?.isSelected = true
            }
        }
    }

    // --------------------------- Méthode openHomeActivity ---------------------------
    // Ouvre l'écran d'accueil
    private fun openHomeActivity() {
        val intentHomeActivity = Intent(this, HomeActivity::class.java)
        startActivity(intentHomeActivity)
        finish()
    }
}