package twilio.flutter.twilio_programmable_video

import android.content.Context
import com.twilio.video.LocalVideoTrack
import com.twilio.video.VideoTrack
import com.twilio.video.VideoView
import io.flutter.plugin.common.MessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class ParticipantViewFactory(createArgsCodec: MessageCodec<Any>, private val plugin: PluginHandler) : PlatformViewFactory(createArgsCodec) {
    private val TAG = "RoomListener"

    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        var videoTrack: VideoTrack? = null
        val params = args as? Map<*, *> ?: throw IllegalStateException("args cannot be null")
        debug("create => params: ${params}")

        if (params["isLocal"] == true) {
            debug("create => constructing local view with params: '${params.values.joinToString(", ")}'")
            val localVideoTrackName = params["name"] as? String ?: ""
            debug("[isLocal == true][localVideoTrackName]: $localVideoTrackName")
            var localVideoTrack =  TwilioProgrammableVideoPlugin.getLocalVideoTrack(localVideoTrackName)
            var localVideoTracks = TwilioProgrammableVideoPlugin.localVideoTracks
            debug("[localVideoTrack]: $localVideoTrack")
            debug("[localVideoTracks]: $localVideoTracks")
            debug("[TwilioProgrammableVideoPlugin]: ${TwilioProgrammableVideoPlugin.Companion.localVideoTracks}")
            debug("[TwilioProgrammableVideoPlugin.cameraCapturer]: ${TwilioProgrammableVideoPlugin.cameraCapturer}")
            if (localVideoTrackName != "") {
                videoTrack = if(TwilioProgrammableVideoPlugin.localVideoTracks[localVideoTrackName] != null) TwilioProgrammableVideoPlugin.localVideoTracks[localVideoTrackName] else   LocalVideoTrack.create(
                    TwilioProgrammableVideoPlugin.applicationContext,
                    true,
                    TwilioProgrammableVideoPlugin.cameraCapturer!!,
                    localVideoTrackName,
                )
                debug("[videoTrack]: $videoTrack")
            } else {
                val localParticipant = plugin.getLocalParticipant()
                debug("[localParticipant]: $localParticipant")
                if (localParticipant?.localVideoTracks?.isNotEmpty() == true) {
                    videoTrack = localParticipant.localVideoTracks.firstOrNull()?.localVideoTrack
                    debug("[videoTrack]: $videoTrack")
                }
            }
        } else {
            debug("create => constructing view with params: '${params.values.joinToString(", ")}'")
            if ("remoteParticipantSid" in params && "remoteVideoTrackSid" in params) {
                val remoteParticipant = plugin.getRemoteParticipant(params["remoteParticipantSid"] as String)
                val remoteVideoTrack = remoteParticipant?.remoteVideoTracks?.find { it.trackSid == params["remoteVideoTrackSid"] }
                if (remoteParticipant != null && remoteVideoTrack != null) {
                    videoTrack = remoteVideoTrack.remoteVideoTrack
                }
            }
        }

        if (videoTrack == null) {
            throw IllegalStateException("Could not create VideoTrack")
        }
        val videoView = VideoView(context as Context)
        videoView.mirror = params["mirror"] as Boolean
        return ParticipantView(videoView, videoTrack)
    }

    internal fun debug(msg: String) {
        TwilioProgrammableVideoPlugin.debug("$TAG::$msg")
    }
}
