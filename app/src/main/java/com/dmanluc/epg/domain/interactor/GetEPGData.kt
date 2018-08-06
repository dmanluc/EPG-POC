package com.dmanluc.epg.domain.interactor

import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.domain.repository.EPGRepository
import io.reactivex.Single

/**
 *  Use case to get EPG data from EPGApi
 *
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */
class GetEPGData(private val repository: EPGRepository) : UseCase<EPG, Any?>() {

    override fun buildUseCaseObservable(params: Any?): Single<EPG> = repository.getEpgInfo()

}