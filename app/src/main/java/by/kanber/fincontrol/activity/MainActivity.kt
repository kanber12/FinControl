package by.kanber.fincontrol.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kanber.fincontrol.R
import by.kanber.fincontrol.database.DBHelper
import by.kanber.fincontrol.transaction.TransactionListFragment

class MainActivity : AppCompatActivity() {
    lateinit var helper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helper = DBHelper(this, null)
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = TransactionListFragment()
        transaction.replace(R.id.fragment_container, fragment, fragment.javaClass.name)
        transaction.commit()
    }
}
