package com.dmanluc.epg.app.di.module

import com.dmanluc.epg.app.di.scope.ActivityScope
import com.dmanluc.epg.data.api.EPGApi
import com.dmanluc.epg.data.repository.EPGRepositoryImpl
import com.dmanluc.epg.data.transformer.EPGTransformer
import com.dmanluc.epg.domain.interactor.GetEPGData
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

/**
 *  Module class for providing dependencies
 *
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */
@Module
class EPGActivityModule {

    @ActivityScope
    @Provides
    fun provideEPGService(retrofit: Retrofit): EPGApi {
        return retrofit.create<EPGApi>(EPGApi::class.java)
    }

    @ActivityScope
    @Provides
    fun provideTransformer(): EPGTransformer = EPGTransformer()

    @ActivityScope
    @Provides
    fun provideEPGRepository(service: EPGApi, transformer: EPGTransformer): EPGRepositoryImpl = EPGRepositoryImpl(service, transformer)

    @ActivityScope
    @Provides
    fun provideGetEPGDataUseCase(repository: EPGRepositoryImpl): GetEPGData = GetEPGData(repository)

}