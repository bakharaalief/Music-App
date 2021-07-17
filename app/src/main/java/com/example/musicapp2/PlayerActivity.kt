package com.example.musicapp2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.Observer

class PlayerActivity : AppCompatActivity() {
    private var isPlaying = false
    private var mBound : Boolean = false
    private lateinit var playbackService: PlaybackService

    //about view
    private lateinit var playbackButton : ImageView
    private lateinit var minuteDoneText : TextView
    private lateinit var minuteLeftText : TextView
    private lateinit var seekBar: SeekBar

    //connection to service
    private val connection = object : ServiceConnection{
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as PlaybackService.MyBinder
            playbackService = binder.getService()
            mBound = true
            Log.d("Service", "Udah Ngebound")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mBound = false
            Log.d("Service", "Udah nggak Ngebound")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        //setview
        playbackButton = findViewById(R.id.playback_button)
        minuteDoneText = findViewById(R.id.minute_done)
        minuteLeftText = findViewById(R.id.minute_left)
        seekBar = findViewById(R.id.progressBar)

        //bind service
        Intent(this, PlaybackService::class.java).also{
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }

        //playback button
        playbackButton.setOnClickListener {
            when(isPlaying){
                true -> pauseMusic()
                false -> playMusic()
            }
        }

        //seekbar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    playbackService.progressChange(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playbackService.startTrackingTouch()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                playbackService.stopTrackingTouch()
            }
        })
    }

    private fun playMusic(){
        playbackService.playMusic()

        //set progressbar
        seekBar.progress = 0
        seekBar.max = playbackService.getDuration()

        //song current duration
        playbackService.currentDuration.observe(this, Observer {
            minuteDoneText.text = it.toString()
            seekBar.progress = it
        })

        //song left duration
        playbackService.leftDuration.observe(this, Observer {
            minuteLeftText.text = it.toString()
        })

        isPlaying = true
        playbackButton.setImageResource(R.drawable.ic_baseline_pause_circle)
    }

    private fun pauseMusic(){
        playbackService.pauseMusic()

        isPlaying = false
        playbackButton.setImageResource(R.drawable.ic_baseline_play_circle)
    }

    private fun createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Musik channel"
            val descriptionText = "Musik channel untuk musik playback"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(PlaybackService.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        unbindService(connection)
        mBound = false
        super.onDestroy()
    }
}