package com.example.mobile_handson_29

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val tvDate = findViewById<TextView>(R.id.tvSummaryDate)
        val tvTotal = findViewById<TextView>(R.id.tvSummaryTotal)

        val dateReceived = intent.getStringExtra("DATE_KEY")
        tvDate.text = "Date: $dateReceived"

        if (dateReceived != null) {
            val db = AppDatabase.getDatabase(this)

            lifecycleScope.launch(Dispatchers.IO) {
                val total = try {
                    db.activityDao().getTotalDuration(dateReceived)
                } catch (e: Exception) {
                    0
                }


                withContext(Dispatchers.Main) {
                    tvTotal.text = "Total Duration: $total minutes"
                }
            }
        }
    }
}