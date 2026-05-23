package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AuthResponse(
    @SerializedName("token", alternate = ["accessToken", "access_token"])
    val token: String? = null,
    @SerializedName("type")
    val type: String? = "Bearer",
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("verified", alternate = ["isVerified", "is_verified", "is_verified_user"])
    val verified: Boolean = false,
    @SerializedName("kycStatus", alternate = ["kyc_status"])
    val kycStatus: KycStatus? = KycStatus.PENDING
)

data class User(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("fullName", alternate = ["full_name"])
    val fullName: String? = null,
    @SerializedName("role", alternate = ["user_role", "authority"])
    val role: String? = null,
    @SerializedName("verified", alternate = ["isVerified", "is_verified", "verified_user"])
    val verified: Boolean = false,
    
    @SerializedName("walletBalance", alternate = ["wallet_balance", "current_wallet_balance", "account_balance"])
    val walletBalance: BigDecimal? = BigDecimal.ZERO,
    
    @SerializedName("withdrawableBalance", alternate = ["withdrawable_balance", "withdrawable_funds", "farmer_earnings"])
    val withdrawableBalance: BigDecimal? = BigDecimal.ZERO,
    
    @SerializedName("releasedToFarmer", alternate = ["released_to_farmer", "total_payouts"])
    val releasedToFarmer: BigDecimal? = BigDecimal.ZERO,

    @SerializedName("aadhaarNo", alternate = ["aadhaar_no"])
    val aadhaarNo: String? = null,
    @SerializedName("kycDocumentUrl", alternate = ["kyc_document_url"])
    val kycDocumentUrl: String? = null,
    @SerializedName("kycStatus", alternate = ["kyc_status"])
    val kycStatus: KycStatus? = KycStatus.PENDING,
    @SerializedName("kycRejectionReason", alternate = ["kyc_rejection_reason"])
    val kycRejectionReason: String? = null
) {
    /**
     * Handles role-based KYC logic for legacy users. 
     * If an Investor is in SUBMITTED state (handles legacy database records),
     * we treat them as APPROVED automatically.
     */
    val effectiveKycStatus: KycStatus
        get() {
            // Flexible check for Investor role
            val isInvestor = role?.contains("INVESTOR", ignoreCase = true) == true
            return if (isInvestor && kycStatus == KycStatus.SUBMITTED) {
                KycStatus.APPROVED
            } else {
                kycStatus ?: KycStatus.PENDING
            }
        }
        
    /**
     * Checks if the user is effectively verified.
     */
    val isEffectivelyVerified: Boolean
        get() = verified || effectiveKycStatus == KycStatus.APPROVED
}
