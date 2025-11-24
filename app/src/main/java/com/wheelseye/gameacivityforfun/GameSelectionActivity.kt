package com.wheelseye.gameacivityforfun

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class GameSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_selection)

        findViewById<MaterialButton>(R.id.playWithRobotButton).setOnClickListener {
            val intent = Intent(this, ChessGameActivity::class.java)
            intent.putExtra(ChessGameActivity.EXTRA_GAME_MODE, ChessGameActivity.GAME_MODE_ROBOT)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.playWithNearbyButton).setOnClickListener {
            val intent = Intent(this, ChessGameActivity::class.java)
            intent.putExtra(ChessGameActivity.EXTRA_GAME_MODE, ChessGameActivity.GAME_MODE_NEARBY)
            startActivity(intent)
        }
    }
}

