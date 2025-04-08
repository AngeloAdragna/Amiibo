package fr.ceri.amiibo.utils

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("MusicPrefs", MODE_PRIVATE)
        // Observer le cycle de vie global de l'application
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        // Lire la dernière musique jouée ou utiliser celle par défaut
        val currentMusicFile = sharedPreferences.getString("currentMusicFile", "lost_woods_zelda") ?: "lost_woods_zelda"
        playMusic(currentMusicFile)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val musicFile = intent?.getStringExtra("MUSIC_FILE")
        when (action) {
            "PLAY" -> musicFile?.let {
                if (!sharedPreferences.getBoolean("isMusicOn", false)) playMusic(it)
                else resumeMusic()
            }
            "STOP" -> stopMusic()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopMusic()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --------------------------- Contrôle de la musique ---------------------------
    private fun playMusic(musicFile: String) {
        // Ne pas relancer si un MediaPlayer est déjà actif
        if (mediaPlayer != null) return

        // Charger la ressource depuis le dossier raw
        val resId = resources.getIdentifier(musicFile, "raw", packageName)
        if (resId == 0) {
            Log.e("MusicService", "Fichier introuvable: $musicFile")
            return
        }
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            isLooping = true
            start()
        }
        saveMusicState(true, musicFile)
    }

    private fun resumeMusic() {
        mediaPlayer?.let {
            it.start()
            saveMusicState(true, sharedPreferences.getString("currentMusicFile", "lost_woods_zelda") ?: "")
        }
    }

    private fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        saveMusicState(false, sharedPreferences.getString("currentMusicFile", "lost_woods_zelda") ?: "")
    }

    // --------------------------- Gestion des préférences ---------------------------

    private fun saveMusicState(isPlaying: Boolean, musicFile: String) {
        sharedPreferences.edit().apply {
            putBoolean("isMusicOn", isPlaying)
            putString("currentMusicFile", musicFile)
            apply()
        }
    }

    // --------------------------- Observateur de cycle de vie ---------------------------
    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> pauseMusic()
            Lifecycle.Event.ON_RESUME -> resumeMusic()
            else -> {}
        }
    }
}
