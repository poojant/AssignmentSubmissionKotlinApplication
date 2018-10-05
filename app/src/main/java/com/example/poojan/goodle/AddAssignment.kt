package com.example.poojan.goodle

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*

class AddAssignment : AppCompatActivity() {

    lateinit var assignNo : EditText
    lateinit var assignTitle : EditText
    lateinit var dateend : TextView
    lateinit var timeend : TextView
    lateinit var submitbtn : Button
    lateinit var fromDatepicker: DatePickerDialog.OnDateSetListener
    internal var myCalendar = Calendar.getInstance()

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_assignment)

        assignNo = findViewById(R.id.assignNo)
        assignTitle = findViewById(R.id.assignTitle);
        dateend = findViewById(R.id.dateend);
        timeend = findViewById(R.id.timeend);
        submitbtn = findViewById(R.id.submit);

        dateend.setOnClickListener {
            DatePickerDialog(this, fromDatepicker, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        timeend.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute = mcurrentTime.get(Calendar.MINUTE)

            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener
            { timePicker, selectedHour, selectedMinute -> timeend.setText(String.format("%02d:%02d", selectedHour, selectedMinute)) },
                    hour, minute, true)
            mTimePicker.setTitle("Select Time")
            timeend.setTextColor(R.color.black)
            mTimePicker.show()
        }

        fromDatepicker = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "MM/dd/yy" //In which you need put here
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            dateend.setText(sdf.format(myCalendar.time))
            dateend.setTextColor(R.color.black)
        }

        submitbtn.setOnClickListener {
            val num = assignNo.text.toString().trim()
            val title = assignTitle.text.toString().trim()
            val date = dateend.text.toString().trim()
            val time = timeend.text.toString().trim()

            if(num.isEmpty()){
                Toast.makeText(this,"Please Enter Assignment Number", Toast.LENGTH_SHORT).show()
            }
            else if(title.isEmpty()){
                Toast.makeText(this,"Please Enter Assignment Title", Toast.LENGTH_SHORT).show()
            }else if(date.equals("Select Date")){
                Toast.makeText(this,"Please Enter Assignment Date", Toast.LENGTH_SHORT).show()
            }else if(time.equals("Select Time")){
                Toast.makeText(this,"Please Enter Assignment Time", Toast.LENGTH_SHORT).show()
            }else{
                val number = num.toInt()
                val assignment = Assignment(number, title, date, time)
                val ref = FirebaseDatabase.getInstance().getReference("assignments")
                ref.child("assignment "+num).setValue(assignment).addOnCompleteListener {
                    Toast.makeText(this, "Assignment Added Succesfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
