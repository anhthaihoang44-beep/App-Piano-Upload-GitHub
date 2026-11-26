package com.pianoflow.app.core.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

class LocalizedContextWrapper(
    base: Context,
    private val localizedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
    override fun getAssets(): AssetManager = localizedResources.assets
}

object LocaleHelper {

    const val LANG_EN = "en"
    const val LANG_VI = "vi"

    fun wrapContext(base: Context, languageCode: String): Context {
        val code = if (languageCode == LANG_VI) LANG_VI else LANG_EN
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        val configContext = base.createConfigurationContext(config)
        return LocalizedContextWrapper(base, configContext.resources)
    }

    tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
