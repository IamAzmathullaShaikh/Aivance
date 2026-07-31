package com.bangersoul.aivance.core.database.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.bangersoul.aivance.core.common.security.EncryptedString
import com.bangersoul.aivance.core.util.EncryptionService
import javax.inject.Inject

@ProvidedTypeConverter
class EncryptedTypeConverters @Inject constructor(
    private val encryptionService: EncryptionService
) {
    @TypeConverter
    fun fromEncryptedString(encrypted: EncryptedString?): String? {
        return encrypted?.value?.let { encryptionService.encrypt(it) }
    }

    @TypeConverter
    fun toEncryptedString(cipherText: String?): EncryptedString? {
        return cipherText?.let {
            val decrypted = encryptionService.decrypt(it)
            EncryptedString(decrypted)
        }
    }
}
