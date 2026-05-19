package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Milestone(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("title")
    val title: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("status")
    val status: String, // PENDING, SUBMITTED, COMPLETED
    
    @SerializedName("project_id")
    val projectId: Long?,

    @SerializedName("amount_to_release", alternate = ["amount", "amountToRelease"])
    val amountToRelease: BigDecimal? = BigDecimal.ZERO,
    
    @SerializedName("release_percentage", alternate = ["releasePercentage"])
    val releasePercentage: Double,
    
    @SerializedName("proof_image_url", alternate = ["proofImageUrl", "proof_url"])
    val proofImageUrl: String? = null,

    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    @SerializedName("funds_released")
    val fundsReleased: Boolean = false,

    @SerializedName("submitted_at")
    val submittedAt: String? = null,

    @SerializedName("verified_at")
    val verifiedAt: String? = null
)

// Wrapper for the file upload response
data class UploadResponse(
    @SerializedName("url")
    val url: String
)

// Request wrapper for final submission
data class ProofRequest(
    @SerializedName("proof_image_url", alternate = ["proofUrl", "proofImageUrl"])
    val proofImageUrl: String
)
