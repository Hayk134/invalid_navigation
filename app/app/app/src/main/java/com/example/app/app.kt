package com.example.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("576b91a0-ac5c-421a-a932-38cbe1d4c633")
        MapKitFactory.initialize(this)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Новые места"
            val descriptionText = "Уведомления о доступных местах поблизости"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("PLACES_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}