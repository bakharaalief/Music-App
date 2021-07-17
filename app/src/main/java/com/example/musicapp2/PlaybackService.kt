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
import androidx.lifecycle.MutableLiveData

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
    private var songDuration = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val mBinder = MyBinder()

    private val _currentDuration : MutableLiveData<Int> = MutableLiveData<Int>()
    val currentDuration = _currentDuration

    private val _leftDuration : MutableLiveData<Int> = MutableLiveData<Int>()
    val leftDuration = _leftDuration

    inner class MyBinder() : Binder() {
        fun getService() : PlaybackService = this@PlaybackService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Service", "Sevice Kebuat")

        //set media player
        mediaPlayer = MediaPlayer.create(this, R.raw.racing_into_the_night)
        _currentDuration.value = mediaPlayer.currentPosition
        songDuration = mediaPlayer.duration
        _leftDuration.value = songDuration

        Log.d("Service", mediaPlayer.duration.toString())

        //runnable to update currentDuration
        runnable = Runnable {
            _currentDuration.value = mediaPlayer.currentPosition
            _leftDuration.value = songDuration - mediaPlayer.currentPosition

            Log.d("Service", mediaPlayer.currentPosition.toString())

            handler.postDelayed(runnable, 100)
        }
    }

    override fun onDestroy() {
        Log.d("Service", "Sevice Setop")
        handler.removeCallbacks(runnable)
//        stopNotif()
        super.onDestroy()
    }

//    private fun notifBuilder() : NotificationCompat.Builder{
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//    }
//
//    private fun showNotif(){
//        val intent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val builder = notifBuilder()
//            .setContentTitle("My notification")
//            .setContentText("Hello World!")
//            .setContentIntent(pendingIntent)
//
//        with(NotificationManagerCompat.from(this)){
//            notify(NOTIF_ID, builder.build())
//        }
//    }

//    private fun stopNotif(){
//        with(NotificationManagerCompat.from(this)){
//            cancel(NOTIF_ID)
//        }
//    }

    fun getDuration() : Int {
        return songDuration
    }

    fun playMusic(){
        if(isPlaying){
            _currentDuration.value = mediaPlayer.currentPosition
            mediaPlayer.start()
        }
        else{
            mediaPlayer.start()
            isPlaying = true
        }

        handler.postDelayed(runnable, 100)
    }

    fun pauseMusic(){
        mediaPlayer.pause()
        handler.removeCallbacks(runnable)
        _currentDuration.value = mediaPlayer.currentPosition
        _leftDuration.value = songDuration - mediaPlayer.currentPosition
//        stopNotif()
    }

    fun stopMusic(){
        mediaPlayer.stop()
        mediaPlayer.release()
        _currentDuration.value = mediaPlayer.duration
        _leftDuration.value = 0
    }

    fun startTrackingTouch(){
        pauseMusic()
        handler.removeCallbacks(runnable)
    }

    fun progressChange(progress : Int){
        _currentDuration.value = progress
        mediaPlayer.seekTo(progress)
        _leftDuration.value = songDuration - mediaPlayer.currentPosition
    }

    fun stopTrackingTouch(){
        playMusic()
        handler.postDelayed(runnable, 100)
    }
}