package bangkit.project.fed.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @Multipart
    @POST("add-egg-detected/{userId}")
    suspend fun uploadImage(
        @Path("userId") userId: String,
        @Part images: MultipartBody.Part,
        @Part("label") label: RequestBody,
        @Part("detectionTimestamp") detectionTimestamp: RequestBody,
    ): UploadResponse

    @GET("get-egg-by-id")
    suspend fun getImageByDocumentId(
        @Part("fertilization") fertilization: String,
        @Part("id") id: String,
        @Part("userId") userId: String,
    ) : GetByDocumentIdResponse
}