package com.dmanluc.epg.data.api

import com.dmanluc.epg.data.contract.EPGApiResponse
import io.reactivex.Single
import retrofit2.http.GET

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */
interface EPGApi {

    @GET("epg")
    fun getEpgData(): Single<EPGApiResponse>

}