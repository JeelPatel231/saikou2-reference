package ani.saikou2.reference


import dev.brahmkshatriya.nicehttp.Requests
import dev.brahmkshatriya.nicehttp.ResponseParser
import dev.brahmkshatriya.nicehttp.addGenericDns
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.OkHttpClient
import java.util.concurrent.*
import kotlin.math.min
import kotlin.reflect.KClass

var adult = false

val defaultHeaders = mapOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36",
)

lateinit var okHttpClient: OkHttpClient
lateinit var client: Requests

enum class AvailableDNS {
    Google,
    CloudFlare,
    AdGuard,
}

class Uri(string: String) {
    var host: String? = null
    var path: String? = null

    init {
        val reg = Regex("^((https?|ftp):/)?/?([^:/\\s]+)((/\\w+)*/)([\\w\\-.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?\$").find(string)?.destructured
        if(reg!=null) {
            val (url, protocol, host, path, file, query, hash) = reg
            this.host = host
            this.path = path
        }
    }
}

fun initializeNetwork(dns: AvailableDNS? = null) {

    okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .apply {
            when (dns) {
                AvailableDNS.Google -> addGoogleDns()
                AvailableDNS.CloudFlare -> addCloudFlareDns()
                AvailableDNS.AdGuard -> addAdGuardDns()
                null -> {}
            }
        }
        .build()
    client = Requests(
        okHttpClient,
        defaultHeaders,
        defaultCacheTime = 6,
        defaultCacheTimeUnit = TimeUnit.HOURS,
        responseParser = Mapper
    )
}

object Mapper : ResponseParser {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @OptIn(InternalSerializationApi::class)
    override fun < T : Any> parse(text: String, kClass: KClass<T>): T {
        return json.decodeFromString(kClass.serializer(), text)
    }

    override fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T? {
        return try {
            parse(text, kClass)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> parse(text: String): T {
        return json.decodeFromString(text)
    }
}

suspend fun <A, B> Collection<A>.asyncMap(lambda: suspend (A) -> B): List<B> = coroutineScope {
    map { async { lambda(it) } }.awaitAll()
}

suspend fun <A, B> Collection<A>.asyncMapNotNull(lambda: suspend (A) -> B?): List<B> = coroutineScope {
    map { async { lambda(it) } }.awaitAll().filterNotNull()
}

//fun logError(e: Exception, post: Boolean = true, snackbar: Boolean = true) {
//    val sw = StringWriter()
//    val pw = PrintWriter(sw)
//    e.printStackTrace(pw)
//    val stackTrace: String = sw.toString()
//    if (post) {
//        if (snackbar)
//            println(stackTrace)
//        else
//            println(e.localizedMessage)
//    }
//    e.printStackTrace()
//}

//fun <T> tryWith(post: Boolean = false, snackbar: Boolean = true, call: () -> T): T? {
//    return try {
//        call.invoke()
//    } catch (e: Exception) {
//        logError(e, post, snackbar)
//        null
//    }
//}

//suspend fun <T> tryWithSuspend(post: Boolean = false, snackbar: Boolean = true, call: suspend () -> T): T? {
//    return try {
//        call.invoke()
//    } catch (e: Exception) {
//        logError(e, post, snackbar)
//        null
//    } catch (e: CancellationException) {
//        null
//    }
//}

/**
 * A url, which can also have headers
 * **/
@kotlinx.serialization.Serializable
data class FileUrl(
    val url: String,
    val headers: Map<String, String> = mapOf()
) {
    companion object {
        operator fun get(url: String, headers: Map<String, String> = mapOf()): FileUrl {
            return FileUrl(url, headers)
        }
    }
}

fun <T> saveData(s: String, data: T){
    println("saving to $s\n$data")
}

fun <T> loadData(s: String): T?{
    println("loading $s")
    return null
}

suspend fun getSize(file: FileUrl): Double? =
    client.head(file.url, file.headers, timeout = 1000).size?.toDouble()?.div(1024 * 1024)

suspend fun getSize(file: String): Double? =
    getSize(FileUrl(file))

//Credits to leg
//data class Lazier<T>(
//    val lClass: KFunction<T>,
//    val name: String
//) {
//    val get = lazy { lClass.call() }
//}

//fun <T> lazyList(vararg objects: Pair<String, KFunction<T>>): List<Lazier<T>> {
//    return objects.map {
//        Lazier(it.second, it.first)
//    }
//}

fun <T> T.printIt(pre:String=""):T{
    println("$pre$this")
    return this
}

fun <T> T.printIt(pre: String = "") : T
where T: Collection<Any> {
    this.forEach{
        println("$pre$it")
    }
    return this
}

fun String.findBetween(a: String, b: String): String? {
    val start = this.indexOf(a)
    val end = if (start != -1) this.indexOf(b, start) else return null
    return if (end != -1) this.subSequence(start, end).removePrefix(a).removeSuffix(b).toString() else null
}

//fun toast(string: String?) {
//    if (string != null) {
//        println(string)
//    }
//}

fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
    if (lhs == rhs) {
        return 0
    }
    if (lhs.isEmpty()) {
        return rhs.length
    }
    if (rhs.isEmpty()) {
        return lhs.length
    }

    val lhsLength = lhs.length + 1
    val rhsLength = rhs.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1 until rhsLength) {
        newCost[0] = i

        for (j in 1 until lhsLength) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength - 1]
}

//fun MutableList<ShowResponse>.sortByTitle(string: String) {
//    val temp: MutableMap<Int, Int> = mutableMapOf()
//    for (i in 0 until this.size) {
//        temp[i] = levenshtein(string.lowercase(), this[i].name.lowercase())
//    }
//    val c = temp.toList().sortedBy { (_, value) -> value }.toMap()
//    val a = ArrayList(c.keys.toList().subList(0, min(this.size, 25)))
//    val b = c.values.toList().subList(0, min(this.size, 25))
//    for (i in b.indices.reversed()) {
//        if (b[i] > 18 && i < a.size) a.removeAt(i)
//    }
//    val temp2 = this.toMutableList()
//    this.clear()
//    for (i in a.indices) {
//        this.add(temp2[a[i]])
//    }
//}

//fun snackString(s: String?, clipboard: String? = null) {
//    if (s != null) {
//        println("$s : $clipboard")
//    }
//}


fun OkHttpClient.Builder.addGoogleDns() = (
        addGenericDns(
            "https://dns.google/dns-query",
            listOf(
                "8.8.4.4",
                "8.8.8.8"
            )
        ))

fun OkHttpClient.Builder.addCloudFlareDns() = (
        addGenericDns(
            "https://cloudflare-dns.com/dns-query",
            listOf(
                "1.1.1.1",
                "1.0.0.1",
                "2606:4700:4700::1111",
                "2606:4700:4700::1001"
            )
        ))

fun OkHttpClient.Builder.addAdGuardDns() = (
        addGenericDns(
            "https://dns.adguard.com/dns-query",
            listOf(
                // "Non-filtering"
                "94.140.14.140",
                "94.140.14.141",
            )
        ))