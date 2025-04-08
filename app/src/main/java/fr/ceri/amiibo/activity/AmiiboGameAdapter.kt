package fr.ceri.amiibo.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.ceri.amiibo.databinding.AmiiboGameBinding
import fr.ceri.amiibo.utils.Display
import fr.ceri.amiibo.webService.AmiiboGame
import io.realm.RealmResults

// --------------------------- Adapter pour les jeux Amiibo ---------------------------
// Cet adapter est responsable de l'affichage des jeux Amiibo et de la gestion des cases à cocher.
class AmiiboGameAdapter(val amiiboGames: RealmResults<AmiiboGame>) :
    RecyclerView.Adapter<AmiiboGameViewHolder>() {
    private val amiiboGamesSelectedByCheckbox = mutableMapOf<String, Boolean>() // Liste pour stocker l'état des cases à cocher

    // --------------------------- Initialisation ---------------------------
    // Chargement des états des cases à cocher et gestion des changements dans les données
    init {
        for (amiiboGame in amiiboGames) {
            amiiboGamesSelectedByCheckbox[amiiboGame.name!!] = amiiboGame.isSelected // Initialiser l'état de chaque case à cocher
        }

        amiiboGames.addChangeListener { _, changeSet -> // Écouter les changements dans la liste des jeux
            for (change in changeSet.deletionRanges) {
                notifyItemRangeRemoved(change.startIndex, change.length) // Notifier la suppression des éléments
            }
            for (change in changeSet.insertionRanges) {
                notifyItemRangeInserted(change.startIndex, change.length) // Notifier l'ajout de nouveaux éléments
            }
            for (change in changeSet.changeRanges) {
                notifyItemRangeChanged(change.startIndex, change.length) // Notifier les changements d'éléments
            }
        }
    }

    // --------------------------- Création du ViewHolder ---------------------------
    // Crée un nouveau ViewHolder pour chaque élément de la liste
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmiiboGameViewHolder {
        val ui = AmiiboGameBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Lier le layout du ViewHolder
        return AmiiboGameViewHolder(ui)
    }

    // --------------------------- Nombre d'éléments ---------------------------
    // Retourne le nombre d'éléments à afficher dans le RecyclerView
    override fun getItemCount(): Int {
        return amiiboGames.size
    }

    // --------------------------- Lier les données au ViewHolder ---------------------------
    // Cette méthode lie les données d'un élément avec un ViewHolder
    override fun onBindViewHolder(holder: AmiiboGameViewHolder, position: Int) {
        val amiiboGame = amiiboGames[position] // Récupérer le jeu Amiibo à la position donnée
        holder.amiiboGame = amiiboGame // Associer l'objet amiiboGame au ViewHolder
        holder.checkBoxState = amiiboGamesSelectedByCheckbox // Associer l'état de la case à cocher

        // Synchroniser la case à cocher avec la base de données
        holder.ui.checkBox.isChecked = amiiboGame?.isSelected ?: false
        // Synchroniser la case à cocher avec l'état en mémoire
        val isChecked = amiiboGamesSelectedByCheckbox[amiiboGame?.name!!] ?: false
        holder.ui.checkBox.isChecked = isChecked
    }

    // --------------------------- Sélectionner/Désélectionner toutes les cases ---------------------------
    // Cette méthode permet de sélectionner ou désélectionner toutes les cases à cocher
    fun setAllCheckBoxSelected(isSelected: Boolean) {
        for (amiiboGame in amiiboGames) {
            amiiboGamesSelectedByCheckbox[amiiboGame.name!!] = isSelected // Mettre à jour l'état de chaque case à cocher
        }
        //updateVisibleCheckBoxes(recyclerView) // Mettre à jour les cases visibles
        notifyDataSetChanged() // Rafraîchir l'affichage
    }


    // --------------------------- Vérification de la sélection ---------------------------
    // Cette méthode vérifie si la sélection contient au moins 4 éléments et retourne les clés des éléments sélectionnés
    // --------------------------- Vérification de la sélection ---------------------------
    fun checkingSelectionIsValid(context: Context): List<String> {
        val selectedKeys = mutableListOf<String>() // Liste des clés des éléments sélectionnés
        var count = 0

        // Parcours de tous les éléments et ajout des éléments sélectionnés à la liste
        for (amiiboGame in amiiboGamesSelectedByCheckbox) {
            if (amiiboGame.value) { // Si l'élément est sélectionné
                selectedKeys.add(amiiboGame.key) // Ajouter la clé à la liste
                count++
            }
        }

        // Si au moins 4 éléments sont sélectionnés
        if (count >= 4) {
            Display.showLog("Il y a au moins 4 éléments avec la checkbox cochée")
            return selectedKeys // Retourner la liste des clés sélectionnées
        } else {
            // Affichage du Toast avec le message incluant le nombre d'éléments sélectionnés
            Display.showToast(context, "Il n'y a pas assez d'éléments avec la checkbox cochée. Seulement $count élément(s) sélectionné(s).")
        }
        return emptyList() // Retourner une liste vide si moins de 4 éléments sont sélectionnés
    }

}
