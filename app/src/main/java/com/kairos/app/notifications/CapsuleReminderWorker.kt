package com.devsoftware.kairos.notifications
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devsoftware.kairos.R
class CapsuleReminderWorker(c:Context,p:WorkerParameters):CoroutineWorker(c,p){
 override suspend fun doWork():Result{
  val title=inputData.getString("capsule_title")?:"Geleceğe bıraktığın kapsül";val channel="kairos_capsule_reminders"
  val mgr=applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26)mgr.createNotificationChannel(NotificationChannel(channel,"Kapsül hatırlatmaları",NotificationManager.IMPORTANCE_DEFAULT))
  val n=NotificationCompat.Builder(applicationContext,channel).setSmallIcon(R.drawable.ic_kairos_notification).setContentTitle("Kairos kapsülün hazır").setContentText(title).setAutoCancel(true).build()
  return try{NotificationManagerCompat.from(applicationContext).notify((inputData.getString("capsule_id")?:title).hashCode(),n);Result.success()}catch(_:SecurityException){Result.success()}
 }
}