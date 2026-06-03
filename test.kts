import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class ApiStatus(val code: String, val message: String)

@Serializable
data class CustomersApiResponse(
    val data: List<String>? = null,
    val status: ApiStatus
)

val json = Json { ignoreUnknownKeys = true }
try {
    json.decodeFromString<CustomersApiResponse>("""[]""")
} catch(e: Exception) {
    println("Array: " + e.message)
}
