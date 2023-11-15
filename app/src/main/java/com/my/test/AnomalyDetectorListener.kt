package com.my.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object AnomalyDetectorListener {
    private var _observer = MutableLiveData<Boolean>()
    var observer: LiveData<Boolean> = _observer

    fun detected() {
        _observer.value = true
    }

    fun reset() {
        _observer.value = false
    }
}