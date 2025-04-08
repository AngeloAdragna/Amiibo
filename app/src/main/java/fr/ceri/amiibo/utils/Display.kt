package fr.ceri.amiibo.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

class Display {
    companion object {
        private const val TAG = "Amiibo"  // Tag pour les logs
        // --------------------------- Afficher un message dans LogCat ---------------------------
        fun showLog(message: String) {
            Log.d(TAG, message)  // Enregistre le message dans le LogCat avec le tag "Amiibo"
        }
        // --------------------------- Afficher un message en bas de l'écran (Toast) ---------------------------
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()  // Affiche un Toast avec le message
        }
    }
}
