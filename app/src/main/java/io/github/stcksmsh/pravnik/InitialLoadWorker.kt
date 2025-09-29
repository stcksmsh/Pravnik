package io.github.stcksmsh.pravnik

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.stcksmsh.pravnik.data.db.AppDb
import io.github.stcksmsh.pravnik.data.seed.DataSeeder

@HiltWorker
class InitialLoadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val db: AppDb,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            DataSeeder(db).seedIfEmpty()
            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.retry()
        }
    }
}
