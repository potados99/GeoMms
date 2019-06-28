package com.potados.geomms.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.fragment.MapFragment
import com.potados.geomms.fragment.MessageListFragment
import com.potados.geomms.R
import com.potados.geomms.viewmodel.MainViewModel
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

    /**
     * 메인 액티비티 수명주기동안 함께할 뷰모델.
     */
    private val mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * 뷰모델과 바인딩
         */
        mainViewModel.getSelectedTabMenuItemId().observe(this) {
            switchFragmentByNavigationItemId(it)
        }

        nav_view.setOnNavigationItemSelectedListener { item ->
            mainViewModel.setSelectedTabMenuItemId(item.itemId)
        }

        /**
         * 지도 탭 선택해놓기.
         */
        mainViewModel.setSelectedTabMenuItemId(R.id.menu_item_navigation_map)
    }

    /**
     * 선택된 네비게이션 버튼의 id에 맞게 프래그먼트를 바꾸어줍니다.
     * @return id가 { navigation_home, navigation_message, navigation_setting } 세 개 중 하나가 아니면 false
     */
    private fun switchFragmentByNavigationItemId(navigationItemId: Int): Boolean {
        val fragment = when (navigationItemId) {
            R.id.menu_item_navigation_map -> {
                mapFragment
            }
            R.id.menu_item_navigation_message -> {
                messageListFragment
            }
            /*
            R.id.navigation_setting -> {
                settingFragment
            }
            */

            else -> return false
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        return true
    }

    companion object {
        val TAB_IDS = arrayOf(
            R.id.menu_item_navigation_map,
            R.id.menu_item_navigation_message
        )
    }
}
