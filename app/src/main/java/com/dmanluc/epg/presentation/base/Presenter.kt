package com.dmanluc.epg.presentation.base

import android.os.Bundle

/**
 * Presenter Interface Template for MVP pattern
 *
 * @author Daniel Manrique Lucas
 */
interface Presenter<in V : BaseView> {

    /**
     * Attach presenter's view.
     */
    fun attachView(view: V)

    /**
     * Detach presenter's view
     */
    fun detachView()

    /**
     * Check if presenter has attached its view
     */
    fun isViewAttached(): Boolean

    /**
     * Save presenter's state
     */
    fun saveState(bundle: Bundle)

    /**
     * Restore presenter's state
     */
    fun recoverState(savedStateInstance: Bundle?)

}
