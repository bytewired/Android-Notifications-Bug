package com.my.test

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.my.test.ui.theme.TestAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val notificationId = Random.nextInt()
    private val notificationManager by lazy { getSystemService<NotificationManager>() }
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isAnomalyDetected by AnomalyDetectorListener.observer.observeAsState(false)

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isAnomalyDetected) {
                            AnomalyDetectedText()
                            job?.cancel()
                        }

                        SpamButton(isAnomalyDetected) {
                            if (job?.isActive == true) job?.cancel() else startGeneratingNotifications()
                            AnomalyDetectorListener.reset()
                        }
                    }
                }
            }
        }

        notificationManager?.createNotificationChannel(
            NotificationChannel(
                TEST_CHANNEL_ID,
                "Test Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    private fun startGeneratingNotifications() {
        var num = 1

        notificationManager?.cancelAll()
        job = lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                repeat(Int.MAX_VALUE) {
                    sendNotification(num)
                    updateGroup()

                    delay(300) // imitate image loading
                    updateWithRichContent(num)

                    if (it > 0 && it % 10 == 0) notificationManager?.cancelAll() // cleanup for readability

                    num++
                    delay(5_000)
                }
            }
        }
    }

    private fun sendNotification(num: Int) {
        NotificationCompat.Builder(this, TEST_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("Num: $num")
            .setGroup(TEST_GROUP_KEY)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .build()
            .let { notificationManager?.notify(notificationId + num, it) }

    }

    private fun updateWithRichContent(num: Int) {
        val img = BitmapFactory.decodeResource(resources, R.raw.img)

        NotificationCompat.Builder(this, TEST_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("Num: $num")
            .setGroup(TEST_GROUP_KEY)
            .setPriority(Notification.PRIORITY_HIGH)
            .setLargeIcon(img)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(img))
            .build()
            .let { notificationManager?.notify(notificationId + num, it) }
    }

    private fun updateGroup() {
        NotificationCompat.Builder(this, TEST_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.InboxStyle())
            .setGroup(TEST_GROUP_KEY)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setAutoCancel(true)
            .build()
            .let { notificationManager?.notify("group_summary", GROUP_ID, it) }
    }

    private companion object {
        const val TEST_CHANNEL_ID = "TEST_CHANNEL"
        const val TEST_GROUP_KEY = "TEST_GROUP"
        const val GROUP_ID = 35232342
    }
}

@Composable
fun AnomalyDetectedText() {
    Text(
        text = "Anomaly detected",
        color = Color.Red
    )
}

@Composable
fun SpamButton(isAnomalyDetected: Boolean, onClick: () -> Unit) {
    var isStarted by remember { mutableStateOf(false) }

    if (isAnomalyDetected) isStarted = false

    Button(onClick = {
        isStarted = !isStarted
        onClick()
    }) {
        Text(text = if (isStarted) "Stop notification spam" else "Start notification spam")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestAppTheme {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnomalyDetectedText()
            SpamButton(true) {
            }
        }
    }
}
