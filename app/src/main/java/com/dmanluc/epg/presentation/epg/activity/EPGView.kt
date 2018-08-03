package com.dmanluc.epg.presentation.epg.activity

import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.presentation.base.BaseView

/**
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    2/8/18.
 */
interface EPGView: BaseView {

    fun showData(epgData: EPG)

    fun showLoadingProgress()

    fun hideLoadingProgress()

}