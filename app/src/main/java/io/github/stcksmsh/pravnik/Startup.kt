package io.github.stcksmsh.pravnik

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SeedInitializer : Initializer<Unit> {
    override fun create(context: android.content.Context) {
        val request = OneTimeWorkRequestBuilder<InitialLoadWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork("seed", ExistingWorkPolicy.KEEP, request)
    }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
