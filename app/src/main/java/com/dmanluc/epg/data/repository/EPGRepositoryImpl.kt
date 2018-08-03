package com.dmanluc.epg.data.repository

import com.dmanluc.epg.data.api.EPGApi
import com.dmanluc.epg.data.transformer.EPGTransformer
import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.domain.repository.EPGRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 *  EPG remote repository implementation
 *
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */
class EPGRepositoryImpl
@Inject constructor(private val api: EPGApi, private val transformer: EPGTransformer): EPGRepository {

    override fun getEpgInfo(): Single<EPG> {
        return api.getEpgData().map { transformer.transformContractToModelEntity(it) }
    }

}