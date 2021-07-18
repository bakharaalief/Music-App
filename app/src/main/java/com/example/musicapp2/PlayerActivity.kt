package com.example.musicapp2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musicapp2.model.Music
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class PlayerActivity : AppCompatActivity() {
    private var playButtonHandle = false
    private var mBound : Boolean = false
    private lateinit var playbackService: PlaybackService

    //about view
    private lateinit var playbackButton : ImageView
    private lateinit var coverImage : ImageView
    private lateinit var songTitleText : TextView
    private lateinit var songArtistText : TextView
    private lateinit var nextButton : ImageView
    private lateinit var minuteDoneText : TextView
    private lateinit var minuteLeftText : TextView
    private lateinit var seekBar: SeekBar

    //handler
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    //song status
    private var songDuration = ""
    private var currentPosition = ""
    private var songTitle = ""
    private var songArtist = ""
    private var coverAlbum = 0

    //music playlist
    private val musicData = MusicData()
    private var musicCount = 0

    //custom Broadcast
    private val broadcast = CustomBroadcast()
    private lateinit var customReceiver : BroadcastReceiver
    private lateinit var localBroadcastManager: LocalBroadcastManager

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
        coverImage = findViewById(R.id.cover_image)
        songTitleText = findViewById(R.id.song_title)
        songArtistText = findViewById(R.id.song_artist)
        nextButton = findViewById(R.id.next_button)
        minuteDoneText = findViewById(R.id.minute_done)
        minuteLeftText = findViewById(R.id.minute_left)
        seekBar = findViewById(R.id.progressBar)

        //bind service
        Intent(this, PlaybackService::class.java).also{
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }

        //create channel
        createChannel()

        //set handler to update song
        runnable = Runnable {
            if(currentPosition == songDuration){
                nextMusic()
            }
            else{
                updateMusic()
                handler.postDelayed(runnable, 100)
            }
        }

        //playback button
        playbackButton.setOnClickListener {
            when(playButtonHandle){
                true -> pauseMusic()
                false -> playMusic()
            }
        }

        //next Button
        nextButton.setOnClickListener {
            nextMusic()
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
                handler.removeCallbacks(runnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                playbackService.stopTrackingTouch()
                handler.postDelayed(runnable, 100)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        //register customBrodcast
//        val filter = IntentFilter().apply {
//            addAction(PlaybackService.PAUSE_SONG)
//            addAction(PlaybackService.BEFORE_SONG)
//            addAction(PlaybackService.NEXT_SONG)
//        }
//        localBroadcastManager = LocalBroadcastManager.getInstance(this)
//        localBroadcastManager.registerReceiver(customReceiver, filter)
    }

    override fun onRestart() {
        super.onRestart()
        handler.postDelayed(runnable, 100)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        unbindService(connection)
//        localBroadcastManager.unregisterReceiver(customReceiver)
        mBound = false
        super.onDestroy()
    }

    private fun playMusic(){
        randomNum()
        val music = Music(
            musicData.musicTitle[musicCount],
            musicData.musicArtist[musicCount],
            musicData.musicAlbum[musicCount],
            musicData.musicList[musicCount],
        )
        playbackService.playMusic(music)

        //set album
        updateSongInfo()

        //set progressbar
        seekBar.progress = 0
        seekBar.max = playbackService.getDuration()

        //set duration
        currentPosition = songTimeFormat(playbackService.getCurrentPosition())
        songDuration = songTimeFormat(playbackService.getDuration())

        handler.postDelayed(runnable, 100)

        playButtonHandle = true
        playbackButton.setImageResource(R.drawable.ic_baseline_pause_circle)
    }

    private fun pauseMusic(){
        playbackService.pauseMusic()

        playButtonHandle = false
        playbackButton.setImageResource(R.drawable.ic_baseline_play_circle)
    }

    private fun nextMusic(){
        randomNum()
        val music = Music(
            musicData.musicTitle[musicCount],
            musicData.musicArtist[musicCount],
            musicData.musicAlbum[musicCount],
            musicData.musicList[musicCount],
        )
        playbackService.nextMusic(music)

        //update song info
        updateSongInfo()

        //set progressbar
        seekBar.progress = 0
        seekBar.max = playbackService.getDuration()

        //set duration
        currentPosition = songTimeFormat(playbackService.getCurrentPosition())
        songDuration = songTimeFormat(playbackService.getDuration())

        handler.postDelayed(runnable, 100)

        playButtonHandle = true
        playbackButton.setImageResource(R.drawable.ic_baseline_pause_circle)
    }

    private fun stopMusic(){
        handler.removeCallbacks(runnable)
        playbackService.stopMusic()

        playButtonHandle = false
        playbackButton.setImageResource(R.drawable.ic_baseline_play_circle)
    }

    private fun updateMusic(){
        currentPosition = songTimeFormat(playbackService.getCurrentPosition())
        songDuration = songTimeFormat(playbackService.getDuration())
        val minuteLeft = songTimeFormat(playbackService.getLeftPosition())

        seekBar.progress = playbackService.getCurrentPosition()
        minuteDoneText.text = currentPosition
        minuteLeftText.text = minuteLeft
    }

    private fun updateSongInfo(){
        songTitle = musicData.musicTitle[musicCount]
        songArtist = musicData.musicArtist[musicCount]
        coverAlbum = musicData.musicAlbum[musicCount]

        songTitleText.text = songTitle
        songArtistText.text = songArtist
        coverImage.setImageResource(coverAlbum)
    }

    private fun randomNum(){
        var number = Random.nextInt(musicData.musicList.size)
        while (number == musicCount){
            number = Random.nextInt(musicData.musicList.size)
        }

        musicCount = number
    }

    private fun songTimeFormat(input: Int) : String{
        val longType = input.toLong()

        return String.format("%02d : %02d",
            TimeUnit.MILLISECONDS.toMinutes(longType),
            TimeUnit.MILLISECONDS.toSeconds(longType) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(longType))
        )
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
}