package ca.makakolabs.makakomusic.utils


class Utils{

    companion object {
        fun convertToTime(milliseconds: Long): String {



            var segs = "" + (milliseconds / 1000).toInt() % 60
            var mins = "" + (milliseconds / (1000 * 60) % 60).toInt()
            var hours = "" + (milliseconds / (1000 * 60 * 60) % 24).toInt()
            if (segs.length == 1) {
                segs = "0$segs"
            }
            if (mins.length == 1) {
                mins = "0$mins"
            }
            if (hours.length == 1) {
                hours = "0$hours"
            }
            return if ((milliseconds / (1000 * 60 * 60) % 24).toInt() == 0) {
                mins + ":" + segs
            } else hours + ":" + mins + ":" + segs
        }
    }

}
