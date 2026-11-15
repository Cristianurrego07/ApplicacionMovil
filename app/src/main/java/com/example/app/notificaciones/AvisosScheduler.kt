package com.example.app.notificaciones

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AvisosScheduler {
    fun programar(context: Context) {
        val gestor = WorkManager.getInstance(context)
        val revision = PeriodicWorkRequestBuilder<AvisosWorker>(12, TimeUnit.HOURS).build()
        gestor.enqueueUniquePeriodicWork("revision_vencimientos", ExistingPeriodicWorkPolicy.KEEP, revision)
        val revisionAhora = OneTimeWorkRequestBuilder<AvisosWorker>().build()
        gestor.enqueueUniqueWork("revision_inmediata", ExistingWorkPolicy.REPLACE, revisionAhora)
    }
}
