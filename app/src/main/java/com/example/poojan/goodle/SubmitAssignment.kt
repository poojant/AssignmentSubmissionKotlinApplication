package com.example.poojan.goodle

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.PendingIntent.getActivity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnPausedListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import org.w3c.dom.Text
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class SubmitAssignment : AppCompatActivity() {

    lateinit var assignmentNumber : TextView
    lateinit var assignmentTitle : TextView
    lateinit var assignmentDate : TextView
    lateinit var assignmentTime : TextView
    lateinit var assignmentStatus : TextView
    lateinit var submitDoc : LinearLayout
    lateinit var downloadDoc : LinearLayout
    lateinit var number : String
    lateinit var title : String
    lateinit var date : String
    lateinit var time : String
    lateinit var documenturi : Uri
    var FILE_INTENT = 4
    val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_assignment)
        val bundle= intent.extras
        if(bundle!=null)
        {
            number = bundle.getString("assignmentNumber")
            title = bundle.getString("assignmentTitle")
            date = bundle.getString("assignmentDate")
            time = bundle.getString("assignmentTime")
        }

        assignmentNumber = findViewById(R.id.submitNo)
        assignmentTitle = findViewById(R.id.submitTitle)
        assignmentDate = findViewById(R.id.submitDate)
        assignmentTime = findViewById(R.id.submitTime)
        assignmentStatus = findViewById(R.id.submitStatus)
        submitDoc = findViewById(R.id.clicksubmit)
        downloadDoc = findViewById(R.id.downloadsubmit)

        assignmentNumber.text = number
        assignmentTitle.text = title
        assignmentDate.text = date+" "+time
        assignmentTime.text = giveEventPrivileges(date,time)

        val ref = FirebaseDatabase.getInstance().getReference().child("user")
                .child("poojan").child("assignment "+number).child("docUri")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            @SuppressLint("ResourceAsColor")
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val doc = p0.getValue(String::class.java)
                    assignmentStatus.text = "Submitted"
                    assignmentStatus.setTextColor(applicationContext.getResources().getColor(R.color.green))
                    submitDoc.visibility = View.GONE
                    downloadDoc.visibility = View.VISIBLE
                    assignmentTime.setTextColor(applicationContext.getResources().getColor(R.color.green))
                }else{
                    assignmentStatus.text = "Not Submitted"
                    assignmentStatus.setTextColor(applicationContext.getResources().getColor(R.color.red))
                    downloadDoc.visibility = View.GONE
                }
            }
        })

        submitDoc.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            val mimeTypes = arrayOf("application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                    "text/plain", "application/pdf", "application/zip")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                if (mimeTypes.size > 0) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            } else {
                var mimeTypesStr = ""
                for (mimeType in mimeTypes) {
                    mimeTypesStr += "$mimeType|"
                }
                intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
            }

            //intent.setType("application/*");
            startActivityForResult(intent, FILE_INTENT)
        }

        downloadDoc.setOnClickListener {
            val result = checkPermission()
            if (result) {
                var db = FirebaseDatabase.getInstance().getReference()
                        .child("user").child("poojan").child("assignment "+number)
                db.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val attachmenlink = dataSnapshot.child("docUri").value.toString()
                        if (attachmenlink == "null" || attachmenlink == null) {
                            Toast.makeText(applicationContext, "No attachments.", Toast.LENGTH_SHORT).show()
                        } else {
                            /* Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(attachmenlink));
                       startActivity(browserIntent);*/
                            val attachmenturi = Uri.parse(attachmenlink)
                            val extension = attachmenturi.lastPathSegment.toString()
                            val request = DownloadManager.Request(attachmenturi)
                            request.setDescription("Attachment downloading")
                            // in order for this if to run, you must use the android 3.2 to compile your app
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.allowScanningByMediaScanner()
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            }
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, extension)
                            Log.d("location info", "" + extension)

                            // get download service and enqueue file
                            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            manager.enqueue(request)

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            } else {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_INTENT && resultCode == RESULT_OK) {
            val dialog = ProgressDialog(this)
            dialog.setMessage("Sending")
            dialog.show()
            documenturi = data!!.getData()
            var filePathColumn : ()->String = { MediaStore.Images.Media.DATA };
            var docName = documenturi.lastPathSegment
            var mStorage = FirebaseStorage.getInstance().getReference().child("poojan")
            mStorage.putFile(documenturi).addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                val location = taskSnapshot.downloadUrl
                Log.d("documentaddress", "" + location.toString())
                val address = location.toString()
                var mDatabase = FirebaseDatabase.getInstance().getReference().child("user").child("poojan").child("assignment "+number)
                mDatabase.child("docName").setValue(docName)
                mDatabase.child("docUri").setValue(address).addOnCompleteListener {
                    dialog.dismiss()
                    Toast.makeText(this,"Uploaded Succesfully",Toast.LENGTH_SHORT).show()
                }
            }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                val current_progress = progress.toInt()
                dialog.setProgress(current_progress)
            }).addOnPausedListener(OnPausedListener<UploadTask.TaskSnapshot> { Toast.makeText(this, "Upload Has Stopped Due to Network Error..", Toast.LENGTH_LONG).show() })
            val args = Bundle()
            args.putString("documenturi", documenturi.toString())
            //val frag = DocumentDialogFragment()
            //frag.setArguments(args)
            //frag.show(getActivity()!!.getFragmentManager(), "txn_tag")
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    val alertBuilder = AlertDialog.Builder(this)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Permission necessary")
                    alertBuilder.setMessage("The access is required to save the files to your device")
                    alertBuilder.setPositiveButton(android.R.string.yes) { dialog, which -> ActivityCompat.requestPermissions(this as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_WRITE_STORAGE) }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(this as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_WRITE_STORAGE)
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
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
