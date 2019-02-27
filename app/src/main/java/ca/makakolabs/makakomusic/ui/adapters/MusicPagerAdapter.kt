package ca.makakolabs.makakomusic.ui.adapters

import android.support.v4.media.MediaBrowserCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ca.makakolabs.makakomusic.ui.fragments.AlbumsFragment
import ca.makakolabs.makakomusic.ui.fragments.MediaBrowserFragment
import ca.makakolabs.makakomusic.ui.activities.MainActivity
import ca.makakolabs.makakomusic.ui.fragments.SongsFragment


class MusicPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){

    val sections = arrayOf("Songs","Albums")

    lateinit  var mediaBrowser: MediaBrowserCompat


    private  val ARG_OBJECT = "object"

    override fun getPageTitle(position: Int): CharSequence? {
        return sections[position]
    }


    override fun getCount(): Int {
       return sections.size
    }
    override fun getItem(i: Int): Fragment {

        var fragment = MediaBrowserFragment()


        when (i) {
            MediaBrowserFragment.SongsFrag -> {


                fragment = SongsFragment()
            }
            MediaBrowserFragment.AlbumFrag -> {
                fragment = AlbumsFragment()
            }
        }

        return fragment

    }





}

