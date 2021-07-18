package com.example.musicapp2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class CustomBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            PlaybackService.NEXT_SONG -> {
                Toast.makeText(context, "Makan Nasi Goreng", Toast.LENGTH_SHORT).show()
            }
        }
    }
}