package com.example.musicapp2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class CustomBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intentData = Intent(PlayerActivity.INTENT_ACTIVITY)

        when(intent?.action){
            PlaybackService.NEXT_SONG -> {
                Log.d("Service", "Bekerja")
                context?.sendBroadcast(intentData.apply {
                    putExtra("isi", PlayerActivity.NEXT_SONG_ACTIVITY)
                })
            }
            PlaybackService.PAUSE_SONG -> {
                context?.sendBroadcast(intentData.apply {
                    putExtra("isi", PlayerActivity.PAUSE_SONG_ACTIVITY)
                })
            }
            PlaybackService.PLAY_SONG -> {
                context?.sendBroadcast(intentData.apply {
                    putExtra("isi", PlayerActivity.PLAY_SONG_ACTIVITY)
                })
            }
        }
    }
}