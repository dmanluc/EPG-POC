package com.dmanluc.epg.presentation.epg.activity

import android.os.Bundle
import com.dmanluc.epg.R
import com.dmanluc.epg.app.di.component.DaggerEPGActivityComponent
import com.dmanluc.epg.domain.entity.Channel
import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.domain.entity.Schedule
import com.dmanluc.epg.presentation.base.BaseActivity
import com.dmanluc.epg.presentation.widgets.EPGWidget
import kotlinx.android.synthetic.main.activity_epg.bottomNavigation
import kotlinx.android.synthetic.main.activity_epg.epg
import javax.inject.Inject

class EPGActivity : BaseActivity<EPGView, EPGPresenterImpl>(), EPGView {

    @Inject
    lateinit var internalPresenter: EPGPresenterImpl

    override val layoutId: Int
        get() = R.layout.activity_epg
    override val screenTitle: String?
        get() = ""
    override val showToolbar: Boolean
        get() = false
    override val presenter: EPGPresenterImpl
        get() = internalPresenter

    override fun showBackArrow(): Boolean = false

    override fun provideDaggerDependency() {
        DaggerEPGActivityComponent.builder()
                .appComponent(appComponent)
                .build().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bottomNavigation.apply {
            setTextVisibility(false)
            enableAnimation(false)
            enableItemShiftingMode(false)
            enableShiftingMode(false)
        }
        presenter.fetchEPGData()
    }

    override fun showData(epgData: EPG) {
        epg.setEPGData(epgData)
        epg.setEPGClickListener(object : EPGWidget.EPGClickListener {
            override fun onChannelClicked(channelPosition: Int, epgChannel: Channel) {
            }

            override fun onEventClicked(channelPosition: Int, programPosition: Int, epgEvent: Schedule) {
            }

            override fun onNowButtonClicked() {
                epg.resetView(withAnimation = true)
            }
        })
    }

    override fun showLoadingProgress() {
        showLoading()
    }

    override fun hideLoadingProgress() {
        hideLoading()
    }

}
