package com.potados.geomms.viewmodel

import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.activity.MainActivity

class MainViewModel : ViewModel() {
    /**
     * 선택된 탭에 관한 정보를 저장합니다.
     * 현재는 탭이 두 개밖에 없습니다.
     */
    private val selectedTabMenuItemId =  MutableLiveData<Int>()
    fun getSelectedTabMenuItemId(): LiveData<Int> = selectedTabMenuItemId
    fun setSelectedTabMenuItemId(id: Int): Boolean {
        if (id !in MainActivity.TAB_IDS) {
            Log.e("MainViewModel:setSelectedTabMenuItemId()", "Wrong implementation!")
            return false
        }
        selectedTabMenuItemId.value = id

        return true
    }

}