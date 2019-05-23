/*
 *   Copyright (c) 2016-2019 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file
 */

package com.pspdfkit.labs.quickdemo

import android.content.Context
import android.widget.Toast

/** Shows a normal toast message. */
fun Context.toast(message: CharSequence, length: Int = Toast.LENGTH_SHORT)
    = Toast.makeText(this, message, length).show()