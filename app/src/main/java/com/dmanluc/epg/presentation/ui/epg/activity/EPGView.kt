package com.dmanluc.epg.presentation.ui.epg.activity

import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.presentation.base.BaseView

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */
interface EPGView: BaseView {

    fun showData(epgData: EPG)

    fun handleRefreshButton(enable: Boolean)

    fun showLoadingProgress()

    fun hideLoadingProgress()

}