package com.bangersoul.aivance.core.common.constant

object DatabaseConstants {
    const val DATABASE_NAME = "aivance-database"
    const val DATABASE_VERSION = 4

    const val TABLE_RESUMES = "resumes"
    const val TABLE_APPLICATIONS = "applications"
    const val TABLE_ATS_RESULTS = "ats_results"
    const val TABLE_COVER_LETTERS = "cover_letters"
    const val TABLE_ROADMAPS = "roadmaps"
    const val TABLE_ROADMAP_STEPS = "roadmap_steps"
    const val TABLE_INTERVIEW_SESSIONS = "interview_sessions"
    const val TABLE_INTERVIEW_MESSAGES = "interview_messages"
    const val TABLE_JOBS = "jobs"
    const val TABLE_AI_CONVERSATIONS = "ai_conversations"
    const val TABLE_AI_MESSAGES = "ai_messages"
}

object NetworkConstants {
    const val DEFAULT_TIMEOUT_SECONDS = 30L
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_USER_AGENT = "User-Agent"
    const val HEADER_X_API_KEY = "X-Api-Key"

    const val CONTENT_TYPE_JSON = "application/json"

    const val MAX_RETRIES = 3
    const val RETRY_BACKOFF_INITIAL_MS = 1000L
    const val RETRY_BACKOFF_FACTOR = 2.0
}

object ProviderConstants {
    const val PROVIDER_GEMINI = "GEMINI"
    const val PROVIDER_OPENAI = "OPENAI"
    const val PROVIDER_GROQ = "GROQ"
    const val PROVIDER_OLLAMA = "OLLAMA"
    const val PROVIDER_OPENROUTER = "OPENROUTER"
    const val PROVIDER_APIFY = "APIFY"

    const val DEFAULT_TEMPERATURE = 0.7f
    const val DEFAULT_MAX_TOKENS = 2048
    const val DEFAULT_CONTEXT_WINDOW = 8192

    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
    const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
    const val OLLAMA_DEFAULT_BASE_URL = "http://localhost:11434/"
    const val APIFY_BASE_URL = "https://api.apify.com/v2/"
}

object RouteConstants {
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_RESUME = "resume"
    const val ROUTE_ATS = "ats"
    const val ROUTE_COVER_LETTER = "cover_letter"
    const val ROUTE_INTERVIEW = "interview"
    const val ROUTE_JOBS = "jobs"
    const val ROUTE_TRACKER = "tracker"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_SETTINGS = "settings"
}

object KeyConstants {
    const val DATASTORE_NAME = "aivance_user_preferences"
    const val KEYSTORE_ALIAS = "aivance_encrypted_datastore_key"

    const val KEY_GEMINI_API_KEY = "gemini_api_key"
    const val KEY_OPENAI_API_KEY = "openai_api_key"
    const val KEY_GROQ_API_KEY = "groq_api_key"
    const val KEY_APIFY_TOKEN = "apify_token"
    const val KEY_USER_ID = "user_id"
}

object ValidationConstants {
    const val EMAIL_REGEX_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    const val PHONE_REGEX_PATTERN = "^\\+?[0-9]{7,15}$"
    const val URL_REGEX_PATTERN = "^(https?|ftp)://[^\\s/$.?#].\\S*$"
    const val PASSWORD_MIN_LENGTH = 8
    const val MIN_RESUME_CHAR_COUNT = 50
    const val MAX_RESUME_CHAR_COUNT = 500000
}

object AnalyticsConstants {
    const val EVENT_RESUME_UPLOADED = "resume_uploaded"
    const val EVENT_RESUME_ANALYZED = "resume_analyzed"
    const val EVENT_ATS_CHECK_COMPLETED = "ats_check_completed"
    const val EVENT_COVER_LETTER_GENERATED = "cover_letter_generated"
    const val EVENT_MOCK_INTERVIEW_STARTED = "mock_interview_started"
    const val EVENT_MOCK_INTERVIEW_COMPLETED = "mock_interview_completed"
    const val EVENT_JOB_SEARCHED = "job_searched"
    const val EVENT_JOB_SAVED = "job_saved"
    const val EVENT_APPLICATION_CREATED = "application_created"
    const val EVENT_PROVIDER_SWITCHED = "provider_switched"

    const val PARAM_PROVIDER_ID = "provider_id"
    const val PARAM_MODEL_NAME = "model_name"
    const val PARAM_SCORE = "score"
    const val PARAM_LATENCY_MS = "latency_ms"
    const val PARAM_SUCCESS = "success"
}

object PreferencesConstants {
    const val PREF_FILE_NAME = "user_preferences.json"
    const val DEFAULT_SYNC_INTERVAL_HOURS = 24
    const val DEFAULT_CACHE_RETENTION_DAYS = 7
}
