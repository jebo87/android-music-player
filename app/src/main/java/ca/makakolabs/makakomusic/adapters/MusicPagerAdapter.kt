package ca.makakolabs.makakomusic.adapters

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ca.makakolabs.makakomusic.fragments.AlbumsFragment
import ca.makakolabs.makakomusic.fragments.SongsFragment
import androidx.viewpager.widget.ViewPager
import ca.makakolabs.makakomusic.fragments.MediaBrowserFragment


class MusicPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){

    val sections = arrayOf("Songs","Albums")

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

