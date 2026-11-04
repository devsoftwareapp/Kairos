package com.devsoftware.kairos.notifications
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
object ReminderScheduler{
 fun schedule(c:Context,id:String,title:String,at:Long){
  val delay=(at-System.currentTimeMillis()).coerceAtLeast(0)
  val req=OneTimeWorkRequestBuilder<CapsuleReminderWorker>().setInitialDelay(delay,TimeUnit.MILLISECONDS)
   .setInputData(Data.Builder().putString("capsule_id",id).putString("capsule_title",title).build()).build()
  WorkManager.getInstance(c).enqueueUniqueWork("kairos_capsule_$id",ExistingWorkPolicy.REPLACE,req)
 }
 fun cancel(c:Context,id:String){WorkManager.getInstance(c).cancelUniqueWork("kairos_capsule_$id")}
}