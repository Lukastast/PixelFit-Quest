package com.pixelfitquest.health

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient

object HealthConnectIntents {
    private const val PLAY_STORE_PACKAGE = "com.google.android.apps.healthdata"
    private const val PLAY_STORE_WEB =
        "https://play.google.com/store/apps/details?id=$PLAY_STORE_PACKAGE"

    fun openHealthConnectSettings(context: Context) {
        context.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS))
    }

    fun openPlayStore(context: Context) {
        val market = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$PLAY_STORE_PACKAGE"),
        )
        try {
            context.startActivity(market)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_WEB)))
        }
    }
}
