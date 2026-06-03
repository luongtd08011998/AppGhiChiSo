import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames

@Serializable
data class ApiStatus(val code: String, val message: String)

@Serializable
data class CustomersApiResponse(
    val data: List<String>? = null,
    val status: ApiStatus
)

fun main() {
    val json = Json { ignoreUnknownKeys = true }
    try {
        json.decodeFromString<CustomersApiResponse>("""[]""")
    } catch (e: Exception) {
        println("Array error: ${e.message}")
    }
    
    try {
        json.decodeFromString<CustomersApiResponse>("""{}""")
    } catch (e: Exception) {
        println("Empty object error: ${e.message}")
    }
}
