/*
 *   Copyright (c) 2016-2019 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file
 */

package com.pspdfkit.labs.quickdemo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebView
import android.widget.Toast
import com.pspdfkit.labs.quickdemo.DemoMode
import com.pspdfkit.labs.quickdemo.R
import com.pspdfkit.labs.quickdemo.toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * An interactive introduction guide for first-time users.
 */
class SetupGuideActivity : AppCompatActivity() {

    var permissionChecking: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val demoMode = DemoMode.get(this)

        // If the app has already been granted the required permissions, there's no need to setup.
        if (demoMode.requiredPermissionsGranted && demoMode.demoModeAllowed) finish()

        // Setup content is just informative. We're using a webview to show pre-formatted instructions.
        setContentView(R.layout.activity_setup_guide)
        val webView: WebView = findViewById(R.id.webview)
        webView.loadUrl("file:///android_asset/setup-guide.html")

        // We periodically check if the permission has been granted. Once this happened, we notify the user and finish the activity.
        permissionChecking = Observable.interval(1, TimeUnit.SECONDS)
            .filter { demoMode.requiredPermissionsGranted && demoMode.demoModeAllowed }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                toast("QuickDemo has been successfully set up! Ready for activating demo mode.", Toast.LENGTH_LONG)
                ConfigurationActivity.launch(this)
                finish()
            }
            .subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop permission checking when destroying the activity (preventing leaks).
        permissionChecking?.dispose()
        permissionChecking = null
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(intent(context))
        }

        fun intent(context: Context) = Intent(context, SetupGuideActivity::class.java)
    }
}
