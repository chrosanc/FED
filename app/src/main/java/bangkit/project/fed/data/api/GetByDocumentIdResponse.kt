package bangkit.project.fed.data.api

import com.google.gson.annotations.SerializedName

data class GetByDocumentIdResponse(

	@field:SerializedName("egg")
	val egg: Egg? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Egg(

	@field:SerializedName("fertilization")
	val fertilization: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("detectionTimestamp")
	val detectionTimestamp: DetectionTimestamp? = null
)

data class DetectionTimestamp(

	@field:SerializedName("_nanoseconds")
	val nanoseconds: Int? = null,

	@field:SerializedName("_seconds")
	val seconds: Int? = null
)
