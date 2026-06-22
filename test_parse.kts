import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceDto(
    val id: Long,
    val invNumber: String? = null,
    val yearMonth: String? = null,
    val type: Int = 1,
    val custCode: String? = null,
    val custName: String? = null,
    val custAddress: String? = null,
    val totalAmount: Double,
    val oldIndex: Long,
    val newIndex: Long,
    val empPhone: String? = null,
    val numOfPages: Int? = null
)

val jsonStr = """[{"id":821182,"invNumber":"","yearMonth":"202606","type":1,"custCode":"03600494","custName":"LÂM THIÊN ĐẠI","custAddress":"Đường số 3, Phường Phú Mỹ, TP Hồ Chí Minh","totalAmount":209530.0,"oldIndex":631,"newIndex":648,"empPhone":null,"numOfPages":3}]"""

val json = Json { ignoreUnknownKeys = true; isLenient = true }
try {
    val items = json.decodeFromString<List<InvoiceDto>>(jsonStr)
    println("Success: " + items.size + " items")
} catch (e: Exception) {
    println("Error: " + e.message)
}
