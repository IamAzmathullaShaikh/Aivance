package com.bangersoul.aivance.core.common.security

import kotlinx.serialization.Serializable

/**
 * A wrapper for strings that should be stored encrypted in the database.
 * Used by Room TypeConverters to differentiate between plaintext and ciphertext.
 */
@Serializable
@JvmInline
value class EncryptedString(val value: String)
