package com.example.musicapp2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CustomBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            PlaybackService.SONG_DATA -> {
                val songDuration = intent.getIntExtra(PlaybackService.SONG_DURATION, 0)
                val songCurrentDuration = intent.getIntExtra(PlaybackService.SONG_CURRENT_DURATION, 0)
            }
        }
    }
}