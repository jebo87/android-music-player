package ca.makakolabs.makakomusic.ui.activities

import android.support.v4.media.MediaBrowserCompat

interface MediaBrowserProvider{
    fun getMediaBrowserCompat(): MediaBrowserCompat
}