package com.potados.geomms.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.fragment.MapFragment
import com.potados.geomms.fragment.MessageListFragment
import com.potados.geomms.R
import com.potados.geomms.fragment.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 구현 목표:
 * 1. 코드 알아보기 쉽게 작성하기.
 * 2. 안전하게 작성하기.
 */
class MainActivity : AppCompatActivity() {

    /**
     * 시작할 때에 프래그먼트를 모두 만들어 가지고 있음.
     */
    private val mapFragment = MapFragment()
    private val messageListFragment = MessageListFragment()
    private val settingFragment = SettingFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav_view.setOnNavigationItemSelectedListener { item ->
            switchFragmentByNavigationItemId(item.itemId)
        }

        /**
         * navigation_home이 기본.
         */
        switchFragmentByNavigationItemId(R.id.navigation_map)
    }

    /**
     * 선택된 네비게이션 버튼의 id에 맞게 프래그먼트를 바꾸어줍니다.
     * @return id가 { navigation_home, navigation_message, navigation_setting } 세 개 중 하나가 아니면 false
     */
    private fun switchFragmentByNavigationItemId(navigationItemId: Int): Boolean {
        val fragment = when (navigationItemId) {
            R.id.navigation_map -> {
                mapFragment
            }
            R.id.navigation_message -> {
                messageListFragment
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
