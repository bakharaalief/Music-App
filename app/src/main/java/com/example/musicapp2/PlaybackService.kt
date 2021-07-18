package com.example.musicapp2

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.musicapp2.model.Music

class PlaybackService : Service() {

    companion object {
        //for notification
        const val CHANNEL_ID = "PRIMARY_CHANNEL"
        const val NOTIF_ID = 1

        //brodcast receiver data
        const val NEXT_SONG = "com.example.musicapp2.NEXT_SONG"
        const val BEFORE_SONG = "com.example.musicapp2.BEFORE_SONG"
        const val PAUSE_SONG = "com.example.musicapp2.PAUSE_SONG"
        const val PLAY_SONG = "com.example.musicapp2.PLAY_SONG"
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false
    private lateinit var music: Music

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
        stopForeground(true)
        super.onDestroy()
    }

    @SuppressLint("NewApi")
    private fun notifBuilder() : NotificationCompat.Builder{

        //media session compat
        val mediaSession = MediaSessionCompat(this, "PlaybackService")
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                mediaStyle
            )
    }

    private fun playNotif(){
        //artwork
        val artWork = BitmapFactory.decodeResource(resources, music.album)

        //content intent
        val intent = Intent(this, PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //before action
        val beforeSongIntent = Intent(this, CustomBroadcast::class.java).apply {
            action = BEFORE_SONG
        }
        val beforeSongPendingIntent = PendingIntent.getBroadcast(this, 0, beforeSongIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //pause action
        val pauseSongIntent = Intent(this, CustomBroadcast::class.java).apply {
            action = PAUSE_SONG
        }
        val pauseSongPendingIntent = PendingIntent.getBroadcast(this, 0, pauseSongIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //next action
        val nextSongIntent = Intent(this, CustomBroadcast::class.java).apply {
            action = NEXT_SONG
        }
        val nextSongPendingIntent = PendingIntent.getBroadcast(this, 0, nextSongIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = notifBuilder()
            .setLargeIcon(artWork)
            .setContentTitle(music.title)
            .setContentText(music.artist)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_baseline_fast_rewind_24, "before_song_intent", beforeSongPendingIntent)
            .addAction(R.drawable.ic_baseline_pause_24, "pause_song_intent", pauseSongPendingIntent)
            .addAction(R.drawable.ic_baseline_fast_forward_24, "next_song_intent", nextSongPendingIntent)
            .setOngoing(true)

        //music use foreground to start notif
        startForeground(NOTIF_ID, builder.build())
    }

    private fun pauseNotif(){
        stopForeground(true)

        //artwork
        val artWork = BitmapFactory.decodeResource(resources, music.album)

        //content intent
        val intent = Intent(this, PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //before action
        val beforeSongIntent = Intent(this, CustomBroadcast::class.java).apply {
            action = BEFORE_SONG
        }
        val beforeSongPendingIntent = PendingIntent.getBroadcast(this, 0, beforeSongIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //pause action
        val playSongIntent = Intent(this, CustomBroadcast::class.java).apply {
            action = PLAY_SONG
        }
        val playSongPendingIntent = PendingIntent.getBroadcast(this, 0, playSongIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //next action
        val nextSongIntent = Intent(this, CustomBroadcast::class.java).apply {
            action = NEXT_SONG
        }
        val nextSongPendingIntent = PendingIntent.getBroadcast(this, 0, nextSongIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = notifBuilder()
            .setLargeIcon(artWork)
            .setContentTitle(music.title)
            .setContentText(music.artist)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_baseline_fast_rewind_24, "before_song_intent", beforeSongPendingIntent)
            .addAction(R.drawable.ic_baseline_play_arrow_24, "play_song_intent", playSongPendingIntent)
            .addAction(R.drawable.ic_baseline_fast_forward_24, "next_song_intent", nextSongPendingIntent)

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

    fun isPlaying() : Boolean{
        return isPlaying
    }

    fun playMusic(musicInput : Music){
        //play from pause
        if(isPlaying){
            mediaPlayer.start()
        }

        //first time play
        else{
            music = musicInput
            mediaPlayer.release()
            mediaPlayer = MediaPlayer.create(this, music.data)
            mediaPlayer.start()
            isPlaying = true
        }

        playNotif()
    }

    fun pauseMusic(){
        mediaPlayer.pause()
        pauseNotif()
    }

    fun stopMusic(){
        stopForeground(true)
        mediaPlayer.stop()
        Log.d("Service", "Musik Stop")
    }

    fun nextMusic(music : Music){
        isPlaying = false
        mediaPlayer.release()
        playMusic(music)
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