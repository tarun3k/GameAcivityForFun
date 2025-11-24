package com.wheelseye.gameacivityforfun

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChessGameActivity : AppCompatActivity() {


    private lateinit var chessGame: ChessGame
    private lateinit var chessAI: ChessAI
    private lateinit var boardGridView: GridView
    private lateinit var statusTextView: TextView
    private lateinit var connectionStatusTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var adapter: ChessSquareAdapter

    private var selectedPosition: Position? = null
    private var gameMode: String = GAME_MODE_ROBOT
    private var playerColor: PieceColor = PieceColor.WHITE
    private var isMyTurn: Boolean = true

    // Nearby Connections
    private var connectionsClient: ConnectionsClient? = null
    private var opponentEndpointId: String? = null
    private var isHost: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_game)

        gameMode = intent.getStringExtra(EXTRA_GAME_MODE) ?: GAME_MODE_ROBOT

        initializeViews()
        initializeGame()

        if (gameMode == GAME_MODE_NEARBY) {
            requestPermissions()
        } else {
            startRobotGame()
        }
    }

    private fun initializeViews() {
        boardGridView = findViewById(R.id.chessBoardGridView)
        statusTextView = findViewById(R.id.statusTextView)
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        // GridView click listener as backup (adapter also handles clicks)
        boardGridView.setOnItemClickListener { _, _, position, _ ->
            val row = position / 8
            val col = position % 8
            onSquareClick(Position(row, col))
        }

        findViewById<MaterialButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.newGameButton).setOnClickListener {
            resetGame()
        }
    }

    private fun initializeGame() {
        chessGame = ChessGame()
        if (gameMode == GAME_MODE_ROBOT) {
            chessAI = ChessAI(chessGame, PieceColor.BLACK)
        }
        updateBoard()
        updateStatus()
    }

    private fun startRobotGame() {
        playerColor = PieceColor.WHITE
        isMyTurn = true
        updateStatus()
    }

    private fun updateBoard() {
        val board = chessGame.getBoard()
        val validMoves = selectedPosition?.let { chessGame.getValidMoves(it) } ?: emptyList()
        
        adapter = ChessSquareAdapter(board, selectedPosition, validMoves) { position ->
            onSquareClick(position)
        }
        boardGridView.adapter = adapter
    }

    private fun onSquareClick(position: Position) {
        // Debug log
        android.util.Log.d("ChessGame", "Square clicked: row=${position.row}, col=${position.col}")
        
        if (!isMyTurn || chessGame.isGameOver()) {
            android.util.Log.d("ChessGame", "Click ignored: isMyTurn=$isMyTurn, gameOver=${chessGame.isGameOver()}")
            return
        }

        val piece = chessGame.getBoard()[position.row][position.col]

        if (selectedPosition == null) {
            // Select a piece
            if (piece != null && piece.color == playerColor) {
                selectedPosition = position
                updateBoard()
            }
        } else {
            // Try to move
            if (position == selectedPosition) {
                // Deselect
                selectedPosition = null
                updateBoard()
            } else {
                val move = Move(selectedPosition!!, position)
                if (chessGame.makeMove(move)) {
                    selectedPosition = null
                    updateBoard()
                    updateStatus()

                    if (chessGame.isGameOver()) {
                        handleGameEnd()
                    } else if (gameMode == GAME_MODE_ROBOT) {
                        // Robot's turn
                        isMyTurn = false
                        updateStatus()
                        makeRobotMove()
                    } else if (gameMode == GAME_MODE_NEARBY) {
                        // Send move to opponent
                        sendMoveToOpponent(move)
                        isMyTurn = false
                        updateStatus()
                    }
                } else {
                    // Invalid move, try selecting new piece
                    if (piece != null && piece.color == playerColor) {
                        selectedPosition = position
                        updateBoard()
                    } else {
                        selectedPosition = null
                        updateBoard()
                    }
                }
            }
        }
    }

    private fun makeRobotMove() {
        lifecycleScope.launch {
            delay(500) // Small delay for better UX
            val move = chessAI.getBestMove()
            if (move != null && chessGame.makeMove(move)) {
                updateBoard()
                updateStatus()

                if (chessGame.isGameOver()) {
                    handleGameEnd()
                } else {
                    isMyTurn = true
                    updateStatus()
                }
            }
        }
    }

    private fun updateStatus() {
        val currentPlayer = chessGame.getCurrentPlayer()
        val gameState = chessGame.getGameState()

        val statusText = when {
            chessGame.isGameOver() -> gameState
            gameMode == GAME_MODE_NEARBY && !isMyTurn -> getString(R.string.opponent_turn)
            gameMode == GAME_MODE_NEARBY && isMyTurn -> getString(R.string.your_turn)
            gameMode == GAME_MODE_ROBOT && !isMyTurn -> "Robot's Turn"
            else -> getString(R.string.your_turn)
        }

        statusTextView.text = statusText

        if (gameState.isNotEmpty() && !chessGame.isGameOver()) {
            Toast.makeText(this, gameState, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGameEnd() {
        val winner = chessGame.getWinner()
        val result = when {
            chessGame.isDraw() -> "draw"
            winner == playerColor -> "win"
            else -> "loss"
        }

        val opponentType = if (gameMode == GAME_MODE_ROBOT) "robot" else "human"

        // Update score to API
        updateScoreToApi(result, opponentType)

        // Show game over dialog
        val message = when {
            chessGame.isDraw() -> getString(R.string.draw)
            winner == playerColor -> getString(R.string.you_won)
            else -> getString(R.string.you_lost)
        }

        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage(message)
            .setPositiveButton("New Game") { _, _ -> resetGame() }
            .setNegativeButton("Back") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun resetGame() {
        chessGame = ChessGame()
        if (gameMode == GAME_MODE_ROBOT) {
            chessAI = ChessAI(chessGame, PieceColor.BLACK)
        }
        selectedPosition = null
        isMyTurn = true
        playerColor = PieceColor.WHITE
        updateBoard()
        updateStatus()
    }

    // Nearby Connections Implementation
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            initializeNearbyConnections()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeNearbyConnections()
            } else {
                Toast.makeText(this, "Permissions required for nearby play", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun initializeNearbyConnections() {
        connectionsClient = Nearby.getConnectionsClient(this)
        connectionStatusTextView.visibility = TextView.VISIBLE
        loadingProgressBar.visibility = ProgressBar.VISIBLE

        // Start both advertising and discovering
        startAdvertising()
        startDiscovering()
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_STAR)
            .build()

        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                opponentEndpointId = endpointId
                connectionsClient?.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        connectionStatusTextView.text = getString(R.string.connected)
                        loadingProgressBar.visibility = ProgressBar.GONE
                        isHost = true
                        playerColor = PieceColor.WHITE
                        isMyTurn = true
                        updateStatus()
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        connectionStatusTextView.text = "Connection rejected"
                    }
                    else -> {
                        connectionStatusTextView.text = "Connection failed"
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                connectionStatusTextView.text = getString(R.string.disconnected)
                opponentEndpointId = null
            }
        }

        val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // Endpoint found
            }

            override fun onEndpointLost(endpointId: String) {
                // Endpoint lost
            }
        }

        connectionsClient?.startAdvertising(
            "Chess Player",
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        )?.addOnSuccessListener {
            connectionStatusTextView.text = getString(R.string.advertising)
        }?.addOnFailureListener {
            connectionStatusTextView.text = "Failed to start advertising: ${it.message}"
        }
    }

    private fun startDiscovering() {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_STAR)
            .build()
        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                opponentEndpointId = endpointId
                connectionsClient?.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        connectionStatusTextView.text = getString(R.string.connected)
                        loadingProgressBar.visibility = ProgressBar.GONE
                        isHost = false
                        playerColor = PieceColor.BLACK
                        isMyTurn = false
                        updateStatus()
                    }
                    else -> {
                        connectionStatusTextView.text = "Connection failed"
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                connectionStatusTextView.text = getString(R.string.disconnected)
                opponentEndpointId = null
            }
        }
        val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                connectionsClient?.requestConnection(
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
                    endpointId,
                    connectionLifecycleCallback
                )
            }

            override fun onEndpointLost(endpointId: String) {
                // Endpoint lost
            }
        }



        connectionsClient?.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        )?.addOnSuccessListener {
            connectionStatusTextView.text = getString(R.string.searching_nearby)
        }?.addOnFailureListener {
            // Discovery failed, but advertising might still work
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val data = String(payload.asBytes()!!)
                    handleReceivedMove(data)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Handle transfer updates if needed
        }
    }

    private fun sendMoveToOpponent(move: Move) {
        opponentEndpointId?.let { endpointId ->
            val json = JSONObject().apply {
                put("fromRow", move.from.row)
                put("fromCol", move.from.col)
                put("toRow", move.to.row)
                put("toCol", move.to.col)
            }
            val bytes = json.toString().toByteArray()
            connectionsClient?.sendPayload(endpointId, Payload.fromBytes(bytes))
        }
    }

    private fun handleReceivedMove(data: String) {
        try {
            val json = JSONObject(data)
            val move = Move(
                Position(json.getInt("fromRow"), json.getInt("fromCol")),
                Position(json.getInt("toRow"), json.getInt("toCol"))
            )
            if (chessGame.makeMove(move)) {
                updateBoard()
                updateStatus()
                isMyTurn = true

                if (chessGame.isGameOver()) {
                    handleGameEnd()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateScoreToApi(result: String, opponentType: String) {
        lifecycleScope.launch {
            try {
                val playerId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val request = ScoreRequest(
                    playerId = playerId,
                    gameType = "chess",
                    result = result,
                    opponentType = opponentType
                )
                
                // Use mock API for now
                val response = ScoreApiService.updateScoreMock(request)
                Toast.makeText(
                    this@ChessGameActivity,
                    "Score updated: ${response.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                // Silently fail - game still works without API
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionsClient?.stopAllEndpoints()
        connectionsClient?.stopAdvertising()
        connectionsClient?.stopDiscovery()
    }

    companion object {
        private const val SERVICE_ID = "com.wheelseye.gameacivityforfun.CHESS"
        const val EXTRA_GAME_MODE = "game_mode"
        const val GAME_MODE_ROBOT = "robot"
        const val GAME_MODE_NEARBY = "nearby"
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }
}

