package com.example.poojan.goodle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_assignment -> {
                val intent = Intent(this, AddAssignment::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    lateinit var assignmentList: MutableList<Assignment>
    lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        assignmentList = mutableListOf()
        listView = findViewById(R.id.listView)
        val assignment_id = ArrayList<Assignment>()

        val ref = FirebaseDatabase.getInstance().getReference("assignments")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    for(h in p0.children){
                        val assignment = h.getValue(Assignment::class.java)
                        assignmentList.add(assignment!!)
                        assignment_id.add(assignment)
                    }
                    val adapter = AssignmentAdapter(applicationContext, R.layout.assignment_viewholder, assignmentList)
                    listView.adapter = adapter
                }
            }
        })

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            //Toast.makeText(this,assignment_id[position].title,Toast.LENGTH_LONG).show()
            val i = Intent(applicationContext, SubmitAssignment::class.java)
            i.putExtra("assignmentNumber", assignment_id.get(position).number.toString())
            i.putExtra("assignmentTitle", assignment_id.get(position).title)
            i.putExtra("assignmentDate", assignment_id.get(position).date)
            i.putExtra("assignmentTime", assignment_id.get(position).time)
            startActivity(i)
        }
    }
}
