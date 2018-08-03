package com.dmanluc.epg.presentation.base

import android.os.Bundle
import com.dmanluc.epg.BuildConfig
import org.parceler.Parcel
import org.parceler.Parcels
import java.lang.ref.WeakReference

/**
 * Base Presenter Template for MVP pattern
 *
 * @author Daniel Manrique Lucas
 */
abstract class BasePresenter<V : BaseView, out S : BasePresenter.State> : Presenter<V> {

    val view: V?
        get() = weakReference?.get()


    private var state: S? = null
    private var weakReference: WeakReference<V>? = null

    override fun attachView(view: V) {
        if (!isViewAttached()) {
            weakReference = WeakReference(view)
        }
    }

    override fun detachView() {
        weakReference?.clear()
        weakReference = null
    }

    override fun isViewAttached(): Boolean = weakReference?.get() != null

    override fun saveState(bundle: Bundle) {
        bundle.putParcelable(KEY_PRESENTER_STATE, Parcels.wrap(state))
    }

    override fun recoverState(savedStateInstance: Bundle?) {
        if (savedStateInstance != null && savedStateInstance.containsKey(KEY_PRESENTER_STATE)) {
            state = Parcels.unwrap<S>(savedStateInstance.getParcelable(KEY_PRESENTER_STATE))
        } else {
            state = newState()
        }

        if (BuildConfig.DEBUG) {
            if (state == null) {
                throw IllegalStateException("Presenter " + javaClass
                        .simpleName + "returns a null in newState().")
            } else if (!(state as S).javaClass.isAnnotationPresent(Parcel::class.java)) {
                throw RuntimeException("Presenter " + javaClass
                        .simpleName + " uses a presenter state " + (state as S).javaClass.simpleName + " which is not annotated with @Parcel " +
                                       "annotation and as such cannot be persisted.")
            }
        }
    }

    fun getState(): S = state ?: newState()

    protected abstract fun newState(): S

    interface State

    companion object {

        private val KEY_PRESENTER_STATE: String = "key:bundle:presenter_state"

    }

}
