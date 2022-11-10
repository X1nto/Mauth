package com.xinto.mauth.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.xinto.mauth.domain.repository.MainRepository

class MainViewModel(
    mainRepository: MainRepository
) : ViewModel() {

    val privateMode = mainRepository.observeSecureMode()

}