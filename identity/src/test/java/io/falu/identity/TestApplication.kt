package io.falu.identity

import android.app.Application
import com.google.android.material.R as MaterialR

internal class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setTheme(MaterialR.style.Theme_MaterialComponents_DayNight_NoActionBar)
    }
}