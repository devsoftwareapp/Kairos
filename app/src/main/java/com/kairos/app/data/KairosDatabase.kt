package com.devsoftware.kairos.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities=[CapsuleEntity::class],version=2,exportSchema=false)
abstract class KairosDatabase:RoomDatabase(){
 abstract fun capsuleDao():CapsuleDao
 companion object{
  @Volatile private var INSTANCE:KairosDatabase?=null
  fun get(context:Context):KairosDatabase=INSTANCE?: synchronized(this){
   INSTANCE?:Room.databaseBuilder(context.applicationContext,KairosDatabase::class.java,"kairos.db").fallbackToDestructiveMigration().build().also{INSTANCE=it}
  }
 }
}