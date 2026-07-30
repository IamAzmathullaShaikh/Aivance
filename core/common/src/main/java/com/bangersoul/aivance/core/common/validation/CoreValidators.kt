package com.bangersoul.aivance.core.common.validation

import com.bangersoul.aivance.core.common.constant.ValidationConstants
import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.Resume
import kotlinx.serialization.json.Json

data class ValidationErrorItem(val field: String, val message: String)

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val errors: List<ValidationErrorItem>) : ValidationResult {
        constructor(field: String, message: String) : this(listOf(ValidationErrorItem(field, message)))
    }

    val isValid: Boolean get() = this is Valid
    val isInvalid: Boolean get() = this is Invalid
}

fun interface Validator<T> {
    fun validate(input: T): ValidationResult
}

infix fun <T> Validator<T>.and(other: Validator<T>): Validator<T> = Validator { input ->
    val first = this.validate(input)
    val second = other.validate(input)
    when {
        first is ValidationResult.Valid && second is ValidationResult.Valid -> ValidationResult.Valid
        first is ValidationResult.Invalid && second is ValidationResult.Invalid ->
            ValidationResult.Invalid(first.errors + second.errors)
        first is ValidationResult.Invalid -> first
        second is ValidationResult.Invalid -> second
        else -> ValidationResult.Valid
    }
}

object StringValidator {
    fun notBlank(field: String = "String"): Validator<String> = Validator { input ->
        if (input.isNotBlank()) ValidationResult.Valid
        else ValidationResult.Invalid(field, "$field must not be blank.")
    }

    fun minLength(min: Int, field: String = "String"): Validator<String> = Validator { input ->
        if (input.length >= min) ValidationResult.Valid
        else ValidationResult.Invalid(field, "$field length must be at least $min characters.")
    }

    fun maxLength(max: Int, field: String = "String"): Validator<String> = Validator { input ->
        if (input.length <= max) ValidationResult.Valid
        else ValidationResult.Invalid(field, "$field length must not exceed $max characters.")
    }

    fun matchesRegex(regex: String, message: String, field: String = "String"): Validator<String> = Validator { input ->
        if (Regex(regex).matches(input)) ValidationResult.Valid
        else ValidationResult.Invalid(field, message)
    }
}

object EmailValidator : Validator<String> {
    override fun validate(input: String): ValidationResult {
        return if (Regex(ValidationConstants.EMAIL_REGEX_PATTERN).matches(input)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("email", "Invalid email address format.")
        }
    }
}

object PhoneValidator : Validator<String> {
    override fun validate(input: String): ValidationResult {
        if (input.isBlank()) return ValidationResult.Valid
        return if (Regex(ValidationConstants.PHONE_REGEX_PATTERN).matches(input)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("phone", "Invalid phone number format.")
        }
    }
}

object PasswordValidator : Validator<String> {
    override fun validate(input: String): ValidationResult {
        return if (input.length >= ValidationConstants.PASSWORD_MIN_LENGTH) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("password", "Password must be at least ${ValidationConstants.PASSWORD_MIN_LENGTH} characters.")
        }
    }
}

object UrlValidator : Validator<String> {
    override fun validate(input: String): ValidationResult {
        return if (Regex(ValidationConstants.URL_REGEX_PATTERN).matches(input)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("url", "Invalid URL format.")
        }
    }
}

object JsonValidator : Validator<String> {
    private val json = Json { ignoreUnknownKeys = true }

    override fun validate(input: String): ValidationResult {
        return try {
            json.parseToJsonElement(input)
            ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid("json", "Invalid JSON syntax: ${e.localizedMessage}")
        }
    }
}

object ResumeValidator : Validator<Resume> {
    override fun validate(input: Resume): ValidationResult {
        val errors = mutableListOf<ValidationErrorItem>()
        if (input.fileName.isBlank()) {
            errors.add(ValidationErrorItem("fileName", "File name cannot be blank."))
        }
        if (input.fileUri.isBlank()) {
            errors.add(ValidationErrorItem("fileUri", "File URI cannot be blank."))
        }
        if (input.rawText.length < ValidationConstants.MIN_RESUME_CHAR_COUNT) {
            errors.add(ValidationErrorItem("rawText", "Resume text must be at least ${ValidationConstants.MIN_RESUME_CHAR_COUNT} characters."))
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

object JobValidator : Validator<JobListing> {
    override fun validate(input: JobListing): ValidationResult {
        val errors = mutableListOf<ValidationErrorItem>()
        if (input.id.isBlank()) {
            errors.add(ValidationErrorItem("id", "Job ID cannot be blank."))
        }
        if (input.title.isBlank()) {
            errors.add(ValidationErrorItem("title", "Job title cannot be blank."))
        }
        if (input.company.isBlank()) {
            errors.add(ValidationErrorItem("company", "Company name cannot be blank."))
        }
        if (input.url.isBlank() || !Regex(ValidationConstants.URL_REGEX_PATTERN).matches(input.url)) {
            errors.add(ValidationErrorItem("url", "Job URL must be a valid non-blank URL."))
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

object ProviderValidator : Validator<String> {
    private val knownProviders = setOf("GEMINI", "OPENAI", "GROQ", "OLLAMA", "OPENROUTER", "APIFY")

    override fun validate(input: String): ValidationResult {
        return if (knownProviders.contains(input.uppercase())) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("providerId", "Unknown or unsupported provider: $input")
        }
    }
}

object ConfigurationValidator : Validator<AiProviderConfig> {
    override fun validate(input: AiProviderConfig): ValidationResult {
        val errors = mutableListOf<ValidationErrorItem>()
        if (input.providerId.isBlank()) {
            errors.add(ValidationErrorItem("providerId", "Provider ID cannot be blank."))
        }
        if (input.isEnabled && input.providerId != "OLLAMA" && input.apiKey.isBlank()) {
            errors.add(ValidationErrorItem("apiKey", "API Key is required for provider ${input.providerId}."))
        }
        if (input.temperature !in 0.0f..1.0f) {
            errors.add(ValidationErrorItem("temperature", "Temperature must be between 0.0 and 1.0."))
        }
        if (input.maxTokens <= 0) {
            errors.add(ValidationErrorItem("maxTokens", "Max tokens must be greater than 0."))
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}
