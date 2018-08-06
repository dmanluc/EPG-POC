package com.dmanluc.epg.presentation.ui.epg.activity

import com.dmanluc.epg.domain.entity.EPG
import com.dmanluc.epg.domain.interactor.BaseObserver
import com.dmanluc.epg.domain.interactor.GetEPGData
import com.dmanluc.epg.presentation.base.BasePresenter
import org.parceler.Parcel
import javax.inject.Inject

/**
 * @author   Daniel Manrique <dmanluc91@gmail.com>
 * @version  1
 * @since    2/8/18.
 */
class EPGPresenterImpl @Inject constructor(private val getEpgDataUseCase: GetEPGData) : BasePresenter<EPGView, EPGPresenterImpl.State>() {

    fun fetchEPGData() {
        view?.handleRefreshButton(false)
        view?.showLoadingProgress()
        getEpgDataUseCase.execute(object : BaseObserver<EPG>() {
            override fun onSuccess(t: EPG) {
                super.onSuccess(t)
                view?.hideLoadingProgress()
                view?.showData(t)
            }

            override fun onErrorMessage(errorMessage: String?) {
                super.onErrorMessage(errorMessage)
                view?.hideLoadingProgress()
                view?.handleRefreshButton(true)
                view?.showErrorMessage(errorMessage)
            }
        }, null)
    }

    override fun newState(): State = State()

    @Parcel
    class State : BasePresenter.State
}