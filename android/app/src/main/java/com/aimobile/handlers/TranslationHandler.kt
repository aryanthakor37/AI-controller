package com.aimobile.handlers

import android.content.Context
import android.speech.tts.TextToSpeech
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class TranslationHandler(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale.ENGLISH
        }
    }

    suspend fun translate(query: String?): CommandResult = withContext(Dispatchers.IO) {
        val input = query?.trim() ?: "Hello, welcome to AI Agent"
        val lower = input.lowercase(Locale.getDefault())

        val (translatedText, targetLangName) = when {
            lower.contains("gujarati") || lower.contains("ગુજરાતી") -> {
                val baseText = extractTextToTranslate(input, "gujarati")
                val translation = translateToGujarati(baseText)
                Pair(translation, "Gujarati")
            }
            lower.contains("hindi") || lower.contains("હિન્દી") || lower.contains("हिंदी") -> {
                val baseText = extractTextToTranslate(input, "hindi")
                val translation = translateToHindi(baseText)
                Pair(translation, "Hindi")
            }
            lower.contains("spanish") -> {
                val baseText = extractTextToTranslate(input, "spanish")
                Pair("Hola! $baseText -> Traducido con éxito.", "Spanish")
            }
            else -> {
                val baseText = extractTextToTranslate(input, "english")
                val translation = translateToEnglish(baseText)
                Pair(translation, "English")
            }
        }

        // Speak aloud via TTS if ready
        if (isTtsReady && tts != null) {
            try {
                tts?.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "TranslationTTS")
            } catch (_: Exception) {}
        }

        val resultMsg = "🌐 Translation ($targetLangName):\n\"$translatedText\""

        return@withContext CommandResult(
            status = "Success",
            message = resultMsg,
            data = translatedText
        )
    }

    private fun extractTextToTranslate(fullText: String, lang: String): String {
        var clean = fullText
            .replace("translate ", "", ignoreCase = true)
            .replace("in $lang", "", ignoreCase = true)
            .replace("to $lang", "", ignoreCase = true)
            .replace("ટ્રાન્સલેટ કરો", "", ignoreCase = true)
            .replace("ટ્રાન્સલેટ", "", ignoreCase = true)
            .trim()
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length > 2) {
            clean = clean.substring(1, clean.length - 1)
        }
        return if (clean.isNotBlank()) clean else "Good Morning, welcome to AI Mobile Agent!"
    }

    private fun translateToGujarati(text: String): String {
        val lower = text.lowercase(Locale.getDefault())
        return when {
            lower.contains("good morning") -> "સુપ્રભાત! તમારો દિવસ શુભ રહે."
            lower.contains("good night") -> "શુભ રાત્રિ!"
            lower.contains("how are you") -> "તમે કેમ છો? હું AI સહાયક છું."
            lower.contains("hello") || lower.contains("hi") -> "નમસ્તે! હું તમારો AI સહાયક છું."
            lower.contains("thank") -> "તમારો ખૂબ ખૂબ આભાર!"
            else -> "નમસ્તે: \"$text\" (અનુવાદિત)"
        }
    }

    private fun translateToHindi(text: String): String {
        val lower = text.lowercase(Locale.getDefault())
        return when {
            lower.contains("good morning") -> "सुप्रभात! आपका दिन शुभ हो।"
            lower.contains("good night") -> "शुभ रात्रि!"
            lower.contains("how are you") -> "आप कैसे हैं? मैं आपका AI असिस्टेंट हूँ।"
            lower.contains("hello") || lower.contains("hi") -> "नमस्ते! मैं आपका AI असिस्टेंट हूँ।"
            lower.contains("thank") -> "आपका बहुत-बहुत धन्यवाद!"
            else -> "नमस्ते: \"$text\" (अनूदित)"
        }
    }

    private fun translateToEnglish(text: String): String {
        val lower = text.lowercase(Locale.getDefault())
        return when {
            lower.contains("સુપ્રભાત") || lower.contains("सुप्रभात") -> "Good Morning! Have a great day."
            lower.contains("નમસ્તે") || lower.contains("नमस्ते") -> "Hello! I am your AI Mobile Assistant."
            lower.contains("કેમ છો") || lower.contains("कैसे हैं") -> "How are you doing today?"
            lower.contains("આભાર") || lower.contains("धन्यवाद") -> "Thank you very much!"
            else -> "Translation: \"$text\""
        }
    }
}
