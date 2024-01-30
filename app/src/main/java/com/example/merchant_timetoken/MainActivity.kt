package com.example.merchant_timetoken

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.example.merchant_timetoken.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView(){
        binding.btnPay.setOnClickListener{
           // val cardNo = binding.etCardNumber.text.toString().trim()
//            if(!isCardNumberValid(cardNo)){
//                Toast.makeText(applicationContext,"Card Invalid",Toast.LENGTH_SHORT).show()
//            }else{
                val intent = Intent(this, TimeTokenGateway::class.java)
            //    intent.putExtra("cardNo", cardNo)
                startActivity(intent)
          //  }
        }
    }

}