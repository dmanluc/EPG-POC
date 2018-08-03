package com.dmanluc.epg.data.transformer

import com.dmanluc.epg.app.isoDateToMillis
import com.dmanluc.epg.data.contract.EPGApiResponse
import com.dmanluc.epg.domain.entity.Channel
import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.domain.entity.Schedule

/**
 *  Transformer to adapt EPG output contract from JSON response to EPG domain entity of the app
 *
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */
class EPGTransformer: Transformer<EPGApiResponse, EPG> {

    override fun transformContractToModelEntity(outputContract: EPGApiResponse): EPG {
        return EPG(channels = outputContract.channels.orEmpty().map {
            Channel(it.id.orEmpty(), it.title.orEmpty(), it.imagesData?.logoUrl.orEmpty(), it.schedules.orEmpty().map {
                Schedule(it.id.orEmpty(), it.title.orEmpty(), it.start.orEmpty().isoDateToMillis(), it.end.orEmpty().isoDateToMillis())
            })
        })
    }

}