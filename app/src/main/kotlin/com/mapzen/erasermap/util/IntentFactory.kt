package com.mapzen.erasermap.util

import android.content.Intent
import android.net.Uri

open class IntentFactory {
    open fun newIntent(action: String, data: String): Intent {
        return Intent(action, Uri.parse(data))
    }
}
