package edu.nd.pmcburne.hello

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
// ---- Retrofit Interface ----
interface LocationApiService {
    @GET("~wxt4gm/placemarks.json")
    suspend fun getLocations(): List<ApiLocation>  // ✅ direct list, no wrapper
}

// ---- Retrofit Singleton ----
object RetrofitInstance {
    val api: LocationApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.cs.virginia.edu/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationApiService::class.java)
    }
}

// ---- JSON Response Models ----
data class ApiLocation(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("tag_list") val tagList: List<String>?,
    @SerializedName("visual_center") val visualCenter: VisualCenter?
)

data class VisualCenter(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

// ---- Parse into Room entities ----
fun parseLocations(response: List<ApiLocation>): List<Location> {
    return response.mapNotNull { api ->
        val lat = api.visualCenter?.latitude ?: return@mapNotNull null
        val lng = api.visualCenter?.longitude ?: return@mapNotNull null

        Location(
            id          = api.id ?: return@mapNotNull null,
            name        = api.name ?: "Unknown",
            description = api.description ?: "",
            latitude    = lat,
            longitude   = lng,
            tags        = api.tagList?.joinToString(",") ?: ""
        )
    }
}