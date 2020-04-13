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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        Log.d(TAG, "onCreate")

        val sessionBox: Box<Session> = ObjectBox.boxStore.boxFor()

//        sessionBox.removeAll()

        layout_session_recycler_view.adapter = SessionRecyclerViewAdapter(
            this,
            sessionBox.query().orderDesc(Session_.start).build().find()
        )
        layout_session_recycler_view.layoutManager = LinearLayoutManager(this)

        //TODO: DB + recycler view
    }
}