package io.github.stcksmsh.pravnik

import android.content.Context
import androidx.startup.Initializer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SeedInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        SeedScheduler.markShouldSeed(context.applicationContext)
    }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}



object SeedScheduler {
    @Volatile private var shouldSeed = false

    fun markShouldSeed(appCtx: Context) { shouldSeed = true }

    fun maybeSeed(appCtx: Context) {
        if (!shouldSeed) return
        shouldSeed = false

        val request = OneTimeWorkRequestBuilder<InitialLoadWorker>().build()
        WorkManager.getInstance(appCtx)
            .enqueueUniqueWork("seed", ExistingWorkPolicy.KEEP, request)
    }
}
