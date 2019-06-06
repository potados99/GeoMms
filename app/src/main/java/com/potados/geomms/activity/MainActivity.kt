package com.potados.geomms.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.potados.geomms.fragment.MapFragment
import com.potados.geomms.fragment.MessageListFragment
import com.potados.geomms.R
import com.potados.geomms.fragment.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mapFragment = MapFragment()
    private val messageFragment = MessageListFragment()
    private val settingFragment = SettingFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav_view.setOnNavigationItemSelectedListener { item ->
            switchFragment(item.itemId)
        }

        switchFragment(R.id.navigation_home)
    }


    /**
     * Switch fragment by id of selected menu item.
     */
    private fun switchFragment(id: Int): Boolean {
        val fragment = when (id) {
            R.id.navigation_home -> {
                mapFragment
            }
            R.id.navigation_message -> {
                messageFragment
            }
            R.id.navigation_setting -> {
                settingFragment
            }

            else -> return false
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        return true
    }
}
