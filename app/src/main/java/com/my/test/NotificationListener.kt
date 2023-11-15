package com.my.test

import android.service.notification.NotificationListenerService
import android.widget.Toast

class NotificationListener : NotificationListenerService() {
    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        val keys = rankingMap?.orderedKeys?.filter { it.contains(packageName) } ?: return

        if (keys.isNotEmpty() && !keys.first().contains("group_summary")) return // group notif should be first, wait for reordering

        keys.forEachIndexed { index, key ->
            if (index == 0) return@forEachIndexed

            val nextKey = keys.getOrNull(index + 1) ?: return

            val current = activeNotifications.find { it.key == key } ?: return
            val next = activeNotifications.find { it.key == nextKey } ?: return

            if (current.notification.`when` < next.notification.`when`) {
                Toast.makeText(this, "Anomaly detected", Toast.LENGTH_SHORT).show()
                AnomalyDetectorListener.detected()
                return
            }
        }
    }
}