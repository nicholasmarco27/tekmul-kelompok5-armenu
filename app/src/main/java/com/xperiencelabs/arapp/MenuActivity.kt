package com.xperiencelabs.arapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar

class MenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter
    private val foodList = mutableListOf<Food>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.foodRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Prepare food data
        prepareFoodData()

        // Initialize and set adapter
        foodAdapter = FoodAdapter(foodList)
        recyclerView.adapter = foodAdapter
    }

    private fun prepareFoodData() {
        // Add food items with descriptions
        foodList.add(
            Food(
                "burger",
                "Hamburger",
                "A delicious burger with all the fixings - beef patty, lettuce, tomato, and special sauce.",
                R.drawable.burger_image
            )
        )
        foodList.add(
            Food(
                "ramen",
                "Ramen",
                "Authentic Japanese ramen with rich broth, noodles, soft-boiled egg, and char siu pork.",
                R.drawable.ramen_image
            )
        )
        foodList.add(
            Food(
                "taco",
                "Taco",
                "Traditional Mexican taco with seasoned meat, fresh vegetables, and zesty salsa.",
                R.drawable.taco_image
            )
        )
        foodList.add(
            Food(
                "geprek",
                "Ayam Geprek",
                "Indonesian crispy smashed chicken with spicy sambal and rice.",
                R.drawable.geprek_image
            )
        )
    }
}