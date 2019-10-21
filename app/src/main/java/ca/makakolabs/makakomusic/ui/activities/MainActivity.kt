package ca.makakolabs.makakomusic.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.ui.adapters.MusicPagerAdapter
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import ca.makakolabs.makakomusic.ui.helpers.ZoomOutTransformation
import ca.makakolabs.makakomusic.MakakoPlaybackService
import ca.makakolabs.makakomusic.ui.fragments.AlbumsFragment
import ca.makakolabs.makakomusic.ui.fragments.SongsFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MediaActionListener {


    val fragmentManager = supportFragmentManager

    private lateinit var musicPageAdapter: MusicPagerAdapter
    private lateinit var viewPager: ViewPager
    lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var currentSong: Song
    lateinit var songs: MutableList<Song>
    private lateinit var vectordr:AnimatedVectorDrawableCompat
    private var interpolator: OvershootInterpolator = OvershootInterpolator()
    private var density = 1f
    private var mHandler = Handler()




    companion object {
        val TAG = "MainActivity"
        var NOW_PLAYING_OPEN=false

    }

    init{

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hide the now playing bar by default
        now_playing.visibility=FrameLayout.INVISIBLE


        setSupportActionBar(toolbar)


        //navigation drawer default stuff
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        //set density to handle image sizes according to display density on different devices
        var metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        density = metrics.density



        checkPermissions()
        setUpNowPlayingEvents()

    }

    private fun setUpNowPlayingEvents(){


        //handle the toggle of the now playing button
        main_fab.setOnClickListener {
            if(!NOW_PLAYING_OPEN)
                openNowPlaying()
            else
                closeNowPlaying()
        }


        //If the user touches the card where the song info is, it will open the playback activity
        now_playing_card.setOnClickListener{
            //make sure to close the now playing bar before opening the playback activity
            closeNowPlaying()

            //start the activity
            var playbackIntent = Intent(this, PlaybackActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(playbackIntent)
        }
        now_playing_next.setOnClickListener{
            //skip to next song by accessing the transport controls offered by the media controller
            mediaController.transportControls.skipToNext()
        }
        now_playing_previous.setOnClickListener{
            //skip to previous song by accessing the transport controls offered by the media controller
            mediaController.transportControls.skipToPrevious()
        }

        now_playing_pause.setOnClickListener{
            //Depending on the state we want to pause or resume playback
            //make sure to update the button image from "pause" to "play" accordingly
            when(mediaController.playbackState.state){
                PlaybackStateCompat.STATE_PLAYING -> {
                    Log.d(TAG,"Pausing from now playing")
                    mediaController.transportControls.pause()
                    //now_playing_pause.setImageURI()
                }

                PlaybackStateCompat.STATE_PAUSED ->{
                    Log.d(TAG,"Resuming playback from now playing")
                    mediaController.transportControls.play()
                }


            }



        }
    }

    private fun setUpNowPlaying(){

        //defatul configuration for the now playing bar
        //basically hide everything

        main_fab.scaleX=1f
        main_fab.scaleY=1f
        now_playing_card.alpha=0f
        now_playing_card.translationX=400f*density

        now_playing_previous.translationY = 40f*density
        now_playing_previous.translationX = 40f*density
        now_playing_previous.alpha = 0f

        now_playing_next.translationY = 40f*density
        now_playing_next.translationX = -5f*density
        now_playing_next.alpha=0f

        now_playing_pause.translationY = 40f*density
        now_playing_pause.translationX = 10f*density
        now_playing_pause.alpha=0f


        now_playing.visibility=FrameLayout.VISIBLE
        now_playing_pause.isEnabled = false
        now_playing_previous.isEnabled = false
        now_playing_next.isEnabled = false


    }

    private fun openNowPlaying(){
        //disable the main button to avoid the user touching the button rapidly
        //once all animations are finished we will enable the button again.
        main_fab.isEnabled = false
        if(!NOW_PLAYING_OPEN) {
            //enable the controls
            now_playing_pause.isEnabled = true
            now_playing_previous.isEnabled = true
            now_playing_next.isEnabled = true

            //bring everything to the screen with fancy animations
            main_fab.animate().scaleX(1.6f).scaleY(1.6f).rotationBy(-360f).setInterpolator(interpolator)
                .setDuration(300)
            now_playing_card.animate().translationX(0f).alpha(1f).setDuration(200)
            now_playing_previous.animate().translationX(0f).translationY(0f).alpha(1f).setDuration(200)
            now_playing_next.animate().translationX(0f).translationY(0f).alpha(1f).setDuration(200)
            now_playing_pause.animate().translationX(0f).translationY(0f).alpha(1f).setDuration(200).withEndAction {
                //once everything is ready, we consider the now playing bar as operative.
                NOW_PLAYING_OPEN = true
                //reanable the main button
                main_fab.isEnabled = true
            }



        }

    }

    private fun closeNowPlaying(){
        //disable the main button to avoid the user touching the button rapidly
        //once all animations are finished we will enable the button again.
        main_fab.isEnabled = false

        if(NOW_PLAYING_OPEN) {
            //disable all controls
            now_playing_pause.isEnabled = false
            now_playing_previous.isEnabled = false
            now_playing_next.isEnabled = false


            //remove the additional elements from the bar with fancy animations

            main_fab.animate().scaleX(1f).scaleY(1f).rotationBy(360f).setInterpolator(interpolator).setDuration(300)
            now_playing_card.animate().translationX(400f * density).alpha(0f).setDuration(200)
            now_playing_previous.animate().translationX(40f * density).translationY(40f * density).alpha(0f)
                .setDuration(200)
            now_playing_next.animate().translationX(-5f * density).translationY(40f * density).alpha(0f)
                .setDuration(200)
            now_playing_pause.animate().translationX(10f * density).translationY(40f * density).alpha(0f)
                .setDuration(200).withEndAction {
                    //once everything is ready we update the state of the noe playing bar
                    NOW_PLAYING_OPEN = false
                    //enable the button again
                    main_fab.isEnabled = true

                }




        }


    }

    //Async load the small image for the nowplaying button
    private fun loadSmallImage(metadata:MediaMetadataCompat?){
        mHandler = Handler()
        Thread(Runnable {
            mHandler.post {
                var options = RequestOptions()
                //we don't need much resolution, 100 by 100 should be enough
                options.override(100,100)
                var myBitmap: Bitmap
                try {
                    //Try to get the image from the music file, if not available
                    //we will load a default one when the exception appears
                    myBitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        Uri.parse(metadata?.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
                    )
                    Log.d(TAG, metadata?.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))

                    //we use glide to load the image into the button.
                    Glide.with(applicationContext)
                        .load(myBitmap)
                        .placeholder(R.drawable.ic_empty_album)
                        .circleCrop().override(100 ,100)
                        .into(main_fab)
                } catch (e: FileNotFoundException) {
                    //if no image is available, we load a default artwork
                        Glide.with(applicationContext)
                    .load(R.drawable.ic_empty_album)
                    .centerCrop()
                    .circleCrop().override(100,100)
                    .into(main_fab)
                }


            }
        }).start()
    }
    //this function updates the nowplaying section on the bottom
    private fun updateNowPlaying(metadata: MediaMetadataCompat?){
            now_playing_title.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            now_playing_artist.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

            loadSmallImage(metadata)

    }

    private fun checkPermissions(){
        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            // No explanation needed; request the permission
            if (Build.VERSION.SDK_INT > 23) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
            } else {

                TODO("Do this for SDK < 23")
            }


            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {


            loadFragments()
        }

    }

    private fun loadFragments() {

        //create a MediaBrowser client to connect to our MediaBrowser service
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MakakoPlaybackService::class.java),
            mediaBrowserConnectionCallback,
            null // optional Bundle
        )


        //viewpager
        musicPageAdapter = MusicPagerAdapter(supportFragmentManager)
        viewPager = content_main_view_pager
        viewPager.adapter = musicPageAdapter

        //load the custom transformation when the user swipes between the different views
        val zoomOutTransformation = ZoomOutTransformation()
        viewPager.setPageTransformer(true, zoomOutTransformation)

        val tabLayout = this.findViewById<TabLayout>(R.id.content_main_tab_layout)
        tabLayout.setupWithViewPager(viewPager)


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            2 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    loadFragments()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request.
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.songs_menu_item -> {
//                val fragment = SongsFragment()
//                val fragmentTransaction = fragmentManager.beginTransaction()
//                fragmentTransaction.replace(R.id.content_main,fragment)
//                fragmentTransaction.commit()


            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStart() {
        super.onStart()
        if (!mediaBrowser.isConnected) {
            Log.v(TAG, "mediaBrowser not connected")

            mediaBrowser.connect()
        }


        Log.d(TAG, "connecting....")

    }



    public override fun onStop() {
        super.onStop()
        // (see "stay in sync with the MediaSession")
        //MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    override fun getMediaBrowserCompat(): MediaBrowserCompat {
        return mediaBrowser
    }

    override fun getMediaControllerCompat(): MediaControllerCompat {
        return MediaControllerCompat.getMediaController(this)
    }

    override fun onMediaItemSelected(song: Song) {
        var controller = MediaControllerCompat.getMediaController(this@MainActivity)
        currentSong = song


        //prepare everything for the now playing bar
        setUpNowPlaying()
        if(NOW_PLAYING_OPEN)
            closeNowPlaying()



        controller.transportControls.stop()

        for (songTemp in songs) {
            controller.addQueueItem(songTemp.toMetaData().description)

        }

        controller.transportControls.playFromMediaId(song.id, Bundle().apply {
                putParcelable("com.makakolabs.makakomusic.song", song)
            })


        var playbackIntent = Intent(this, PlaybackActivity::class.java)
        playbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(playbackIntent)

    }

    override fun setMediaList(newSongs: MutableList<Song>) {
        songs = newSongs
    }

    fun onPlaybackStart() {

    }



    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            updateNowPlaying(metadata)
        }
    }

    private val mediaBrowserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnectionFailed() {
            Log.d(TAG, "Connection Aborted!")
        }

        override fun onConnected() {



            Log.d(TAG, "Connected to MediaBrowser")
            Log.d(TAG, "Loading songs...")
            //fragment in position 0 is our songs fragment
            var songsFragment = (fragmentManager.fragments[0] as SongsFragment)

            songsFragment.onConnected()


            Log.d(TAG, "Loading albums...")
            var albumsFragment = (fragmentManager.fragments[1] as AlbumsFragment)

            albumsFragment.onConnected()

            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@MainActivity, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)

                var pbState:PlaybackStateCompat? = mediaController.playbackState


                //if there is something playing or paused, we still need to show the information in our now playing bar
                if (pbState?.state == PlaybackStateCompat.STATE_PLAYING ||pbState?.state == PlaybackStateCompat.STATE_PAUSED )
                {
                    setUpNowPlaying()
                    updateNowPlaying(mediaController.metadata)
                }

                // Register a Callback to stay in sync
                mediaController.registerCallback(controllerCallback)

            }
        }


    }


}
