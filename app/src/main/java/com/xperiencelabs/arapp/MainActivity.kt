package com.xperiencelabs.arapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: ArSceneView
    private lateinit var placeButton: FloatingActionButton
    private lateinit var takePictureButton: FloatingActionButton
    private lateinit var modelNode: ArModelNode
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var instructionCard: CardView
    private lateinit var surfaceIndicator: ImageView
    private lateinit var adjustmentControls: CardView
    private lateinit var backButton: ImageButton
    private lateinit var arTitleText: TextView
    private lateinit var scaleLabel: TextView

    // Animation for surface indicator
    private lateinit var pulseAnimation: AlphaAnimation

    // Model adjustment variables - INCREASED DEFAULT SCALE
    private var currentScale = 1.0f  // Increased from 0.2f for better visibility

    // Current selected model
    private var currentModel = "burger"
    private var modelPlaced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        sceneView = findViewById(R.id.sceneView)
        placeButton = findViewById(R.id.place)
        takePictureButton = findViewById(R.id.takePicture)
        instructionCard = findViewById(R.id.instructionCard)
        surfaceIndicator = findViewById(R.id.surfaceIndicator)
        adjustmentControls = findViewById(R.id.adjustmentControls)
        backButton = findViewById(R.id.backButton)
        arTitleText = findViewById(R.id.arTitleText)
        scaleLabel = findViewById(R.id.scaleLabel)

        // Set up SceneView
        sceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED

        // Hide adjustment controls initially
        adjustmentControls.visibility = View.GONE

        // Hide take picture button initially
        takePictureButton.visibility = View.GONE

        // Update scale label
        updateScaleLabel()

        try {
            // Set up media player - in a try/catch to avoid crashes if sound file is missing
            mediaPlayer = MediaPlayer.create(this, R.raw.ad)
        } catch (e: Exception) {
            // Just continue without sound if there's an issue
        }

        // Set up pulse animation for surface indicator
        setupPulseAnimation()

        // Set up click listeners
        setupClickListeners()

        // Get selected food model from intent
        val foodModel = intent.getStringExtra("FOOD_MODEL") ?: "burger"
        currentModel = foodModel

        // Set the title based on the model
        updateFoodTitle(foodModel)

        // Initialize model
        createModelNode(currentModel)

        // Auto-hide instruction card after delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (!modelPlaced) {
                instructionCard.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction {
                        instructionCard.visibility = View.GONE
                    }
                    .start()
            }
        }, 5000)
    }

    private fun setupPulseAnimation() {
        pulseAnimation = AlphaAnimation(0.3f, 0.9f)
        pulseAnimation.duration = 1000
        pulseAnimation.repeatMode = Animation.REVERSE
        pulseAnimation.repeatCount = Animation.INFINITE
        surfaceIndicator.startAnimation(pulseAnimation)
    }

    private fun setupClickListeners() {
        // Back button
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Place button
        placeButton.setOnClickListener {
            if (!modelPlaced) {
                placeModel()
            } else {
                // If already placed, toggle adjustment controls
                if (adjustmentControls.visibility == View.VISIBLE) {
                    adjustmentControls.animate()
                        .alpha(0f)
                        .translationX(-100f)
                        .setDuration(200)
                        .withEndAction {
                            adjustmentControls.visibility = View.GONE
                            adjustmentControls.alpha = 1f
                            adjustmentControls.translationX = 0f
                        }
                        .start()
                } else {
                    adjustmentControls.alpha = 0f
                    adjustmentControls.translationX = -50f
                    adjustmentControls.visibility = View.VISIBLE
                    adjustmentControls.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(200)
                        .start()
                }
            }
        }

        // Take picture button
        takePictureButton.setOnClickListener {
            Toast.makeText(this, "Capture feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Adjustment buttons
        findViewById<MaterialButton>(R.id.resetButton).setOnClickListener {
            resetModel()
        }

        findViewById<FloatingActionButton>(R.id.scaleUpButton).setOnClickListener {
            scaleModel(1.25f)
        }

        findViewById<FloatingActionButton>(R.id.scaleDownButton).setOnClickListener {
            scaleModel(0.8f)
        }
    }

    private fun updateScaleLabel() {
        // Format the scale as a percentage
        val scalePercent = (currentScale * 100).toInt()
        scaleLabel.text = "$scalePercent%"
    }

    private fun updateFoodTitle(modelName: String) {
        val foodName = when(modelName) {
            "burger" -> "Hamburger"
            "ramen" -> "Ramen"
            "taco" -> "Taco"
            "geprek" -> "Ayam Geprek"
            else -> "Food AR View"
        }
        arTitleText.text = foodName
    }

    private fun createModelNode(modelName: String) {
        // Remove existing model node if it exists
        if (::modelNode.isInitialized) {
            sceneView.removeChild(modelNode)
        }

        modelPlaced = false

        // Reset UI elements
        placeButton.visibility = View.VISIBLE
        placeButton.setImageResource(android.R.drawable.ic_input_add)
        takePictureButton.visibility = View.GONE
        adjustmentControls.visibility = View.GONE
        surfaceIndicator.visibility = View.VISIBLE
        surfaceIndicator.startAnimation(pulseAnimation)

        // Create new model node with selected model
        modelNode = ArModelNode(sceneView.engine, PlacementMode.BEST_AVAILABLE).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/$modelName.glb",
                scaleToUnits = currentScale,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            ) {
                // Model loaded successfully
            }

            // Use onAnchorChanged for placement detection
            onAnchorChanged = {
                if (it != null && !modelPlaced) {
                    // Model is anchored (placed) for the first time
                    onModelPlaced()
                }
            }
        }

        // Add model node to scene
        sceneView.addChild(modelNode)
    }

    private fun onModelPlaced() {
        modelPlaced = true

        // Hide surface indicator and instruction card
        surfaceIndicator.clearAnimation()
        surfaceIndicator.visibility = View.GONE
        instructionCard.visibility = View.GONE

        // Change place button icon and show additional controls
        placeButton.setImageResource(android.R.drawable.ic_menu_edit)
        takePictureButton.visibility = View.VISIBLE

        // Show a success toast
        Toast.makeText(this, "Food placed! Tap edit to adjust", Toast.LENGTH_SHORT).show()
    }

    private fun placeModel() {
        if (::modelNode.isInitialized && !modelNode.isAnchored) {
            modelNode.anchor()
        }
    }

    private fun resetModel() {
        if (::modelNode.isInitialized) {
            modelNode.rotation = Rotation(x = 0f, y = 0f, z = 0f)
            currentScale = 1.0f  // Reset to larger default scale
            modelNode.scale = Scale(currentScale, currentScale, currentScale)
            updateScaleLabel()

            // Show success message
            Toast.makeText(this, "Model reset", Toast.LENGTH_SHORT).show()

            // Hide adjustment controls with animation
            adjustmentControls.animate()
                .alpha(0f)
                .translationX(-100f)
                .setDuration(200)
                .withEndAction {
                    adjustmentControls.visibility = View.GONE
                    adjustmentControls.alpha = 1f
                    adjustmentControls.translationX = 0f
                }
                .start()
        }
    }

    private fun scaleModel(factor: Float) {
        if (::modelNode.isInitialized) {
            currentScale *= factor
            // Limit scale to reasonable bounds (increased min/max)
            currentScale = currentScale.coerceIn(0.2f, 2.0f)
            modelNode.scale = Scale(currentScale, currentScale, currentScale)

            // Update the scale label
            updateScaleLabel()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mediaPlayer.isInitialized) {
            try {
                mediaPlayer.pause()
            } catch (e: Exception) {
                // Ignore any media player errors
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            try {
                mediaPlayer.release()
            } catch (e: Exception) {
                // Ignore any media player errors
            }
        }
    }
}