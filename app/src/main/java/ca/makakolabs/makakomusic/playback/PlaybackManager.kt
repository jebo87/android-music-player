package ca.makakolabs.makakomusic.playback

class PlaybackManager : Playback.Callback {
    override fun onCompletion() {
// The current song finished playing

  }

    override fun onPlaybackStatusChanged(state: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(error: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setCurrentMediaId(mediaId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}