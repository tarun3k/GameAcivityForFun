package com.wheelseye.gameacivityforfun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class ChessSquareAdapter(
    private val board: Array<Array<Piece?>>,
    private val selectedPosition: Position?,
    private val validMoves: List<Position>,
    private val onSquareClick: (Position) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = 64

    override fun getItem(position: Int): Any = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = position / 8
        val col = position % 8
        val pos = Position(row, col)
        val isLight = (row + col) % 2 == 0

        val view = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_chess_square, parent, false)

        val cardView = view.findViewById<MaterialCardView>(R.id.squareCardView)
        val pieceTextView = view.findViewById<TextView>(R.id.pieceTextView)

        // Set background color
        val backgroundColor = if (isLight) {
            android.graphics.Color.parseColor("#F0D9B5")
        } else {
            android.graphics.Color.parseColor("#B58863")
        }

        // Highlight selected square
        val finalColor = when {
            selectedPosition?.row == row && selectedPosition?.col == col -> 
                android.graphics.Color.parseColor("#FFFF00")
            validMoves.contains(pos) -> 
                android.graphics.Color.parseColor("#90EE90")
            else -> backgroundColor
        }

        cardView.setCardBackgroundColor(finalColor)

        // Set piece symbol - using Unicode chess symbols
        val piece = board[row][col]
        if (piece != null) {
            val pieceSymbol = getPieceSymbol(piece)
            pieceTextView.text = pieceSymbol
            pieceTextView.setTextColor(if (piece.color == PieceColor.WHITE) 
                android.graphics.Color.parseColor("#FFFFFF") 
            else 
                android.graphics.Color.parseColor("#000000"))
            pieceTextView.visibility = View.VISIBLE
            pieceTextView.contentDescription = "${piece.color} ${piece.type}"
        } else {
            pieceTextView.text = ""
            pieceTextView.visibility = View.VISIBLE
        }

        // Set click listener on the root view
        view.setOnClickListener {
            onSquareClick(pos)
        }

        return view
    }

    private fun getPieceSymbol(piece: Piece): String {
        return when (piece.type) {
            PieceType.PAWN -> if (piece.color == PieceColor.WHITE) "♙" else "♟"
            PieceType.ROOK -> if (piece.color == PieceColor.WHITE) "♖" else "♜"
            PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) "♘" else "♞"
            PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) "♗" else "♝"
            PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) "♕" else "♛"
            PieceType.KING -> if (piece.color == PieceColor.WHITE) "♔" else "♚"
        }
    }
}

