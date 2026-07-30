package com.bangersoul.aivance.sdk.api

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata

/**
 * Interface for OCR (Optical Character Recognition) providers.
 * Inherits from [BaseProvider] to manage metadata and lifecycle.
 */
abstract class OcrProvider(
    metadata: ProviderMetadata,
    capabilities: Set<ProviderCapability>
) : BaseProvider(metadata, capabilities) {

    /**
     * Extracts text from an image or document.
     * @param uri The URI of the image or document file.
     * @return Result containing the extracted text or an error.
     */
    abstract suspend fun extractText(uri: String): Result<String>
}
