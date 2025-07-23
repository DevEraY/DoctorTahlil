package com.example.doktortahlil.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doktortahlil.Item
import java.io.BufferedReader
import java.io.InputStreamReader
import android.content.Context

class DataRepository(private val context: Context) {

    private val _data = MutableLiveData<List<Item>>()
    val data: LiveData<List<Item>> get() = _data

    fun loadDataFromCSV(isChecked: Boolean) {
        val itemList = mutableListOf<Item>()
        val user_gender = if (isChecked) "Female" else "Male"

        try {
            val inputStream = context.assets.open("parametreler.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?

            // Skip the header line
            reader.readLine()

            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(";") // Assuming CSV is semicolon-separated
                val gender_in_csv = tokens[1]
                if (tokens.size >= 4 && user_gender != gender_in_csv) { // Make sure the line has at least 4 columns
                    val tahlil = tokens[0]
                    val lowerLimit = tokens[2]
                    val upperLimit = tokens[3]
                    val unit = tokens[4]
                    val low_message = tokens[5]
                    val high_message = tokens[6]
                    val refRange = "Referans Aralığı : $lowerLimit - $upperLimit $unit"
                    itemList.add(Item(tahlil, refRange, low_message, high_message))
                }
            }

            reader.close()
            _data.value = itemList
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
