package com.android.googleplaysubs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.googleplaysubs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var billingModule: BillingModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        billingModule = BillingModule(this)

        with(binding) {
            month1Subs.setOnClickListener {

            }

            month6Subs.setOnClickListener {

            }
        } // with
    } // onCreate

}
