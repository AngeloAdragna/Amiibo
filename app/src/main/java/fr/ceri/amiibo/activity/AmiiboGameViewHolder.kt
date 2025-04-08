package fr.ceri.amiibo.activity

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import fr.ceri.amiibo.databinding.AmiiboGameBinding
import fr.ceri.amiibo.webService.AmiiboGame

// --------------------------- ViewHolder pour les jeux Amiibo ---------------------------
// Ce ViewHolder est responsable de l'affichage d'un jeu Amiibo dans le RecyclerView et de la gestion des clics sur la case à cocher.
class AmiiboGameViewHolder(val ui: AmiiboGameBinding) : RecyclerView.ViewHolder(ui.root) {

    // --------------------------- Variables et états ---------------------------
    // La carte pour stocker l'état des cases à cocher
    var checkBoxState: MutableMap<String, Boolean>? = null
    var amiiboGameKey: String? = null
    var amiiboGame: AmiiboGame?
        get() = null // Ne retourne pas de valeur ici
        set(amiiboGame) {
            if (amiiboGame == null) return // Si l'objet est null, on ne fait rien
            amiiboGameKey = amiiboGame.name // Utilise le nom du jeu Amiibo comme clé
            ui.textName.text = amiiboGame.name // Afficher le nom du jeu dans l'interface
            // Initialiser l'état de la case à cocher en fonction des valeurs dans checkBoxState
            ui.checkBox.isChecked = checkBoxState?.get(amiiboGameKey) ?: false // Si l'état n'est pas trouvé, la case est décochée
        }

    // --------------------------- Initialisation du clic ---------------------------
    // Initialiser l'écouteur de clic sur la case à cocher
    init {

        ui.checkBox.setOnClickListener(this::onClick)
    }

    // --------------------------- Méthode de gestion du clic ---------------------------
    // Cette méthode est appelée lorsque l'utilisateur clique sur la case à cocher
    private fun onClick(view: View?) {
        checkBoxState?.set(amiiboGameKey!!, ui.checkBox.isChecked) // Mettre à jour l'état de la case dans checkBoxState
    }
}
