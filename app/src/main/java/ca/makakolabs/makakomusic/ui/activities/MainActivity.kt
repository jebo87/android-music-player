package ca.makakolabs.makakomusic.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.*

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import ca.makakolabs.makakomusic.R
import ca.makakolabs.makakomusic.ui.adapters.MusicPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import ca.makakolabs.makakomusic.ui.helpers.ZoomOutTransformation
import ca.makakolabs.makakomusic.data.model.Song
import ca.makakolabs.makakomusic.services.MakakoPlaybackService
import ca.makakolabs.makakomusic.ui.fragments.AlbumsFragment
import ca.makakolabs.makakomusic.ui.fragments.MediaBrowserFragment
import ca.makakolabs.makakomusic.ui.fragments.SongsFragment
import ca.makakolabs.makakomusic.ui.viewholders.SongItem
import com.google.android.material.tabs.TabLayout
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MediaBrowserProvider {

    val fragmentManager = supportFragmentManager

    private lateinit var musicPageAdapter: MusicPagerAdapter
    private lateinit var viewPager: ViewPager
    lateinit var mediaBrowser: MediaBrowserCompat



    companion object {
        val TAG = "MainActivity"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()



        nav_view.setNavigationItemSelectedListener(this)

        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            // No explanation needed; request the permission
            if (Build.VERSION.SDK_INT > 23) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
            }else{

                TODO("Do this for SDK < 23")
            }


            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {


            loadFragments()
        }


    }
    private fun loadFragments(){


        //viewpager
        musicPageAdapter = MusicPagerAdapter(supportFragmentManager)
        viewPager = content_main_view_pager

        viewPager.adapter = musicPageAdapter

        val zoomOutTransformation = ZoomOutTransformation()
        viewPager.setPageTransformer(true, zoomOutTransformation)

        val tabLayout = this.findViewById<TabLayout>(R.id.content_main_tab_layout)


        tabLayout.setupWithViewPager(viewPager)

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MakakoPlaybackService::class.java),
            myCallback,
            null // optional Bundle
        )



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
        mediaBrowser.connect()
        Log.d(TAG, "connecting....")

    }

    public override fun onStop() {
        super.onStop()
        // (see "stay in sync with the MediaSession")
        //MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    override fun getMediaBrowserCompat():MediaBrowserCompat {
       return mediaBrowser
    }

    private val myCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnectionFailed() {
            Log.d(TAG, "Connection Aborted!")
        }
        override fun onConnected() {

            Log.d(TAG, "Connected to MediaBrowser")
            Log.d(TAG, "Loading songs...")
            var songsFragment = (fragmentManager.fragments[0] as SongsFragment)

            songsFragment.onConnected()


            Log.d(TAG, "Loading albums...")
            var albumsFragment = (fragmentManager.fragments[1] as AlbumsFragment)

            albumsFragment.onConnected()





            var root = MakakoPlaybackService.SONGS_MEDIA_ROOT_ID
//            mediaBrowser.subscribe(root,
//                object : MediaBrowserCompat.SubscriptionCallback() {
//                    override fun onChildrenLoaded(
//                        parentId: String,
//                        songs: List<MediaBrowserCompat.MediaItem>
//                    ) {
//                        if (songs == null || songs.isEmpty()) {
//                            return
//                        }
//
//                        var other = songs as MutableList<Song>
//
//
//
//
//
//                    }
//                })

            // Get the token for the MediaSession
            mediaBrowser!!.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@MainActivity, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)

            }
        }


    }






}
