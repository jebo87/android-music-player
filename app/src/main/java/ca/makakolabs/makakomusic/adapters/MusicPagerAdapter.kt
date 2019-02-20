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



class MusicPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){

    val sections = arrayOf("Songs","Albums")
    private  val ARG_OBJECT = "object"


    override fun getCount(): Int {
       return sections.size
    }
    override fun getItem(i: Int): Fragment {

        when (i) {
            0 -> return AlbumsFragment()
            else -> return SongsFragment()
        }

    }



}

