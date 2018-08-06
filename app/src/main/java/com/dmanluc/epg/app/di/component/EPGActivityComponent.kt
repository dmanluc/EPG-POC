package com.dmanluc.epg.app.di.component

import com.dmanluc.epg.app.di.module.EPGActivityModule
import com.dmanluc.epg.app.di.scope.ActivityScope
import com.dmanluc.epg.presentation.ui.epg.activity.EPGActivity
import dagger.Component

/**
 *  Component Interface for Dependency Injection (DI) in EPGActivity
 *
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */
@ActivityScope
@Component(modules = [(EPGActivityModule::class)], dependencies = [(AppComponent::class)])
interface EPGActivityComponent {

    fun inject(activity: EPGActivity)

}