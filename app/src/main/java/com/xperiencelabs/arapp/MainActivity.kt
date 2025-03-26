package com.xperiencelabs.arapp

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
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
    private lateinit var mediaPlayer: MediaPlayer

    // Food model buttons
    private lateinit var hamburgerButton: Button
    private lateinit var ramenButton: Button
    private lateinit var tacoButton: Button
    private lateinit var geprekButton: Button

    // Current selected model
    private var currentModel = "burger"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        mediaPlayer = MediaPlayer.create(this,R.raw.ad)

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
        hamburgerButton.setOnClickListener { changeModel("burger") }
        ramenButton.setOnClickListener { changeModel("ramen") }
        tacoButton.setOnClickListener { changeModel("taco") }
        geprekButton.setOnClickListener { changeModel("geprek") }

        // Initialize model node with default hamburger model
        createModelNode("burger")
    }

    private fun createModelNode(modelName: String) {
        // Remove existing model node if it exists
        if (::modelNode.isInitialized) {
            sceneView.removeChild(modelNode)
        }

        // Show place button when creating a new model
        placeButton.visibility = View.VISIBLE
        placeButton.text = "Place"

        // Create new model node with selected model
        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/$modelName.glb",
                scaleToUnits = 0.2f,
            ) {
                sceneView.planeRenderer.isVisible = true
            }
            onAnchorChanged = {
                if (it != null) {
                    // Model is anchored (placed)
                    placeButton.text = "Placed"
                    placeButton.visibility = View.GONE
                    sceneView.planeRenderer.isVisible = false
                }
            }
        }

        // Add model node to scene
        sceneView.addChild(modelNode)
    }

    private fun changeModel(modelName: String) {
        if (currentModel != modelName) {
            currentModel = modelName
            createModelNode(modelName)
        }
    }

    private fun placeModel() {
        if (!modelNode.isAnchored) {
            modelNode.anchor()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}