package com.example.doktortahlil


import android.content.Context
import android.content.res.Configuration
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doktortahlil.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import java.util.*
import android.content.Intent
import android.util.Log
import kotlin.system.exitProcess

var isLocaleChanging = false
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter  // Assuming you named your adapter CustomAdapter
    private val messagesList = mutableListOf<String>() // List to store messages


    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = sharedPreferences.getString("SelectedLanguage", "en")
        logCurrentLocale()
        Log.d("CurrentLanguageCheck", "Suanki dil: ${currentLanguage}")
        applyLocale(currentLanguage ?: "en") // Load the saved language
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        lateinit var recyclerView: RecyclerView


        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize the switch and its listener
        //////////////////////////////////////////////////////////////////////////////////////////
        val themeSwitch = findViewById<SwitchCompat>(R.id.themeSwitch)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Apply dark theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // Apply light theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

        }
        /////////////////////////////////////////////////////////////////////////////////////////


        ////////////////////////////////////////////////////////////////////////////////////////
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        ////checking if the user male or female
        val switchCompat = findViewById<SwitchCompat>(R.id.gender_switch)
        val isChecked = switchCompat.isChecked
        var itemList = loadDataFromCSV(isChecked).toMutableList()
        var adapter = CustomAdapter(itemList, this)
        val hesaplaButton = findViewById<Button>(R.id.Hesapla)

        hesaplaButton.setOnClickListener {
            val message = adapter.getMessages()
            if (message.isNotEmpty()) {
                showPopup(message)
                adapter.clearMessages()
            }
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
        }

        ////////////////////////////////////////changing genders////////////////////////////////////////////////
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            // Reload data based on the new state of the switch
            val updatedItemList = loadDataFromCSV(isChecked)
            adapter.updateItemList(updatedItemList)
        }
        checkAndShowDisclaimer()
        val infoButton = findViewById<Button>(R.id.infoButton)
        infoButton.setOnClickListener {
            showAboutUsPopup()
        }

        val searchView: SearchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filterItems(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filterItems(newText ?: "")
                return true
            }
        })
        // Initialize the spinner and its listener
        // Initialize the spinner
        val spinner: Spinner = findViewById(R.id.language_spinner)

        // Create an ArrayAdapter using a string array and a default spinner layout
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages,  // This should be defined in your `strings.xml`
            android.R.layout.simple_spinner_item
        )

        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the ArrayAdapter to the spinner
        spinner.adapter = spinnerAdapter

        // Retrieve the saved language from SharedPreferences
        val savedLanguage = sharedPreferences.getString("SelectedLanguage", "en")
        val languagePosition = when (savedLanguage) {
            "en" -> 0  // English
            "tr" -> 1  // Turkish
            "fi" -> 2  // Finnish
            else -> 1  // Default to English
        }

        // Set the spinner selection to the saved language
        spinner.setSelection(languagePosition)


        // Set up the spinner item selected listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguageCode = when (position) {
                    0 -> "en"  // English
                    1 -> "tr"  // Turkish
                    2 -> "fi"  // Finnish
                    else -> "en"
                }

                if (selectedLanguageCode != currentLanguage) {
                    isLocaleChanging = false
                    setLocale(selectedLanguageCode)
                    // Save the selected language to SharedPreferences
                    sharedPreferences.edit().putString("SelectedLanguage", selectedLanguageCode)
                        .apply()
                }
                // Change the locale

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }


        }
    }


    private fun setLocale(localeName: String) {
        if (isLocaleChanging==false) {
            isLocaleChanging = true
            val myLocale = Locale(localeName)
            val res = resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.setLocale(myLocale) // Update the locale configuration
            res.updateConfiguration(conf, dm)

            // Save the selected language to SharedPreferences
            val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("SelectedLanguage", localeName)
                apply()
            }

            restartApp()
        }
    }

    private fun applyLocale(localeName: String) {
        val myLocale = Locale(localeName)
        Locale.setDefault(myLocale)

        val res = resources
        val conf = res.configuration
        conf.setLocale(myLocale) // Update the locale configuration
        res.updateConfiguration(conf, res.displayMetrics)


    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        finish() // Close the current activity
        startActivity(intent) // Start the activity again
    }


    private fun logCurrentLocale() {
        val currentLocale = resources.configuration.locale
        Log.d("LocaleCheck", "Current Language: ${currentLocale.language}")
    }







    private fun checkAndShowDisclaimer() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hasAcceptedDisclaimer = sharedPreferences.getBoolean("accepted_disclaimer", false)

        if (!hasAcceptedDisclaimer) {
            showDisclaimerDialog(sharedPreferences)
        }}

    private fun showDisclaimerDialog(sharedPreferences: SharedPreferences) {
        val disclaimerText = """
            Doktor Tahlil uygulaması, kullanıcılara tahlil sonuçlarını okumada ve bu sonuçlar hakkında genel bilgilendirme sağlamada yardımcı olmak amacıyla geliştirilmiştir. Uygulama, kullanıcıların tahlil sonuçlarında belirtilen değerleri (örneğin, yüksek LDL kolesterol) tanımlayabilir ve bu değerlere ilişkin genel sağlık bilgileri sunar. Örneğin, yüksek LDL kolesterol seviyeleri kalp krizi riskini artırabilir.
            
            Ancak, bu uygulama tıbbi tanı veya tedavi amaçlı değildir. Sağlık durumunuzla ilgili kesin bilgi ve tıbbi tavsiye için mutlaka bir sağlık profesyoneline danışmanız gerekmektedir. Uygulamanın sağladığı bilgiler, doktorunuzun veya sağlık uzmanınızın verdiği tavsiyelerin yerine geçmez.
            
            Uygulamayı kullanarak, bu feragatnameyi okuduğunuzu ve anladığınızı kabul etmiş olursunuz. Uygulama geliştiricileri, bu bilgilendirmelere dayanarak yapılan işlemlerden sorumlu tutulamaz.
        """.trimIndent()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Feragatname")
        builder.setMessage(disclaimerText)
        builder.setPositiveButton("Kabul Ediyorum") { dialog, _ ->
            with(sharedPreferences.edit()) {
                putBoolean("accepted_disclaimer", true)
                apply()
            }
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.create().show()
    }

    private fun showAboutUsPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_aboutus, null)

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // allows the popup to be dismissed when touched outside
        )

        // Set up the close button
        val closeButton = view.findViewById<Button>(R.id.popup_close_button)
        closeButton.setOnClickListener {
            popupWindow.dismiss()
        }

        // Show the popup at the center of the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    fun loadDataFromCSV(isChecked: Boolean): List<Item> {
        val itemList = mutableListOf<Item>()
        val user_gender = if (isChecked) "Female" else "Male"
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val currentLanguage = sharedPreferences.getString("SelectedLanguage", "en")


        try {
            val fileName = when (currentLanguage) {
                "tr" -> "parametreler.csv"
                "en" -> "eng_parametreler.csv"
                "fi" -> "suomea_parametreler.csv"
                else -> throw IllegalArgumentException("Unsupported language: $currentLanguage")
            }

            val inputStream = assets.open(fileName)

            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?


            // Skip the header line
            reader.readLine()

            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(";") // Assuming CSV is comma-separated
                val gender_in_csv = tokens[1]
                if (tokens.size >= 4 && user_gender != gender_in_csv) { // Make sure the line has at least 4 columns
                    val tahlil = tokens[0]
                    val lowerLimit = tokens[2]
                    val upperLimit = tokens[3]
                    val unit = tokens[4]
                    val low_message = tokens[5]
                    val high_message = tokens[6]
                    val refRange = getString(R.string.ReferanceRange, lowerLimit, upperLimit, unit)

                    itemList.add(Item(tahlil, refRange, low_message, high_message))
                }
            }

            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return itemList
    }

    private fun showPopup(messages: List<String>) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_layout, null)
        val popupText = view.findViewById<TextView>(R.id.popup_text)

        val formattedMessages = messages.mapIndexed { index, message -> "${index + 1}- $message" }
        popupText.text = formattedMessages.joinToString("\n")

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // allows the popup to be dismissed when touched outside
        )

        // Show the popup at the center of the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

}

