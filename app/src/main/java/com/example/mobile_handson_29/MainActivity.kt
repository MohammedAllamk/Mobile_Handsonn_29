package com.example.mobile_handson_29

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(this)

        setContent {
            MaterialTheme {
                FitnessTrackerScreen(db, this.lifecycleScope, this)
            }
        }
    }
}

@Composable
fun FitnessTrackerScreen(
    db: AppDatabase,
    scope: androidx.lifecycle.LifecycleCoroutineScope,
    activityContext: Context
) {
    val context = LocalContext.current

    // States
    var activityName by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Select Date") }
    var activities by remember { mutableStateOf(listOf<ActivityEntity>()) }

    fun loadActivities(filterDate: String? = null) {
        scope.launch(Dispatchers.IO) {
            val fetchedList = if (filterDate != null && filterDate != "Select Date") {
                db.activityDao().getByDate(filterDate)
            } else {
                db.activityDao().getAll()
            }
            withContext(Dispatchers.Main) {
                activities = fetchedList
            }
        }
    }


    LaunchedEffect(Unit) {
        loadActivities()
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Fitness Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = activityName,
            onValueChange = { activityName = it },
            label = { Text("Activity Name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = durationText,
            onValueChange = { durationText = it.filter { char -> char.isDigit() } },
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )


        Button(onClick = {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                activityContext,
                { _, year, month, dayOfMonth ->
                    selectedDate = "$year-${month + 1}-$dayOfMonth"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text("Date: $selectedDate")
        }


        Button(
            onClick = {
                val duration = durationText.toIntOrNull()
                if (activityName.isNotBlank() && duration != null && selectedDate != "Select Date") {
                    scope.launch(Dispatchers.IO) {
                        db.activityDao().insert(ActivityEntity(activityName = activityName.trim(), duration = duration, date = selectedDate))
                        withContext(Dispatchers.Main) {
                            activityName = ""
                            durationText = ""
                            loadActivities()
                            Toast.makeText(context, "Activity Saved!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Add Activity")
        }


        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { loadActivities(selectedDate) }, modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                Text("Filter")
            }
            Button(onClick = { loadActivities() }, modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text("Show All")
            }
        }

        Divider()

       
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(activities) { activity ->
                ActivityCard(activity = activity, context = activityContext)
            }
        }
    }
}


@Composable
fun ActivityCard(activity: ActivityEntity, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {

                val intent = Intent(context, SummaryActivity::class.java)
                intent.putExtra("DATE_KEY", activity.date)
                context.startActivity(intent)
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = activity.activityName, style = MaterialTheme.typography.titleMedium)
            Text(text = "${activity.duration} min on ${activity.date}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Database(entities = [ActivityEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "fitness_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}