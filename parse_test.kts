import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class InvoiceDto(
    val id: Long,
    val invNumber: String? = null,
    val type: Int = 1,
    val custCode: String? = null,
    val custName: String? = null,
    val custAddress: String? = null,
    val totalAmount: Double,
    val oldIndex: Long,
    val newIndex: Long,
    val empPhone: String? = null
)

val json = Json { ignoreUnknownKeys = true; isLenient = true }
val file = File("/Users/tranducluong/.gemini/antigravity-ide/brain/ccccc740-5550-4a5c-966a-a505529303b4/.system_generated/tasks/task-530.log")
val text = file.readText().lines().last() // The JSON is on the last line

try {
    val list = json.decodeFromString<List<InvoiceDto>>(text)
    println("SUCCESS: Parsed ${list.size} items!")
    val item = list.find { it.custCode == "00900104" }
    println("Found 00900104: ${item != null}")
} catch (e: Exception) {
    println("FAILED: ${e.message}")
}
