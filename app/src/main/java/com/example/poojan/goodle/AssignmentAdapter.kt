package com.example.poojan.goodle

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AssignmentAdapter(context : Context,val  layoutResId: Int,val list: List<Assignment>)
    :ArrayAdapter<Assignment>(context, layoutResId, list){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(layoutResId, null)

        val assignmentNo = view.findViewById<TextView>(R.id.assignmentNo)
        val assignmentTitle = view.findViewById<TextView>(R.id.assignmentTitle)
        val assignmentStatus = view.findViewById<TextView>(R.id.assignmentStatus)
        val assignmentTime = view.findViewById<TextView>(R.id.assignmentTime)
        val assignmentDoc = view.findViewById<TextView>(R.id.assignmentDoc)
        val file = view.findViewById<LinearLayout>(R.id.file)
        val time = view.findViewById<LinearLayout>(R.id.time)

        val assignment = list[position]
        assignmentNo.text = assignment.number.toString()
        assignmentTitle.text = assignment.title
        //val time = giveEventPrivileges();
        val ref = FirebaseDatabase.getInstance().getReference().child("user")
                .child("poojan").child("assignment "+assignment.number).child("docName")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            @SuppressLint("ResourceAsColor")
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val doc = p0.getValue(String::class.java)
                    assignmentStatus.text = "Submitted"
                    assignmentStatus.setTextColor(context.getResources().getColor(R.color.green))
                    time.visibility = View.GONE
                    assignmentDoc.text = doc
                }else{
                    assignmentStatus.text = "Not Submitted"
                    assignmentStatus.setTextColor(context.getResources().getColor(R.color.red))
                    val dbassignment = FirebaseDatabase.getInstance().getReference()
                            .child("assignments").child("assignment "+assignment.number)
                    dbassignment.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            val assignmentItem = p0!!.getValue(Assignment::class.java)
                            assignmentTime.text =giveEventPrivileges(assignmentItem!!.date, assignmentItem!!.time)
                        }
                    })
                    file.visibility = View.GONE
                }
            }
        })
        return view
    }

    private fun giveEventPrivileges(fromDate: String,
                                    fromTime: String): String {

        val eventDate = beautifyDateTime(fromDate, fromTime)
        // log the date
        Log.d("Date", eventDate)

        // get the current time and compute the difference
        val currentTime = getCurrentTime()
        Log.d("Date", currentTime)

        val time = getDifference(eventDate, currentTime)
        if (time != null) {

            val days = time.days
            val hours = time.hours
            val minutes = time.minutes
            val seconds = time.seconds

            // log all the time info
            Log.d("Date", days.toString())
            Log.d("Date", hours.toString())
            Log.d("Date", minutes.toString())
            Log.d("Date", seconds.toString())

            if (days < 0 || hours < 0 || minutes < 0 || seconds < 0) {
                // the event is over
                return "Due date is over";
            } else {
                // allow the creator to send notifications to people
                //time_left.setVisibility(View.VISIBLE)
                // show how much time is left for the event on the text box
                //notifTitle = "Time left: "
                //hasStarted = false

                if (days > 0) {
                    if (days > 1){
                        return days.toString()+" days"
                    }else {
                        return days.toString()+ " day"
                    }
                } else if (hours > 0) {
                    if (hours > 1) {
                        return hours.toString()+" hours"
                    }else{
                        return hours.toString() + " hour"
                    }
                } else if (minutes > 0) {
                    if (minutes > 1){
                        return minutes.toString()+" minutes"
                    }else{
                        return minutes.toString()+" minute"
                    }
                } else {
                    //hasStarted = true  // since the event has almost started...
                    if (seconds > 1){
                        return seconds.toString()+" seconds"
                    }else{
                        return seconds.toString()+" second"
                    }
                }
            }
        }
        return "submitted"
    }

    private fun getDifference(eventDate: String,
                              currentTime: String): Time? {

        //HH converts hour in 24 hours format (0-23), day calculation
        val format = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)

        try {
            val d1 = format.parse(eventDate)
            val d2 = format.parse(currentTime)

            //in milliseconds
            val diff = d1.time - d2.time

            val diffSeconds = diff / 1000 % 60
            val diffMinutes = diff / (60 * 1000) % 60
            val diffHours = diff / (60 * 60 * 1000) % 24
            val diffDays = diff / (24 * 60 * 60 * 1000)

            return Time(diffHours, diffDays, diffMinutes, diffSeconds)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun getCurrentTime(): String {

        val formatter = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)
        val date = Date()
        return formatter.format(date)
    }

    private fun beautifyDateTime(fromDate: String, fromTime: String): String {

        // change the strings to make parsing them possible
        val from = fromDate.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        // from[0]: month, from[1]: day, from[2]: year
        val year = "20" + from[2]
        // append seconds 00 for the time
        val time = "$fromTime:00"
        // build the complete date
        return from[0] + "/" + from[1] + "/" + year + " " + time
    }
}