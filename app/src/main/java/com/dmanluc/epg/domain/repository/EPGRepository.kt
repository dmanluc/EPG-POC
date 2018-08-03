package com.dmanluc.epg.domain.repository

import com.dmanluc.epg.domain.entity.EPG
import io.reactivex.Single

/**
 *  Marvel Repository interface
 *
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */
interface EPGRepository {

    fun getEpgInfo(): Single<EPG>

}