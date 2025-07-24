package com.example.nimbus

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nimbus.databinding.ActivityMainBinding
import com.google.android.material.color.utilities.ViewingConditions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchWeatherData("Dhaka")
        setupSearchView()
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            // Set focusable properties programmatically
            isFocusable = true
            isIconified = false
            isFocusableInTouchMode = true

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        fetchWeatherData(it)
                        clearFocus()  // Clear focus after submission
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        }
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "622670c7cfd8f5e2ee8bbd7b5bff278f", "metric")
        response.enqueue(object : Callback<Nimbus> {
            override fun onResponse(call: Call<Nimbus>, response: Response<Nimbus>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val main = responseBody.main
                    val sys = responseBody.sys
                    val weather = responseBody.weather.firstOrNull()
                    val wind = responseBody.wind
                    val name = responseBody.name
                    val humidity = main.humidity
                    val temp = main.temp
                    val tempMax = main.temp_max
                    val tempMin = main.temp_min
                    val pressure = main.pressure
                    val seaLevel = main.sea_level ?: "N/A"
                    val sunrise = sys.sunrise
                    val sunset = sys.sunset
                    val windSpeed = wind.speed
                    val condition = weather?.main ?: "N/A"
                    val description = weather?.description ?: "N/A"

                    binding.temp.text = "$temp °C"
                    binding.maxTemp.text = "MAX : $tempMax °C"
                    binding.minTemp.text = "MIN : $tempMin °C"
                    binding.humidity.text = "$humidity %"
                    binding.wind.text = "$windSpeed m/s"
                    binding.condition.text = condition.uppercase()
                    binding.weather.text = description.uppercase()
                    binding.sea.text = "$seaLevel hPa"

                    // Convert sunrise/sunset from unix to HH:mm
                    val sunriseTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date(sunrise * 1000L))
                    val sunsetTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date(sunset * 1000L))
                    binding.sunrise.text = sunriseTime
                    binding.sunset.text = sunsetTime
                    binding.cityName.text = name

                    // Set current date and day
                    val now = System.currentTimeMillis()
                    val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                    binding.date.text = dateFormat.format(now)
                    binding.day.text = dayFormat.format(now)

                    // Use both main and description for weather animation/background
                    val normalizedCondition = (condition + " " + description).lowercase(Locale.getDefault())
                    changeImagesAccordingToWeatherCondition(normalizedCondition)
                }
            }

            override fun onFailure(call: Call<Nimbus>, t: Throwable) {
                binding.temp.text = "--"
                binding.cityName.text = "Error: ${t.message}"
            }
        })
    }

    private fun changeImagesAccordingToWeatherCondition(conditions: String) {
        val cond = conditions.lowercase(Locale.getDefault())
        when {
            cond.contains("rain") || cond.contains("drizzle") || cond.contains("shower") -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            cond.contains("snow") || cond.contains("blizzard") -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            cond.contains("cloud") || cond.contains("overcast") || cond.contains("mist") || cond.contains("fog") -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            cond.contains("clear") || cond.contains("sunny") -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

}