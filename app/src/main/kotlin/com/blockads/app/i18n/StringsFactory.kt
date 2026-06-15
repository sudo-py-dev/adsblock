package com.blockads.app.i18n

import android.content.Context

object StringsFactory {
    fun getStrings(context: Context): LocalizedStrings {
        val languageCode = context.resources.configuration.locales[0].language
        return when (languageCode) {
            "he" -> StringsHe
            "ar" -> StringsAr
            "fr" -> StringsFr
            "de" -> StringsDe
            "es" -> StringsEs
            else -> StringsEn
        }
    }
}
