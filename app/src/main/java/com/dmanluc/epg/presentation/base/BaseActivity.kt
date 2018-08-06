package com.dmanluc.epg.presentation.base

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.Nullable
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.dmanluc.epg.app.core.App
import com.dmanluc.epg.app.di.component.AppComponent
import kotlinx.android.synthetic.main.activity_epg.loadingContainer
import kotlinx.android.synthetic.main.activity_epg.loadingIndicator


/**
 * Base Activity Template for MVP pattern
 *
 * @author Daniel Manrique Lucas
 */
abstract class BaseActivity<in V : BaseView, out P : Presenter<V>> : AppCompatActivity() {

    @get:LayoutRes protected abstract val layoutId: Int

    protected abstract val screenTitle: String?

    protected abstract val showToolbar: Boolean

    protected abstract val presenter: P

    protected val appComponent: AppComponent = App.appComponent

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provideDaggerDependency()
        setContentView(layoutId)
        setActionBar(screenTitle, showBackArrow(), showToolbar)
        disableTouchLoadingView()
        presenter.attachView(this as V)
        presenter.recoverState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this as V)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.attachView(this as V)
    }

    override fun onPause() {
        presenter.detachView()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    protected open fun provideDaggerDependency() {}

    protected abstract fun showBackArrow(): Boolean

    open fun setActionBar(heading: String?, enableBackArrow: Boolean, show: Boolean) {
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_TITLE
            title = heading.orEmpty()
            setDisplayHomeAsUpEnabled(enableBackArrow)
            setDisplayShowHomeEnabled(enableBackArrow)
            if (show) show() else hide()
        }
    }

    protected fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    protected fun showLoading() {
        loadingContainer.visibility = VISIBLE
        loadingIndicator.smoothToShow()
    }

    protected fun hideLoading() {
        loadingIndicator.smoothToHide()
        loadingContainer.visibility = GONE
    }

    private fun disableTouchLoadingView() {
        loadingContainer.setOnTouchListener { _, _ -> true }
    }

}
