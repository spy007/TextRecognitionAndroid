package com.tuts.prakash.simpleocr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tuts.prakash.simpleocr.data.LocalRepository

class ScanPhoneNumberModel(): ViewModel() {
    private lateinit var phoneNumber: LiveData<String>

    fun getPhoneNumber(): LiveData<String> {
        if (phoneNumber == null) {
            phoneNumber = LocalRepository.getPhoneNumber()
        }

        return phoneNumber
    }
}