package com.devsoftware.kairos.security
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
object AesGcmCrypto{
 private const val NONCE=12
 data class SealedData(val nonce:ByteArray,val ciphertext:ByteArray)
 fun generateContentKey():SecretKey=KeyGenerator.getInstance("AES").apply{init(256)}.generateKey()
 fun encrypt(key:SecretKey,plain:ByteArray,aad:ByteArray?=null):SealedData{
  val nonce=ByteArray(NONCE).also{SecureRandom().nextBytes(it)}
  val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key,GCMParameterSpec(128,nonce));aad?.let(c::updateAAD)
  return SealedData(nonce,c.doFinal(plain))
 }
 fun decrypt(key:SecretKey,data:SealedData,aad:ByteArray?=null):ByteArray{
  require(data.nonce.size==NONCE)
  val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key,GCMParameterSpec(128,data.nonce));aad?.let(c::updateAAD)
  return c.doFinal(data.ciphertext)
 }
 fun pack(d:SealedData)=ByteBuffer.allocate(d.nonce.size+d.ciphertext.size).put(d.nonce).put(d.ciphertext).array()
 fun unpack(b:ByteArray):SealedData{require(b.size>NONCE+16);return SealedData(b.copyOfRange(0,NONCE),b.copyOfRange(NONCE,b.size))}
}