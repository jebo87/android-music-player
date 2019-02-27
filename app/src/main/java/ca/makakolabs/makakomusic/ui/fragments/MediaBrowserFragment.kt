package ca.makakolabs.makakomusic.ui.fragments

import androidx.fragment.app.Fragment


open class MediaBrowserFragment() : Fragment() {
    companion object {
        val SongsFrag = 0
        val AlbumFrag = 1
    }

    private val TAG = "MediaBrowserFragment"

}




                // Receive callbacks from the MediaController. Here we update our state such as which queue
     // is being shown, the current title and description and the PlaybackState.
//      val mMediaControllerCallback = object : MediaControllerCompat.Callback() {
//         override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
//             super.onMetadataChanged(metadata)
//             if (metadata == null) {
//                 return
//             }
//             Log.d(
//                 TAG, "Received metadata change to media "+metadata.description.mediaId
//
//             )
//             adapter.notifyDataSetChanged()
//         }
//
//         override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
//             super.onPlaybackStateChanged(state)
//             Log.d(TAG, "Received state change: {$state}")
//
//             adapter.notifyDataSetChanged()
//         }
//     }

//     val mSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
//        override fun onChildrenLoaded(
//            parentId: String,
//            children: List<MediaBrowserCompat.MediaItem>
//        ) {
//            try {
//                Log.d(
//                    TAG, "fragment onChildrenLoaded, parentId=" + parentId +
//                            "  count=" + children.size
//                )
////                checkForUserVisibleErrors(children.isEmpty())
//                adapter.clear()
//                for (item in children) {
//                    adapter.add(MediaItemVH(item))
//                }
//                adapter.notifyDataSetChanged()
//            } catch (t: Throwable) {
//                Log.d(TAG, "Error on childrenloaded"+ t)
//            }
//
//        }

//         fun onError(id: String) {
//            Log.d(TAG, "browse fragment subscription onError, id=$id")
//            Toast.makeText(activity, "Error loading media", Toast.LENGTH_LONG).show()
////            checkForUserVisibleErrors(true)
//        }


