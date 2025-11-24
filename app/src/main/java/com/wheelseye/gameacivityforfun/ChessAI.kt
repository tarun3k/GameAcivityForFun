package com.wheelseye.gameacivityforfun

import kotlin.random.Random

class ChessAI(private val game: ChessGame, private val aiColor: PieceColor) {
    
    fun getBestMove(): Move? {
        val allMoves = getAllPossibleMoves()
        if (allMoves.isEmpty()) return null
        
        // Simple AI: Try to capture pieces, otherwise make a random valid move
        val captureMoves = allMoves.filter { move ->
            val targetPiece = game.getBoard()[move.to.row][move.to.col]
            targetPiece != null && targetPiece.color != aiColor
        }
        
        return if (captureMoves.isNotEmpty()) {
            captureMoves.random()
        } else {
            allMoves.random()
        }
    }
    
    private fun getAllPossibleMoves(): List<Move> {
        val moves = mutableListOf<Move>()
        val board = game.getBoard()
        
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == aiColor) {
                    val validMoves = game.getValidMoves(Position(row, col))
                    for (to in validMoves) {
                        moves.add(Move(Position(row, col), to))
                    }
                }
            }
        }
        
        return moves
    }
}

