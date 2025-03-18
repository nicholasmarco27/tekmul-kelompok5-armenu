package com.xperiencelabs.arapp

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isGone
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: ArSceneView
    lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var modelNode: ArModelNode

    // Food model buttons
    private lateinit var hamburgerButton: Button
    private lateinit var ramenButton: Button
    private lateinit var tacoButton: Button
    private lateinit var geprekButton: Button

    // Current selected model
    private var currentModel = "hamburger"

    // Track if model is placed
    private var isModelPlaced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        placeButton = findViewById(R.id.place)
        placeButton.setOnClickListener {
            placeModel()
        }

        // Initialize food model buttons
        hamburgerButton = findViewById(R.id.hamburgerButton)
        ramenButton = findViewById(R.id.ramenButton)
        tacoButton = findViewById(R.id.tacoButton)
        geprekButton = findViewById(R.id.geprekButton)

        // Set click listeners for food model buttons
        hamburgerButton.setOnClickListener { changeModel("hamburger") }
        ramenButton.setOnClickListener { changeModel("ramen") }
        tacoButton.setOnClickListener { changeModel("taco") }
        geprekButton.setOnClickListener { changeModel("geprek") }

        // Initialize model node with default hamburger model
        createModelNode("hamburger")
    }

    private fun createModelNode(modelName: String) {
        // Remove existing model node if it exists
        if (::modelNode.isInitialized) {
            sceneView.removeChild(modelNode)
        }

        // Reset model placed state when creating a new model
        isModelPlaced = false
        placeButton.visibility = View.VISIBLE

        // Create new model node with selected model
        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/$modelName.glb",
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            ) {
                sceneView.planeRenderer.isVisible = true
            }
            // Only update our tracked state, don't directly modify button visibility here
            onAnchorChanged = {
                isModelPlaced = it != null
                // Update button text instead of hiding it
                if (it != null) {
                    placeButton.text = "Placed"
                } else {
                    placeButton.text = "Place"
                }
            }
        }

        // Add model node to scene
        sceneView.addChild(modelNode)
    }

    private fun changeModel(modelName: String) {
        if (currentModel != modelName) {
            currentModel = modelName

            // Reset placement when changing models
            if (modelNode.isAnchored) {
                modelNode.detachAnchor()
                sceneView.planeRenderer.isVisible = true
            }

            // Create new model node with selected model
            createModelNode(modelName)
        }
    }

    private fun placeModel() {
        // Only place if not already placed
        if (!isModelPlaced) {
            modelNode.anchor()
            sceneView.planeRenderer.isVisible = false
            placeButton.text = "Placed"
            isModelPlaced = true
        } else {
            // If already placed, allow repositioning
            modelNode.detachAnchor()
            sceneView.planeRenderer.isVisible = true
            placeButton.text = "Place"
            isModelPlaced = false
        }
    }
}