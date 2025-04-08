package fr.ceri.amiibo.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import fr.ceri.amiibo.R
import fr.ceri.amiibo.webService.User
import io.realm.Realm
import io.realm.kotlin.where

class UserPreference {
    companion object {
        private const val PREFS_NAME = "ThemePrefs"
        private const val KEY_SELECTED_THEME = "selectedTheme"

        // Mapping des thèmes avec leurs noms
        private val themes = mapOf(
            "MarioTheme" to R.style.MarioTheme,
            "LuigiTheme" to R.style.LuigiTheme,
            "PeachTheme" to R.style.PeachTheme,
            "BowserTheme" to R.style.BowserTheme
        )

        // --------------------------- Charger le nom de l'utilisateur ---------------------------
        fun loadUserName(textView: TextView) {
            val realm = Realm.getDefaultInstance()
            val user = realm.where(User::class.java).findFirst()
            user?.name?.let { name ->
                textView.text = name  // Affiche le nom de l'utilisateur
            } ?: run {
                Display.showLog("Aucun utilisateur trouvé en base")
                textView.text = "user"  // Affichage par défaut
            }
        }

        // --------------------------- Mettre à jour le nom de l'utilisateur ---------------------------
        fun updateUserName(newName: String, textView: TextView) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAsync({ transaction ->
                val user = transaction.where<User>().findFirst()
                user?.name = newName
            }, {
                realm.close()
                textView.text = newName  // Mise à jour immédiate de la vue
                Display.showLog("Nom mis à jour avec succès : $newName")
            }, {
                Display.showLog("Erreur lors de la mise à jour du nom")
                realm.close()
            })
        }

        // --------------------------- Charger les statistiques de l'utilisateur ---------------------------
        fun loadUserStat(statName: String, textView: TextView? = null): Int {
            val realm = Realm.getDefaultInstance()
            val user = realm.where(User::class.java).findFirst()
            var statValue = 0

            user?.stats?.let { stats ->
                statValue = when (statName) {
                    "level" -> stats.level
                    "nbPoints" -> stats.nbPoints
                    "pointsLevel" -> stats.pointsLevel
                    "score" -> stats.score
                    "gamesPlayed" -> stats.gamesPlayed
                    "correctAnswers" -> stats.correctAnswers
                    "wrongAnswers" -> stats.wrongAnswers
                    else -> 0
                }
            }

            realm.close()
            textView?.text = statValue.toString()
            return statValue
        }

        // --------------------------- Mettre à jour les statistiques de l'utilisateur ---------------------------
        fun updateUserStat(newStatValue: Any, nameStat: String, textView: TextView? = null) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAsync({ transaction ->
                val user = transaction.where(User::class.java).findFirst()
                user?.stats?.let { stats ->
                    when (nameStat) {
                        "level" -> stats.level = newStatValue as Int
                        "nbPoints" -> stats.nbPoints = newStatValue as Int
                        "pointsLevel" -> stats.pointsLevel = newStatValue as Int
                        "score" -> stats.score = newStatValue as Int
                        "gamesPlayed" -> stats.gamesPlayed = newStatValue as Int
                        "correctAnswers" -> stats.correctAnswers = newStatValue as Int
                        "wrongAnswers" -> stats.wrongAnswers = newStatValue as Int
                    }
                }
            }, {
                realm.close()
                textView?.text = newStatValue.toString()
            }, {
                realm.close()
            })
        }

        // --------------------------- Charger le thème de l'utilisateur ---------------------------
        fun loadUserTheme(activity: Activity) {
            val realm = Realm.getDefaultInstance()
            val user = realm.where<User>().findFirst()
            val themeName = user?.theme ?: "MarioTheme" // Par défaut : Mario
            realm.close()

            // Appliquer le thème
            val themeResId = themes[themeName] ?: R.style.MarioTheme
            activity.setTheme(themeResId)

            // Sauvegarder le thème dans les préférences
            val sharedPreferences: SharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(KEY_SELECTED_THEME, themeName).apply()
        }

        // --------------------------- Mettre à jour le thème de l'utilisateur ---------------------------
        fun updateUserTheme(activity: Activity, themeName: String) {
            val realm = Realm.getDefaultInstance()

            realm.executeTransactionAsync({ transaction ->
                val user = transaction.where<User>().findFirst()
                user?.theme = themeName
            }, {
                realm.close()

                // Sauvegarder dans les préférences
                val sharedPreferences: SharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                sharedPreferences.edit().putString(KEY_SELECTED_THEME, themeName).apply()

                // Recharger le thème et recréer l'activité
                loadUserTheme(activity)
                activity.recreate()
            }, {
                Display.showLog("Erreur lors du changement de thème")
                realm.close()
            })
        }

        // --------------------------- Charger l'icône de l'utilisateur ---------------------------
        fun loadUserIcon(activity: Activity, imageView: ImageView) {
            val realm = Realm.getDefaultInstance()
            val user = realm.where<User>().findFirst()
            realm.close()

            user?.icon?.let { iconName ->
                val resourceId = activity.resources.getIdentifier(iconName, "drawable", activity.packageName)
                if (resourceId != 0) {
                    imageView.setImageResource(resourceId)
                } else {
                    Display.showLog("Icône introuvable : $iconName")
                    imageView.setImageResource(R.drawable.circular_bg) // Icône par défaut
                }
            } ?: run {
                Display.showLog("Aucun utilisateur trouvé en base")
            }
        }

        // --------------------------- Mettre à jour l'icône dans la base Realm ---------------------------
        fun updateUserIcon(activity: Activity, iconName: String, imageView: ImageView) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAsync({ transaction ->
                val user = transaction.where<User>().findFirst()
                if (user != null) {
                    user.icon = iconName
                } else {
                    Display.showLog("Aucun utilisateur trouvé en base")
                }
            }, {
                realm.close()
                loadUserIcon(activity, imageView)
            }, {
                Display.showLog("Erreur lors de la mise à jour de l'icône")
                realm.close()
            })
        }

        // --------------------------- Activer l'Edge-to-Edge pour la barre de statut ---------------------------
        fun enableEdgeToEdgeTop(window: Window) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
    }
}
