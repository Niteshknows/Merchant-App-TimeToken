package com.example.merchant_timetoken

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.example.merchant_timetoken.databinding.ActivityTimeTokenGatewayBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.io.UnsupportedEncodingException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar


class TimeTokenGateway : AppCompatActivity() {
    private lateinit var binding: ActivityTimeTokenGatewayBinding
    lateinit var cardNo : String
    lateinit var pinEntered : String
    lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeTokenGatewayBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // val receivedIntent = intent
        //cardNo = receivedIntent.getStringExtra("cardNo") ?: "1"
        initView()
    }

    private fun initView() {
        setupFirestore()
      //  binding.etCardNumber.text = cardNo
        binding.btnPay.setOnClickListener{
            if(!validateCardNumber()){
                return@setOnClickListener
            }
            pinEntered = binding.etPinEntered.text.toString()
            if(pinEntered.isNullOrEmpty() || pinEntered.length!=6){
                Toast.makeText(applicationContext,"PIN must be 6 digits",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
               validatePIN()
            }
        }
    }

    fun setupFirestore(){
        firestore = FirebaseFirestore.getInstance()
    }

    private fun validatePIN() {
        val collectionReference = firestore.collection("users")

        collectionReference.addSnapshotListener { value, error ->
            if (value == null || error != null) {
                Toast.makeText(applicationContext, "Error fetching data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            val usersList = value.toObjects(User::class.java) as ArrayList<User>
            val cardNumberToCheck = cardNo
            // Check if there is any user with the specified card number
            val userWithCardNumber = usersList.find { it.cardNumber == cardNumberToCheck }

            if (userWithCardNumber != null) {
                // User found with the specified card number
                val signupTimestamp = userWithCardNumber.signupTimestamp
                signupTimestamp?.let {
                    verifyPayment(signupTimestamp)
                }
            } else {
                // No user found with the specified card number
                Toast.makeText(applicationContext, "No user found with the specified card number", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun validateCardNumber() : Boolean{
        cardNo = binding.etCardNumber.text.toString()
        if(cardNo.isNullOrBlank() || cardNo.length!=16 || !cardNo.isDigitsOnly()){
        Toast.makeText(applicationContext,"Card Invalid",Toast.LENGTH_SHORT).show()
            return false
        }
            return true
        }

    private fun verifyPayment(signupTimestamp: String){
        val tempTimestamp = System.currentTimeMillis()
        val currentTimestamp = roundToNearestTwoMinutes(tempTimestamp)

        val stringToEncrypt = "$signupTimestamp:$currentTimestamp"
        Log.d("nitesh1", "actual  time $tempTimestamp")
        Log.d("nitesh1", "rounded time $currentTimestamp")
        val encryptedString = AESEncryptionMethod(stringToEncrypt)?.takeLast(6)

        val calculatedPIN = generatePinFromAlphanumeric(encryptedString!!)
        if(pinEntered.equals(calculatedPIN)){
            val webView = binding.ivSuccessView

            // Enable JavaScript in the WebView if needed
            webView.settings.javaScriptEnabled = true

            // Set a WebViewClient to handle navigation events inside the WebView
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    // Return false to load the URL in the WebView itself
                    return false
                }
            }
            Toast.makeText(applicationContext,"Payment Successful",Toast.LENGTH_SHORT).show()
            }else{
            Toast.makeText(applicationContext,"Please enter valid PIN !",Toast.LENGTH_SHORT).show()
        }


    }
    fun roundToNearestTwoMinutes(inputTimestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = inputTimestamp

        // Extracting minutes from the current timestamp
        val minutes = calendar.get(Calendar.MINUTE)

        // Calculate the remaining minutes to the next multiple of 2
        val remainingMinutes = minutes % 2

        // Round down the minutes to the nearest floor value of 2
        val roundedMinutes = minutes - remainingMinutes

        // Set the rounded minutes back to the calendar
        calendar.set(Calendar.MINUTE, roundedMinutes)
        // Set seconds and milliseconds to 0 for consistency
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }


    fun generatePinFromAlphanumeric(input: String): String {
        val charToDigitMap = mutableMapOf<Char, Char>()
        // Use a hash-based approach to map characters to digits
        input.forEach {
            if (!charToDigitMap.containsKey(it)) {
                val randomDigit = ('0' + (it.hashCode() and Int.MAX_VALUE) % 10).toChar()
                charToDigitMap[it] = randomDigit
            }
        }
        // Transform each character into a digit
        val pinDigits = input.map { charToDigitMap.getValue(it) }
        // Take the first 6 digits (or pad with zeros if needed)
        val pin = pinDigits.take(6).joinToString("").padEnd(6, '0')
        return pin
    }


    @Throws(UnsupportedEncodingException::class)
    private fun AESEncryptionMethod(string: String): String? {
        val aes = EncodeDecodeAES()
        var encrypted: String? = null
        try {
            Log.d("TAG", "the message before enc is$string")
            encrypted = EncodeDecodeAES.bytesToHex(aes.encrypt(string))
            Log.d("after encryption", "AESEncryptionMethod: "+encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return encrypted
    }

    @Throws(UnsupportedEncodingException::class)
    fun AESDecryptionMethod(string: String?): String? {
        val aes = EncodeDecodeAES()
        var decrypted: String? = null
        try {
            decrypted = String(aes.decrypt(string))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //        Toast.makeText(getApplicationContext() ,"the message after dec is"+decrypted,Toast.LENGTH_SHORT).show();
//
        return decrypted
    }
}