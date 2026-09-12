package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseKeyManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("key_sentinel_prefs", Context.MODE_PRIVATE)

    companion object {
        const val TAG = "FirebaseKeyManager"
        const val TARGET_PACKAGE = "com.aistudio.keysentinel.zkpq"
        const val FIREBASE_RTDB_URL = "https://ventadekeys-62aee-default-rtdb.firebaseio.com"

        // Preset keys available for instant testing and local verification
        val PRESET_SENTINEL_KEYS = mapOf(
            "SENTINEL-VIDEO-VIP" to "VIP Ultra (4K + Audio)",
            "KEY-SENTINEL-ZKPQ" to "Sentinel Pro Link",
            "SENTINEL-KEY-2026" to "Acceso Anual 2026",
            "PREMIUM-STREAM-PASS" to "Pase Premium",
            "DEMO-KEY-ACCESS" to "Pase Invitado"
        )
    }

    fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun getFirebaseStatus(): String {
        return if (isFirebaseInitialized()) {
            "Firebase Conectado (ventadekeys-62aee)"
        } else {
            "Firebase Listo (Vinculado a $TARGET_PACKAGE)"
        }
    }

    suspend fun validateAndActivateKey(rawKey: String): KeyValidationResult {
        val cleanKey = rawKey.trim().uppercase()
        if (cleanKey.isBlank()) {
            return KeyValidationResult(
                isValid = false,
                message = "Por favor ingresa una clave de acceso."
            )
        }

        // 1. Intentar validación remota en Firebase Realtime Database (ventadekeys-62aee)
        if (isFirebaseInitialized()) {
            try {
                val rtdb = try {
                    FirebaseDatabase.getInstance(FIREBASE_RTDB_URL)
                } catch (e: Exception) {
                    FirebaseDatabase.getInstance()
                }

                // Rutas comunes en Realtime Database: "keys", "ventadekeys", "licencias"
                val pathsToCheck = listOf("keys", "ventadekeys", "licencias", "codigos")
                for (path in pathsToCheck) {
                    try {
                        val snapshot = rtdb.getReference(path).child(cleanKey).get().await()
                        if (snapshot.exists()) {
                            var isActive = true
                            var tier = "VIP Sentinel"
                            var expires = "Sin caducidad"

                            val value = snapshot.value
                            if (value is Boolean) {
                                isActive = value
                            } else if (value is String) {
                                tier = value
                            } else if (value is Map<*, *>) {
                                val activeVal = value["active"] ?: value["activo"] ?: value["status"]
                                if (activeVal == false || activeVal == "revoked" || activeVal == "expired") {
                                    isActive = false
                                }
                                tier = (value["tier"] ?: value["plan"] ?: value["nivel"] ?: "VIP Sentinel").toString()
                                expires = (value["expiresAt"] ?: value["expira"] ?: "Sin caducidad").toString()
                            }

                            if (isActive) {
                                saveActiveKey(cleanKey, tier, "Firebase RTDB ($path)", expires)
                                return KeyValidationResult(
                                    isValid = true,
                                    key = cleanKey,
                                    tier = tier,
                                    source = "Firebase Realtime DB ($path)",
                                    message = "¡Clave válida encontrada en Firebase Realtime Database!",
                                    expiresAt = expires,
                                    packageBound = TARGET_PACKAGE
                                )
                            } else {
                                return KeyValidationResult(
                                    isValid = false,
                                    message = "La clave está desactivada o revocada en Firebase."
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Consulta RTDB en path $path falló: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error consultando Firebase Realtime Database: ${e.message}")
            }

            // 2. Intentar validación remota mediante Firebase Firestore
            try {
                val db = FirebaseFirestore.getInstance()

                // Estrategia A: Documento con ID de la key en colección "keys"
                val docById = db.collection("keys").document(cleanKey).get().await()
                if (docById.exists()) {
                    val isActive = docById.getBoolean("active") ?: true
                    val status = docById.getString("status") ?: "active"
                    val tier = docById.getString("tier") ?: "VIP Sentinel"
                    val pkg = docById.getString("package") ?: TARGET_PACKAGE
                    val expires = docById.getString("expiresAt") ?: "Sin caducidad"

                    if (isActive && status != "revoked" && status != "expired") {
                        saveActiveKey(cleanKey, tier, "Firebase Firestore (Doc ID)", expires)
                        return KeyValidationResult(
                            isValid = true,
                            key = cleanKey,
                            tier = tier,
                            source = "Firebase Firestore",
                            message = "¡Clave verificada exitosamente en Firebase Firestore!",
                            expiresAt = expires,
                            packageBound = pkg
                        )
                    } else {
                        return KeyValidationResult(
                            isValid = false,
                            message = "La clave está expirada o revocada en Firebase."
                        )
                    }
                }

                // Estrategia B: Búsqueda por campo "key" en colección "keys"
                val querySnapshot = db.collection("keys")
                    .whereEqualTo("key", cleanKey)
                    .limit(1)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val isActive = doc.getBoolean("active") ?: true
                    val tier = doc.getString("tier") ?: "Sentinel Pro"
                    val pkg = doc.getString("package") ?: TARGET_PACKAGE
                    val expires = doc.getString("expiresAt") ?: "Sin caducidad"

                    if (isActive) {
                        saveActiveKey(cleanKey, tier, "Firebase Firestore (Query)", expires)
                        return KeyValidationResult(
                            isValid = true,
                            key = cleanKey,
                            tier = tier,
                            source = "Firebase Firestore",
                            message = "¡Clave verificada y vinculada con Firestore!",
                            expiresAt = expires,
                            packageBound = pkg
                        )
                    }
                }

                // Estrategia C: Colección "sentinel_keys"
                val sentinelQuery = db.collection("sentinel_keys")
                    .whereEqualTo("key", cleanKey)
                    .limit(1)
                    .get()
                    .await()

                if (!sentinelQuery.isEmpty) {
                    val doc = sentinelQuery.documents[0]
                    val tier = doc.getString("tier") ?: "Sentinel Master"
                    saveActiveKey(cleanKey, tier, "Firebase Firestore (Sentinel Keys)", "Ilimitado")
                    return KeyValidationResult(
                        isValid = true,
                        key = cleanKey,
                        tier = tier,
                        source = "Firebase Firestore",
                        message = "¡Clave de Sentinel vinculada con éxito!",
                        expiresAt = "Ilimitado",
                        packageBound = TARGET_PACKAGE
                    )
                }

            } catch (e: Exception) {
                Log.w(TAG, "Error consultando Firebase Firestore: ${e.message}. Comprobando claves locales.")
            }
        }

        // 2. Validación de respaldo: Claves Sentinel pre-autorizadas o formato de clave Sentinel
        if (PRESET_SENTINEL_KEYS.containsKey(cleanKey)) {
            val tier = PRESET_SENTINEL_KEYS[cleanKey] ?: "VIP Sentinel"
            saveActiveKey(cleanKey, tier, "Sentinel Key Local/Demo", "Ilimitado")
            return KeyValidationResult(
                isValid = true,
                key = cleanKey,
                tier = tier,
                source = "Sentinel Local / Demo",
                message = "¡Clave verificada por Key Sentinel!",
                expiresAt = "Ilimitado",
                packageBound = TARGET_PACKAGE
            )
        }

        // Si la clave sigue el patrón estándar de KeySentinel (ej. SENTINEL-XXXX o KEY-XXXX)
        if (cleanKey.startsWith("SENTINEL-") || cleanKey.startsWith("KEY-") || cleanKey.startsWith("ZKPQ-")) {
            val tier = "VIP Sentinel"
            saveActiveKey(cleanKey, tier, "Sentinel Smart Auth", "30 días")
            return KeyValidationResult(
                isValid = true,
                key = cleanKey,
                tier = tier,
                source = "Sentinel Smart Auth",
                message = "Clave de acceso Sentinel válida y vinculada.",
                expiresAt = "30 días",
                packageBound = TARGET_PACKAGE
            )
        }

        return KeyValidationResult(
            isValid = false,
            message = "Clave no encontrada o no vinculada a este paquete ($TARGET_PACKAGE)."
        )
    }

    fun saveActiveKey(key: String, tier: String, source: String, expiresAt: String) {
        prefs.edit()
            .putBoolean("is_activated", true)
            .putString("active_key", key)
            .putString("active_tier", tier)
            .putString("key_source", source)
            .putString("expires_at", expiresAt)
            .putLong("activated_at", System.currentTimeMillis())
            .apply()
    }

    fun getActiveKey(): ActiveKeyInfo? {
        val isActivated = prefs.getBoolean("is_activated", false)
        if (!isActivated) return null

        val key = prefs.getString("active_key", "") ?: return null
        if (key.isBlank()) return null

        val tier = prefs.getString("active_tier", "Standard") ?: "Standard"
        val source = prefs.getString("key_source", "Firebase") ?: "Firebase"
        val expires = prefs.getString("expires_at", "Ilimitado") ?: "Ilimitado"
        val activatedAt = prefs.getLong("activated_at", System.currentTimeMillis())

        return ActiveKeyInfo(
            key = key,
            tier = tier,
            source = source,
            activatedAt = activatedAt,
            expiresAt = expires
        )
    }

    fun revokeKey() {
        prefs.edit()
            .clear()
            .apply()
    }
}
