package com.example.doktortahlil.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.doktortahlil.data.DataRepository
import com.example.doktortahlil.data.model.Item

class HomeViewModel : ViewModel() {

    private val repository = DataRepository() // Assuming you have a DataRepository
    val data: LiveData<List<Item>> = repository.data

    fun loadDataFromCSV(isChecked: Boolean) {
        repository.loadDataFromCSV(isChecked)
    }
}
