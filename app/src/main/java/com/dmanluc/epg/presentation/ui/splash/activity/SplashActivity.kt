package com.dmanluc.epg.presentation.ui.splash.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dmanluc.epg.presentation.ui.epg.activity.EPGActivity

/**
 *  Initial splash activity
 *
 * @author   Daniel Manrique <daniel.manrique@uxsmobile.com>
 * @version  1
 * @since    6/8/18.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, EPGActivity::class.java)
        startActivity(intent)
        finish()
    }

}