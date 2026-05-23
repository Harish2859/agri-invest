package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName

data class KycRequest(
    val documentUrl: String
)

data class KycResponse(
    val message: String,
    @SerializedName("kycStatus", alternate = ["kyc_status", "status"])
    val kycStatus: KycStatus? = null,
    @SerializedName("verified", alternate = ["is_verified", "isVerified"])
    val verified: Boolean? = null
)
