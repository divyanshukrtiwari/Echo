package com.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.echo.CurrentSongHelper
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.activities.MainActivity
import com.example.echo.databases.EchoDatabase
import com.example.echo.fragments.SongPlayingFragment.Staticated.onSongComplete
import com.example.echo.fragments.SongPlayingFragment.Staticated.playNext
import com.example.echo.fragments.SongPlayingFragment.Staticated.processInformation
import com.example.echo.fragments.SongPlayingFragment.Staticated.updateTextViews
import com.example.echo.fragments.SongPlayingFragment.Statified.audioV
import com.example.echo.fragments.SongPlayingFragment.Statified.audioVisualization
import com.example.echo.fragments.SongPlayingFragment.Statified.currentPosition
import com.example.echo.fragments.SongPlayingFragment.Statified.currentSongHelper
import com.example.echo.fragments.SongPlayingFragment.Statified.endTimeText
import com.example.echo.fragments.SongPlayingFragment.Statified.fab
import com.example.echo.fragments.SongPlayingFragment.Statified.favouriteContent
import com.example.echo.fragments.SongPlayingFragment.Statified.fetchSongs
import com.example.echo.fragments.SongPlayingFragment.Statified.glView
import com.example.echo.fragments.SongPlayingFragment.Statified.mediaPlayer
import com.example.echo.fragments.SongPlayingFragment.Statified.myActivity
import com.example.echo.fragments.SongPlayingFragment.Statified.nextImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.playpauseImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.previousImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.seekbar
import com.example.echo.fragments.SongPlayingFragment.Statified.shuffleImageButton
import com.example.echo.fragments.SongPlayingFragment.Statified.songArtistView
import com.example.echo.fragments.SongPlayingFragment.Statified.songTitleView
import com.example.echo.fragments.SongPlayingFragment.Statified.startTimeText
import com.example.echo.fragments.SongPlayingFragment.Statified.updateSongTime
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.echo.fragments.SongPlayingFragment.Statified.loopImageButton as statifiedLoopImageButton



