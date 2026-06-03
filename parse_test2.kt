import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlin.test.assertEquals

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

fun main() {
    val jsonString = """[{"id":812246,"invNumber":"00028782","yearMonth":"202605","type":1,"custCode":"00900104","custName":"NGUYỄN VIẾT VIỆT","custAddress":"Khu TĐC 25 HA, Phường Phú Mỹ, TP Hồ Chí Minh","totalAmount":122590.0,"oldIndex":304,"newIndex":315,"empPhone":"0359423852"}]"""
    val json = Json { ignoreUnknownKeys = true; isLenient = true }
    val result = json.decodeFromString<List<InvoiceDto>>(jsonString)
    println(result)
}
