package ani.saikou2.reference


import kotlinx.serialization.Serializable

/**
 * Used to extract videos from a specific video host,
 *
 * A new instance is created for every embeds/iframes of that Episode
 * **/


lateinit var ExtractorMap: Map<String, VideoExtractor>

interface VideoExtractor {
    suspend fun extract(server: VideoServer): VideoContainer
//    open suspend fun onVideoPlayed(server: VideoServer, video: Video?) {}
//    open suspend fun onVideoStopped(server:VideoServer, video: Video?) {}
}

/**
 * A simple class containing name, link & extraData(in case you want to give some to it) of the embed which shows the video present on the site
 *
 * `name` variable is used when checking if there was a Default Server Selected with the same name
 *
 *
 * **/
@Serializable
data class VideoServer(
    val name: String,
    val embed: FileUrl,
    val extraData: Map<String, String> = emptyMap(),
) {
    constructor(name: String, embedUrl: String, extraData: Map<String, String> = emptyMap())
            : this(name, FileUrl(embedUrl), extraData)
}

/**
 * A Container for keeping video & subtitles, so you dont need to check backend
 * **/
data class VideoContainer(
    val videos: List<Video>,
    val subtitles: List<Subtitle> = listOf()
)

/**
 * The Class which contains all the information about a Video
 * **/
@Serializable
data class Video(
    /**
     * Will represent quality to user in form of `"${quality}p"` (1080p)
     *
     * If quality is null, shows "Unknown Quality"
     *
     * If isM3U8 is true, shows "Multi Quality"
     * **/
    val quality: Int?,

    /**
     * Mime type / Format of the video,
     *
     * If not a "CONTAINER" format, the app show video as a "Multi Quality" Link
     * "CONTAINER" formats are Mp4 & Mkv
     * **/
    val format: VideoType,

    /**
     * The direct url to the Video
     *
     * Supports mp4, mkv, dash & m3u8, afaik
     * **/
    val url: FileUrl,

    /**
     * use getSize(url) to get this size,
     *
     * no need to set it on M3U8 links
     * **/
    val size: Double? = null,

    /**
     * In case, you want to show some extra notes to the User
     *
     * Ex: "Backup" which could be used if the site provides some
     * **/
    val extraNote: String? = null,
) {
    constructor(
        quality: Int?,
        format: VideoType,
        url: String,
        size: Double? = null,
        extraNote: String? = null,
    ) : this(quality, format, FileUrl(url), size, extraNote)
}

/**
 * The Class which contains the link to a subtitle file of a specific language
 * **/
@Serializable
data class Subtitle(
    /**
     * Language of the Subtitle
     *
     * for now app will directly try to select "English".
     * Probably in rework we can add more subtitles support
     * **/
    val language: String,

    /**
     * The direct url to the Subtitle
     * **/
    val url: FileUrl,

    /**
     * format of the Subtitle
     *
     * Supports VTT, SRT & ASS
     * **/
    val type: SubtitleType = SubtitleType.VTT,
) {
    constructor(language: String, url: String, type: SubtitleType = SubtitleType.VTT) : this(
        language,
        FileUrl(url),
        type
    )
}

enum class VideoType {
    CONTAINER,
    M3U8,
    DASH
}

enum class SubtitleType {
    VTT,
    ASS,
    SRT
}