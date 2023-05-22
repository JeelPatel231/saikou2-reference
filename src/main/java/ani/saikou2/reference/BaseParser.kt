package ani.saikou2.reference

import kotlinx.serialization.Serializable
import java.net.URLDecoder
import java.net.URLEncoder

@Serializable
data class Media(
    val id: Int,

    var idMAL: Int? = null,
    var typeMAL: String? = null,

    val name: String?,
    val nameRomaji: String,

    var isAdult: Boolean,
    var format: String? = null,
    var countryOfOrigin: String? = null,
    var genres: ArrayList<String> = arrayListOf(),
    var synonyms: ArrayList<String> = arrayListOf(),

    var vrvId: String? = null,
    var crunchySlug: String? = null,

    var nameMAL: String? = null,
) {

    val mainName = nameMAL ?: name ?: nameRomaji
    val mangaName = if (countryOfOrigin != "JP") mainName else nameRomaji
}

interface BaseParser {

    /**
     * Name that will be shown in Source Selection
     * **/
    val name: String

    /**
     * Name used to save the ShowResponse selected by user or by autoSearch
     * **/
    val saveName: String

    /**
     * The main URL of the Site
     * **/
    val hostUrl: String

    /**
     * override as `true` if the site **only** has NSFW media
     * **/
    val isNSFW
        get() = false

    /**
     * mostly redundant for official app, But override if you want to add different languages
     * **/
    val language
        get() = "English"

    /**
     *  Search for Anime/Manga/Novel, returns a List of Responses
     *
     *  use `encode(query)` to encode the query for making requests
     * **/
    suspend fun search(query: String): List<ShowResponse>

    fun encode(input: String): String = URLEncoder.encode(input, "utf-8").replace("+", "%20")
    fun decode(input: String): String = URLDecoder.decode(input, "utf-8")
}


/**
 * A single show which contains some episodes/chapters which is sent by the site using their search function.
 *
 * You might want to include `otherNames` & `total` too, to further improve user experience.
 *
 * You can also store a Map of Strings if you want to save some extra data.
 * **/
@Serializable
data class ShowResponse(
    val name: String,
    val link: String,
    val coverUrl: FileUrl,

    //would be Useful for custom search, ig
    val otherNames: List<String> = listOf(),

    //Total number of Episodes/Chapters in the show.
    val total: Int? = null,

    //In case you want to send some extra data
    val extra: Map<String, String> = emptyMap(),
) {
    constructor(
        name: String,
        link: String,
        coverUrl: String,
        otherNames: List<String> = emptyList(),
        total: Int? = null,
        extra: Map<String, String> = emptyMap()
    ) : this(
        name, link, FileUrl(coverUrl), otherNames, total, extra
    )
}