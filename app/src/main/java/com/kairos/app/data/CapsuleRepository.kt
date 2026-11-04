package com.devsoftware.kairos.data
import android.content.Context
import com.devsoftware.kairos.security.AesGcmCrypto
import com.devsoftware.kairos.security.KeystoreKeyManager
import java.io.File
import java.util.UUID
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
data class CapsuleDraft(val title:String,val type:String,val unlockAt:Long,val content:ByteArray)
data class CapsuleSummary(val id:String,val title:String,val type:String,val createdAt:Long,val unlockAt:Long)
class CapsuleRepository(context:Context){
 private val app=context.applicationContext;private val dao=KairosDatabase.get(app).capsuleDao()
 private val keys=KeystoreKeyManager();private val dir=File(app.filesDir,"sealed_capsules").apply{mkdirs()}
 fun observeCapsules():Flow<List<CapsuleEntity>> = dao.observeAll()
 suspend fun create(d:CapsuleDraft):String=withContext(Dispatchers.IO){
  require(d.title.isNotBlank()&&d.content.isNotEmpty()&&d.unlockAt>System.currentTimeMillis())
  val id=UUID.randomUUID().toString();val key=AesGcmCrypto.generateContentKey()
  val title=AesGcmCrypto.pack(AesGcmCrypto.encrypt(key,d.title.toByteArray(),id.toByteArray()))
  val payload=AesGcmCrypto.pack(AesGcmCrypto.encrypt(key,d.content,id.toByteArray()))
  val wrapped=AesGcmCrypto.pack(AesGcmCrypto.encrypt(keys.getOrCreateWrappingKey(),key.encoded,id.toByteArray()))
  val file=File(dir,"$id.krs");file.writeBytes(payload)
  try{dao.insert(CapsuleEntity(id,title,file.absolutePath,d.type,System.currentTimeMillis(),d.unlockAt,wrapped))}
  catch(t:Throwable){file.delete();throw t};id
 }
 suspend fun summaries(rows:List<CapsuleEntity>):List<CapsuleSummary> =withContext(Dispatchers.IO){
  rows.map{r->val title=AesGcmCrypto.decrypt(unwrap(r),AesGcmCrypto.unpack(r.encryptedTitle),r.id.toByteArray()).toString(Charsets.UTF_8)
   CapsuleSummary(r.id,title,r.payloadType,r.createdAtEpochMillis,r.unlockAtEpochMillis)}
 }
 suspend fun readPayload(id:String):ByteArray=withContext(Dispatchers.IO){
  val r=dao.findById(id)?:error("Kapsül bulunamadı");val f=File(r.encryptedPayloadPath);check(f.isFile)
  AesGcmCrypto.decrypt(unwrap(r),AesGcmCrypto.unpack(f.readBytes()),id.toByteArray())
 }
 suspend fun delete(id:String)=withContext(Dispatchers.IO){dao.findById(id)?.let{File(it.encryptedPayloadPath).delete()};dao.deleteById(id)}
 private fun unwrap(r:CapsuleEntity)=SecretKeySpec(AesGcmCrypto.decrypt(keys.getOrCreateWrappingKey(),AesGcmCrypto.unpack(r.wrappedContentKey),r.id.toByteArray()),"AES")
}