/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    object Statified{

        var myActivity : Activity?= null
        var mediaPlayer : MediaPlayer?= null
        var startTimeText: TextView?= null
        var endTimeText: TextView?= null
        var playpauseImageButton: ImageButton?= null
        var previousImageButton: ImageButton?= null
        var nextImageButton: ImageButton?= null
        var loopImageButton: ImageButton?= null
        var seekbar: SeekBar?= null
        var songArtistView: TextView?= null
        var songTitleView: TextView?= null
        var shuffleImageButton: ImageButton?= null

        var currentPosition: Int = 0
        var fetchSongs:ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization?= null
        var glView: GLAudioVisualizationView?= null
        var audioV: AudioVisualization? = null

        var fab : ImageButton?= null

        var favouriteContent : EchoDatabase?= null

        var mSensorManager : SensorManager?= null
        var mSensorListner : SensorEventListener?= null

        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.getCurrentPosition()
                startTimeText!!.text = String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()!!),
                    TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong()!!) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())))
                seekbar?.setProgress(getCurrent.toInt())

                Handler().postDelayed(this, 1000)
            }
        }


    }

    object Staticated{
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"





        fun onSongComplete() {
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                currentSongHelper?.isPlaying = true
            } else {
                if (currentSongHelper?.isLoop as Boolean) {
                    currentSongHelper?.isPlaying = true
                    var nextSong = fetchSongs?.get(currentPosition)

                    currentSongHelper?.songTitle = nextSong?.songTitle
                    currentSongHelper?.songPath = nextSong?.songData
                    currentSongHelper?.currentPosition = currentPosition
                    currentSongHelper?.songId = nextSong?.songID as Long

                    updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

                    mediaPlayer?.reset()

                    try {
                        mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(currentSongHelper?.songPath))
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                        processInformation(mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    playNext("PlayNextNormal")
                }
            }
            if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
            } else {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))

            }
        }

        fun playNext(check: String){
            if(check.equals("PlayNextNormal", true)){
                currentPosition = currentPosition + 1
            }else if(check.equals("PlayNextLikeNormalShuffle", true)){

                var randomObject = Random()
                var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1)as Int)
                currentPosition = randomPosition
            }
            if(currentPosition == fetchSongs?.size){
                currentPosition = 0
            }

            currentSongHelper?.isLoop = false
            val nextSong = fetchSongs?.get(currentPosition)
            currentSongHelper?.songTitle = nextSong?.songTitle
            currentSongHelper?.songPath = nextSong?.songData
            currentSongHelper?.currentPosition = currentPosition
            currentSongHelper?.songId = nextSong?.songID as Long

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

            mediaPlayer?.reset()
            try {
                mediaPlayer?.setDataSource(myActivity as Context,Uri.parse(currentSongHelper?.songPath))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                processInformation(mediaPlayer as MediaPlayer)
            }catch(e:Exception){
                e.printStackTrace()
            }
            if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean){
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
            }else{
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))

            }


        }

        fun updateTextViews(songtitle: String, songArtist: String){
            var songTitleUpdated = songtitle
            var songArtistUpdated = songArtist
            if (songtitle.equals("<unknown>", true)){
                songTitleUpdated = "unknown"
            }
            if (songArtistUpdated.equals("<unknown>", true)){
                songArtistUpdated = "unknown"
            }
            songTitleView?.setText(songTitleUpdated)
            songArtistView?.setText(songArtistUpdated)
        }

        fun processInformation(mediaPlayer: MediaPlayer){
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition

            seekbar?.setMax(finalTime)
            seekbar?.setProgress(startTime)

            startTimeText?.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long)-
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long))))

            endTimeText?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )

            seekbar?.setProgress(startTime)
            Handler().postDelayed(updateSongTime,1000)
        }


    }

    var mAcceleration : Float = 0f
    var mAccelerationCurrent : Float = 0f
    var mAccelerationLast : Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view =  inflater!!.inflate(R.layout.fragment_song_playing, container, false)

        setHasOptionsMenu(true)

        activity?.title = " Now Playing"

        seekbar = view?.findViewById(R.id.seekBar)
        startTimeText = view?.findViewById(R.id.startTime)
        endTimeText = view?.findViewById(R.id.endTime)
        playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        nextImageButton = view?.findViewById(R.id.nextButton)
        previousImageButton = view?.findViewById(R.id.previousButton)
        statifiedLoopImageButton = view?.findViewById(R.id.loopButton)
        shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        songArtistView = view?.findViewById(R.id.songArtist)
        songTitleView = view?.findViewById(R.id.songTitle)
        fab = view?.findViewById(R.id.favouriteIcon)
        fab?.alpha = 0.9f

        glView = view.findViewById(R.id.visualizer_view) as GLAudioVisualizationView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as? AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListner, Statified.mSensorManager?.
            getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        audioVisualization?.onPause()
        super.onPause()

        Statified.mSensorManager?.unregisterListener(Statified.mSensorListner)
    }

    override fun onDestroy() {
        audioVisualization?.release()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListner()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem?= menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem?= menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect ->{
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favouriteContent = EchoDatabase(myActivity)
        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isPlaying = true
        currentSongHelper?.isLoop = false
        currentSongHelper?.isShuffle = false

        audioV = glView as AudioVisualization


        var path : String?= null
        var _songTitle : String?= null
        var _songArtist : String?= null
        var songId : Long?= null
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")?.toLong()
            currentPosition = arguments!!.getInt("songPosition")
            fetchSongs = arguments!!.getParcelableArrayList("songData")


            currentSongHelper?.songPath = path
            currentSongHelper?.songTitle = _songTitle
            currentSongHelper?.songArtist = _songArtist
            currentSongHelper?.songId = songId
            currentSongHelper?.currentPosition = currentPosition

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        }catch (e:Exception){
            e.printStackTrace()
        }

        var fromFavBottomBar =  arguments?.get("FavBottomBar") as? String

        if (fromFavBottomBar != null ){
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        }else{
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(path))
                mediaPlayer?.prepare()

            }catch (e:Exception){
                e.printStackTrace()
            }

            mediaPlayer?.start()
        }



        processInformation(mediaPlayer as MediaPlayer)

        if(currentSongHelper?.isPlaying as Boolean){
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        mediaPlayer?.setOnCompletionListener {
            onSongComplete()

        }
        clickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as Context, 0)
        audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature",  false)
        if(isShuffleAllowed as Boolean){
            currentSongHelper?.isShuffle = true
            currentSongHelper?.isLoop = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            statifiedLoopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }else{
            currentSongHelper?.isShuffle = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForloop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForShuffle?.getBoolean("feature",  false)
        if(isLoopAllowed as Boolean){
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop = true
            shuffleImageButton?.setBackgroundResource(R.drawable.loop_icon)
            statifiedLoopImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }else{
            currentSongHelper?.isLoop = false
            statifiedLoopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean){
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
        }else{
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))

        }

    }

    fun clickHandler(){

        fab?.setOnClickListener({
            if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean){
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
                favouriteContent?.deleteFavourite(currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(myActivity,"Removed from favouritea", Toast.LENGTH_SHORT).show()
            }else{
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
                favouriteContent?.storeAsFavourite(currentSongHelper?.songId?.toInt(),currentSongHelper?.songArtist,
                    currentSongHelper?.songTitle, currentSongHelper?.songPath)
                    Toast.makeText(myActivity, "Added to favourites", Toast.LENGTH_SHORT).show()
            }
        })

        shuffleImageButton?.setOnClickListener({
            var editorShuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (currentSongHelper?.isShuffle as Boolean){
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            }else{
                currentSongHelper?.isShuffle = true
                currentSongHelper?.isLoop = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                statifiedLoopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }

        })
        nextImageButton?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if(currentSongHelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
            }else{
                playNext("PlayNextNormal")
            }
        })
        previousImageButton?.setOnClickListener({

            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isLoop as Boolean){
                statifiedLoopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        statifiedLoopImageButton?.setOnClickListener({

            var editorShuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if(currentSongHelper?.isLoop as Boolean){
                currentSongHelper?.isLoop = false
                statifiedLoopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }else{
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                statifiedLoopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        })
        playpauseImageButton?.setOnClickListener({
            if(mediaPlayer?.isPlaying as Boolean){
                mediaPlayer?.pause()
                currentSongHelper?.isPlaying = false
                playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                mediaPlayer?.seekTo(seekbar?.progress as Int)
                mediaPlayer?.start()
                currentSongHelper?.isPlaying = true
                playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })

        seekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBarget: SeekBar?) {
                seekbar?.setProgress(seekbar?.getProgress() as Int)
                mediaPlayer?.seekTo(seekbar?.getProgress() as Int)
            }
        })
    }


    fun playPrevious(){
        currentPosition = currentPosition - 1
        if(currentPosition == -1){
            currentPosition = 0
        }
        if(currentSongHelper?.isPlaying as Boolean){
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)

        }
        currentSongHelper?.isLoop = false
        val nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songPath = nextSong?.songData
        currentSongHelper?.currentPosition = currentPosition
        currentSongHelper?.songId = nextSong?.songID as Long

        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        mediaPlayer?.reset()

        try {
            mediaPlayer?.setDataSource(myActivity as Context,Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            processInformation(mediaPlayer as MediaPlayer)
        }catch(e:Exception){
            e.printStackTrace()
        }
        if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean){
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
        }else{
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))

        }

    }

    fun bindShakeListner(){
        Statified.mSensorListner = object : SensorEventListener{
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelerationLast =mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt((( x*x + y*y + z*z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12){
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean){
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }
        }
    }


}


