package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AuthResponse(
    @SerializedName("token", alternate = ["accessToken", "access_token"])
    val token: String,
    @SerializedName("type")
    val type: String = "Bearer",
    @SerializedName("role")
    val role: String,
    @SerializedName("verified", alternate = ["isVerified", "is_verified", "is_verified_user"])
    val verified: Boolean = false
)

data class User(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName", alternate = ["full_name"])
    val fullName: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("verified", alternate = ["isVerified", "is_verified", "verified_user"])
    val verified: Boolean = false,
    @SerializedName("walletBalance", alternate = ["wallet_balance"])
    val walletBalance: BigDecimal = BigDecimal.ZERO,
    @SerializedName("withdrawableBalance", alternate = ["withdrawable_balance"])
    val withdrawableBalance: BigDecimal = BigDecimal.ZERO,
    @SerializedName("releasedToFarmer", alternate = ["released_to_farmer"])
    val releasedToFarmer: BigDecimal = BigDecimal.ZERO,
    @SerializedName("aadhaarNo", alternate = ["aadhaar_no"])
    val aadhaarNo: String? = null,
    @SerializedName("kycDocumentUrl", alternate = ["kyc_document_url"])
    val kycDocumentUrl: String? = null
)
