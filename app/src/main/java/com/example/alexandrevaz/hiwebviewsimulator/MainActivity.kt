package com.example.alexandrevaz.hiwebviewsimulator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val beginTransaction = fragmentManager.beginTransaction();
        val wbFragment = MainFragment()
        beginTransaction.add(R.id.container, wbFragment)
        beginTransaction.commit()
    }
}
