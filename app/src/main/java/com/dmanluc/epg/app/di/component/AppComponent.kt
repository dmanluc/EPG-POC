package com.dmanluc.epg.app.di.component

import android.content.Context
import com.dmanluc.epg.app.di.module.AppModule
import dagger.Component
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * App Component Interface for Dependency Injection (DI)
 *
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */
@Singleton
@Component(modules = [(AppModule::class)])
interface AppComponent {

    fun provideApplication(): Context

    fun provideRetrofit(): Retrofit

}