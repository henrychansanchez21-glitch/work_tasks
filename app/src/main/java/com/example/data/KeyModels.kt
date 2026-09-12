package com.example.data

data class KeyValidationResult(
    val isValid: Boolean,
    val key: String = "",
    val tier: String = "Standard",
    val message: String = "",
    val source: String = "Desconocido",
    val expiresAt: String = "Ilimitado",
    val packageBound: String = "com.aistudio.keysentinel.zkpq"
)

data class ActiveKeyInfo(
    val key: String,
    val tier: String,
    val source: String,
    val activatedAt: Long,
    val expiresAt: String = "Ilimitado"
)
