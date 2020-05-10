package ee.taltech.mmalia.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ee.taltech.mmalia.ObjectBox
import ee.taltech.mmalia.R
import ee.taltech.mmalia.SessionRecyclerViewAdapter
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.Session_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_session.*

class SessionActivity : AppCompatActivity() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    lateinit var sessionBox: Box<Session>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        Log.d(TAG, "onCreate")

        sessionBox = ObjectBox.boxStore.boxFor()
        //        sessionBox.removeAll()
    }

    override fun onResume() {
        super.onResume()

        layout_session_recycler_view.apply {
            adapter = SessionRecyclerViewAdapter(
                this@SessionActivity,
                sessionBox
                    .query()
                    .orderDesc(Session_.start)
                    .build()
                    .find()
            )
            layoutManager = LinearLayoutManager(this@SessionActivity)
        }
    }
}