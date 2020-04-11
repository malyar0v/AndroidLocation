package ee.taltech.mmalia.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ee.taltech.mmalia.ObjectBox
import ee.taltech.mmalia.R
import ee.taltech.mmalia.SessionRecyclerViewAdapter
import ee.taltech.mmalia.model.Session
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

/*        sessionBox.put(Session().apply {
            locations.add(SimpleLocation(54.55, 20.95, Date().time))
            locations.add(SimpleLocation(54.58, 20.45, Date().time + 100 * 1000))
            locations.add(SimpleLocation(54.65, 20.49, Date().time + 200 * 1000))
            checkpoints.add(SimpleLocation(54.58, 20.45, Date().time + 100 * 1000))
            waypoints.add(SimpleLocation(54.65, 20.49, Date().time + 200 * 1000))
        })*/

        layout_session_recycler_view.adapter = SessionRecyclerViewAdapter(this, sessionBox.all)
        layout_session_recycler_view.layoutManager = LinearLayoutManager(this)

        //TODO: DB + recycler view
    }
}