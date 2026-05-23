package com.example.agri_invest_app.data.model

import com.google.gson.annotations.SerializedName

enum class KycStatus {
    @SerializedName("PENDING", alternate = ["pending", "PENDING_VERIFICATION"])
    PENDING,
    
    @SerializedName("SUBMITTED", alternate = ["submitted", "UNDER_REVIEW"])
    SUBMITTED,
    
    @SerializedName("APPROVED", alternate = ["approved", "VERIFIED"])
    APPROVED,
    
    @SerializedName("REJECTED", alternate = ["rejected", "DECLINED"])
    REJECTED
}
