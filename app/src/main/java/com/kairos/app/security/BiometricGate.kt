package com.devsoftware.kairos.security
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
object BiometricGate {
 fun authenticate(activity:FragmentActivity,onSuccess:()->Unit,onError:(String)->Unit){
  val authenticators=BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
  if(BiometricManager.from(activity).canAuthenticate(authenticators)!=BiometricManager.BIOMETRIC_SUCCESS){
   onError("Bu cihazda biyometrik veya ekran kilidi doğrulaması kullanılamıyor.");return
  }
  val prompt=BiometricPrompt(activity,ContextCompat.getMainExecutor(activity),object:BiometricPrompt.AuthenticationCallback(){
   override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult){onSuccess()}
   override fun onAuthenticationError(errorCode:Int,errString:CharSequence){onError(errString.toString())}
  })
  prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Kairos kapsülünü aç")
   .setSubtitle("Devam etmek için kimliğini doğrula").setAllowedAuthenticators(authenticators).build())
 }
}