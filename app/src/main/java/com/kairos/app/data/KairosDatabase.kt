package com.devsoftware.kairos.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
@Database(entities=[CapsuleEntity::class],version=2,exportSchema=false)
abstract class KairosDatabase:RoomDatabase(){
 abstract fun capsuleDao():CapsuleDao
 companion object{
  private val MIGRATION_1_2=object:Migration(1,2){
   override fun migrate(db:SupportSQLiteDatabase){
    db.execSQL("CREATE TABLE IF NOT EXISTS capsules_new (id TEXT NOT NULL, encryptedTitle BLOB NOT NULL, encryptedPayloadPath TEXT NOT NULL, payloadType TEXT NOT NULL, createdAtEpochMillis INTEGER NOT NULL, unlockAtEpochMillis INTEGER NOT NULL, wrappedContentKey BLOB NOT NULL, schemaVersion INTEGER NOT NULL, PRIMARY KEY(id))")
    db.execSQL("INSERT INTO capsules_new (id,encryptedTitle,encryptedPayloadPath,payloadType,createdAtEpochMillis,unlockAtEpochMillis,wrappedContentKey,schemaVersion) SELECT id,encryptedTitle,COALESCE(encryptedPayloadPath,''),payloadType,sealedAtEpochMillis,unlockAtEpochMillis,wrappedContentKey,schemaVersion FROM capsules")
    db.execSQL("DROP TABLE capsules")
    db.execSQL("ALTER TABLE capsules_new RENAME TO capsules")
   }
  }
  @Volatile private var INSTANCE:KairosDatabase?=null
  fun get(context:Context):KairosDatabase=INSTANCE?: synchronized(this){
   INSTANCE?:Room.databaseBuilder(context.applicationContext,KairosDatabase::class.java,"kairos.db")
    .addMigrations(MIGRATION_1_2).build().also{INSTANCE=it}
  }
 }
}