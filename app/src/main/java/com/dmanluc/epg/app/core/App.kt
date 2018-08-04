package com.dmanluc.epg.app.core

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import com.dmanluc.epg.BuildConfig
import com.dmanluc.epg.app.di.component.AppComponent
import com.dmanluc.epg.app.di.component.DaggerAppComponent
import com.dmanluc.epg.app.di.module.AppModule

/**
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */
class App : Application() {

    companion object {

        lateinit var appComponent: AppComponent

    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initDagger()
    }

    private fun initDagger() {
        appComponent = DaggerAppComponent
                .builder()
                .appModule(AppModule(this as Context, BuildConfig.EPG_BASE_URL))
                .build()
    }

}