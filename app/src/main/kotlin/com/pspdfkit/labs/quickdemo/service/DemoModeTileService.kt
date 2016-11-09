/*
 * DemoModeTileService.kt
 *
 *   PSPDFKit
 *
 *   Copyright (c) 2016 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file
 */

package com.pspdfkit.labs.quickdemo.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.pspdfkit.labs.quickdemo.DemoMode
import com.pspdfkit.labs.quickdemo.R
import com.pspdfkit.labs.quickdemo.activity.SetupGuideActivity

class DemoModeTileService : TileService() {

    private lateinit var demoMode: DemoMode

    override fun onCreate() {
        super.onCreate()
        demoMode = DemoMode.get(this)
    }

    override fun onStartListening() {
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_demo_mode)
        qsTile.label = "Demo mode"
        updateIcon()
    }

    override fun onClick() {
        if (!demoMode.requiredPermissionsGranted || !demoMode.demoModeAllowed) {
            startActivityAndCollapse(SetupGuideActivity.intent(this))
            return
        }

        demoMode.enabled = !demoMode.enabled
        updateIcon()

        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        sendBroadcast(it)
    }

    private fun updateIcon() {
        qsTile.state = if (demoMode.enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}