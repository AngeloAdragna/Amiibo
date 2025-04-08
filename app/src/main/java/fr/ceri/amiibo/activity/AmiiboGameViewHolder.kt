package fr.ceri.amiibo.activity

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import fr.ceri.amiibo.databinding.AmiiboGameBinding
import fr.ceri.amiibo.webService.AmiiboGame

// --------------------------- ViewHolder pour les jeux Amiibo ---------------------------
// Responsable de l'affichage d'un jeu Amiibo dans le RecyclerView et de la gestion des clics sur la case à cocher.
class AmiiboGameViewHolder(val ui: AmiiboGameBinding) : RecyclerView.ViewHolder(ui.root) {

    // --------------------------- Variables et états ---------------------------
    var checkBoxState: MutableMap<String, Boolean>? = null  // La map pour stocker localement l'état des cases à cocher
    var amiiboGameKey: String? = null
    var amiiboGame: AmiiboGame?
        get() = null
        set(amiiboGame) { // Initialiser l'état de la case à cocher en fonction des valeurs dans checkBoxState
            if (amiiboGame == null) return // Si l'objet est null, on ne fait rien
            amiiboGameKey = amiiboGame.name // Utilise le nom du jeu Amiibo comme clé
            ui.textName.text = amiiboGame.name // Afficher le nom du jeu dans l'interface
            ui.checkBox.isChecked = checkBoxState?.get(amiiboGameKey) ?: false // Si l'état n'est pas trouvé, la case est décochée
        }

    // --------------------------- Initialisation du clic ---------------------------
    init {
        ui.checkBox.setOnClickListener(this::onClick)
    }

    // --------------------------- Méthode de gestion du clic ---------------------------
    private fun onClick(view: View?) { // Mettre à jour l'état de la case dans checkBoxState
        checkBoxState?.set(amiiboGameKey!!, ui.checkBox.isChecked)
    }
}
