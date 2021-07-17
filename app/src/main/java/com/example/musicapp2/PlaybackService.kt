package com.example.musicapp2

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class PlaybackService : Service() {

    companion object {
        //for notification
        const val CHANNEL_ID = "PRIMARY_CHANNEL"
        const val NOTIF_ID = 1

        //action for intent
        const val PLAY_MUSIC = "PLAY_MUSIC"
        const val PAUSE_MUSIC = "PAUSE_MUSIC"

        //brodcast receiver data
        const val SONG_DATA = "com.example.musicapp2.SONG_DATA"
        const val SONG_DURATION = "SONG_DURATION"
        const val SONG_CURRENT_DURATION = "SONG_CURRENT_DURATION"
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false

    private val mBinder = MyBinder()

    inner class MyBinder() : Binder() {
        fun getService() : PlaybackService = this@PlaybackService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        Log.d("Service", "Sevice Kebuat")
    }

    override fun onDestroy() {
        Log.d("Service", "Sevice Setop")
        stopNotif()
        super.onDestroy()
    }

    private fun notifBuilder() : NotificationCompat.Builder{
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun showNotif(){
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = notifBuilder()
            .setContentTitle("My notification")
            .setContentText("Hello World!")
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)){
            notify(NOTIF_ID, builder.build())
        }
    }

    private fun stopNotif(){
        with(NotificationManagerCompat.from(this)){
            cancel(NOTIF_ID)
        }
    }

    fun getDuration() : Int {
        return mediaPlayer.duration
    }

    fun getCurrentPosition() : Int {
        return mediaPlayer.currentPosition
    }

    fun getLeftPosition() : Int{
        return mediaPlayer.duration - mediaPlayer.currentPosition
    }

    fun playMusic(song : Int){
        //play from pause
        if(isPlaying){
            mediaPlayer.start()
        }

        //first time play
        else{
            mediaPlayer = MediaPlayer.create(this, song)
            mediaPlayer.start()
            isPlaying = true
        }
    }

    fun pauseMusic(){
        mediaPlayer.pause()
//        stopNotif()
    }

    fun stopMusic(){
        mediaPlayer.stop()
        Log.d("Service", "Musik Stop")
    }

    fun nextMusic(song : Int){
        isPlaying = false
        mediaPlayer.release()
        playMusic(song)
    }

    fun startTrackingTouch(){
        mediaPlayer.pause()
    }

    fun progressChange(progress : Int){
        mediaPlayer.seekTo(progress)
    }

    fun stopTrackingTouch(){
        mediaPlayer.start()
    }
}