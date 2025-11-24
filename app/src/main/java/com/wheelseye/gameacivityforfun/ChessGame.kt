package com.wheelseye.gameacivityforfun

data class Position(val row: Int, val col: Int)

enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

enum class PieceColor {
    WHITE, BLACK
}

data class Piece(val type: PieceType, val color: PieceColor)

data class Move(val from: Position, val to: Position, val promotion: PieceType? = null)

class ChessGame {
    private val board = Array(8) { Array<Piece?>(8) { null } }
    private var currentPlayer = PieceColor.WHITE
    private var gameOver = false
    private var winner: PieceColor? = null
    private var isCheck = false
    private var isCheckmate = false
    private var isDraw = false

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        // Place pawns
        for (col in 0 until 8) {
            board[1][col] = Piece(PieceType.PAWN, PieceColor.BLACK)
            board[6][col] = Piece(PieceType.PAWN, PieceColor.WHITE)
        }

        // Place other pieces
        val backRow = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
            PieceType.QUEEN, PieceType.KING, PieceType.BISHOP,
            PieceType.KNIGHT, PieceType.ROOK
        )

        for (col in 0 until 8) {
            board[0][col] = Piece(backRow[col], PieceColor.BLACK)
            board[7][col] = Piece(backRow[col], PieceColor.WHITE)
        }
    }

    fun getBoard(): Array<Array<Piece?>> = board.map { it.clone() }.toTypedArray()

    fun getCurrentPlayer(): PieceColor = currentPlayer

    fun isGameOver(): Boolean = gameOver

    fun getWinner(): PieceColor? = winner

    fun isInCheck(): Boolean = isCheck

    fun isInCheckmate(): Boolean = isCheckmate

    fun isDraw(): Boolean = isDraw

    fun getValidMoves(position: Position): List<Position> {
        val piece = board[position.row][position.col] ?: return emptyList()
        if (piece.color != currentPlayer) return emptyList()

        val validMoves = mutableListOf<Position>()
        val allMoves = getAllPossibleMoves(position)

        for (move in allMoves) {
            if (isValidMove(Move(position, move))) {
                validMoves.add(move)
            }
        }

        return validMoves
    }

    private fun getAllPossibleMoves(position: Position): List<Position> {
        val piece = board[position.row][position.col] ?: return emptyList()
        val moves = mutableListOf<Position>()

        when (piece.type) {
            PieceType.PAWN -> moves.addAll(getPawnMoves(position, piece.color))
            PieceType.ROOK -> moves.addAll(getRookMoves(position, piece.color))
            PieceType.KNIGHT -> moves.addAll(getKnightMoves(position, piece.color))
            PieceType.BISHOP -> moves.addAll(getBishopMoves(position, piece.color))
            PieceType.QUEEN -> {
                moves.addAll(getRookMoves(position, piece.color))
                moves.addAll(getBishopMoves(position, piece.color))
            }
            PieceType.KING -> moves.addAll(getKingMoves(position, piece.color))
        }

        return moves
    }

    private fun getPawnMoves(position: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        val direction = if (color == PieceColor.WHITE) -1 else 1
        val startRow = if (color == PieceColor.WHITE) 6 else 1

        // Rule 1: Move forward one square (only if square is empty)
        val oneForward = Position(position.row + direction, position.col)
        if (isValidPosition(oneForward) && board[oneForward.row][oneForward.col] == null) {
            moves.add(oneForward)

            // Rule 2: Move forward two squares from starting position (only if both squares are empty)
            if (position.row == startRow) {
                val twoForward = Position(position.row + 2 * direction, position.col)
                if (isValidPosition(twoForward) && board[twoForward.row][twoForward.col] == null) {
                    moves.add(twoForward)
                }
            }
        }

        // Rule 3: Capture diagonally (only enemy pieces, cannot capture forward or same color)
        for (colOffset in listOf(-1, 1)) {
            val capturePos = Position(position.row + direction, position.col + colOffset)
            if (isValidPosition(capturePos)) {
                val targetPiece = board[capturePos.row][capturePos.col]
                // Can only capture enemy pieces diagonally, not same color
                if (targetPiece != null && targetPiece.color != color) {
                    moves.add(capturePos)
                }
            }
        }

        return moves
    }

    private fun getRookMoves(position: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        // Rook moves horizontally and vertically
        val directions = listOf(Position(-1, 0), Position(1, 0), Position(0, -1), Position(0, 1))

        for (dir in directions) {
            var newRow = position.row + dir.row
            var newCol = position.col + dir.col

            // Can move any number of squares in one direction until blocked
            while (isValidPosition(Position(newRow, newCol))) {
                val targetPiece = board[newRow][newCol]
                if (targetPiece == null) {
                    // Empty square - can move here
                    moves.add(Position(newRow, newCol))
                } else {
                    // Square occupied
                    if (targetPiece.color != color) {
                        // Enemy piece - can capture, then stop
                        moves.add(Position(newRow, newCol))
                    }
                    // Same color piece - cannot move here or beyond, stop
                    break
                }
                newRow += dir.row
                newCol += dir.col
            }
        }

        return moves
    }

    private fun getKnightMoves(position: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        // Knight moves in L-shape: 2 squares one direction, 1 square perpendicular
        // Can jump over pieces, but cannot land on same color
        val knightMoves = listOf(
            Position(-2, -1), Position(-2, 1), Position(-1, -2), Position(-1, 2),
            Position(1, -2), Position(1, 2), Position(2, -1), Position(2, 1)
        )

        for (move in knightMoves) {
            val newPos = Position(position.row + move.row, position.col + move.col)
            if (isValidPosition(newPos)) {
                val targetPiece = board[newPos.row][newPos.col]
                // Can move to empty square or capture enemy piece, but not same color
                if (targetPiece == null || targetPiece.color != color) {
                    moves.add(newPos)
                }
            }
        }

        return moves
    }

    private fun getBishopMoves(position: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        // Bishop moves diagonally
        val directions = listOf(Position(-1, -1), Position(-1, 1), Position(1, -1), Position(1, 1))

        for (dir in directions) {
            var newRow = position.row + dir.row
            var newCol = position.col + dir.col

            // Can move any number of squares diagonally until blocked
            while (isValidPosition(Position(newRow, newCol))) {
                val targetPiece = board[newRow][newCol]
                if (targetPiece == null) {
                    // Empty square - can move here
                    moves.add(Position(newRow, newCol))
                } else {
                    // Square occupied
                    if (targetPiece.color != color) {
                        // Enemy piece - can capture, then stop
                        moves.add(Position(newRow, newCol))
                    }
                    // Same color piece - cannot move here or beyond, stop
                    break
                }
                newRow += dir.row
                newCol += dir.col
            }
        }

        return moves
    }

    private fun getKingMoves(position: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        // King moves one square in any direction (including diagonally)
        val kingMoves = listOf(
            Position(-1, -1), Position(-1, 0), Position(-1, 1),
            Position(0, -1), Position(0, 1),
            Position(1, -1), Position(1, 0), Position(1, 1)
        )

        for (move in kingMoves) {
            val newPos = Position(position.row + move.row, position.col + move.col)
            if (isValidPosition(newPos)) {
                val targetPiece = board[newPos.row][newPos.col]
                // Can move to empty square or capture enemy piece, but not same color
                // Note: Moving into check is prevented by isValidMove()
                if (targetPiece == null || targetPiece.color != color) {
                    moves.add(newPos)
                }
            }
        }

        return moves
    }

    fun isValidMove(move: Move): Boolean {
        if (!isValidPosition(move.from) || !isValidPosition(move.to)) return false

        val piece = board[move.from.row][move.from.col] ?: return false
        if (piece.color != currentPlayer) return false

        // Check if destination has a piece of the same color (cannot capture own pieces)
        val targetPiece = board[move.to.row][move.to.col]
        if (targetPiece != null && targetPiece.color == piece.color) {
            return false
        }

        // Check if the move is in the list of possible moves for this piece
        val possibleMoves = getAllPossibleMoves(move.from)
        if (!possibleMoves.contains(move.to)) {
            return false
        }

        // Make the move temporarily
        val originalPiece = board[move.to.row][move.to.col]
        board[move.to.row][move.to.col] = piece
        board[move.from.row][move.from.col] = null

        // Check if this move puts own king in check
        val kingPosition = findKing(currentPlayer)
        val inCheck = isPositionUnderAttack(kingPosition, if (currentPlayer == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE)

        // Undo the move
        board[move.from.row][move.from.col] = piece
        board[move.to.row][move.to.col] = originalPiece

        return !inCheck
    }

    fun makeMove(move: Move): Boolean {
        if (gameOver || !isValidMove(move)) return false

        val piece = board[move.from.row][move.from.col]!!
        board[move.to.row][move.to.col] = piece
        board[move.from.row][move.from.col] = null

        // Handle pawn promotion
        if (piece.type == PieceType.PAWN) {
            if ((piece.color == PieceColor.WHITE && move.to.row == 0) ||
                (piece.color == PieceColor.BLACK && move.to.row == 7)) {
                board[move.to.row][move.to.col] = Piece(move.promotion ?: PieceType.QUEEN, piece.color)
            }
        }

        // Switch player
        currentPlayer = if (currentPlayer == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE

        // Check game state
        updateGameState()

        return true
    }

    private fun updateGameState() {
        val opponentColor = if (currentPlayer == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        val kingPosition = findKing(currentPlayer)
        isCheck = isPositionUnderAttack(kingPosition, opponentColor)

        // Check for checkmate
        val hasValidMoves = hasAnyValidMoves(currentPlayer)
        if (isCheck && !hasValidMoves) {
            isCheckmate = true
            gameOver = true
            winner = opponentColor
        } else if (!hasValidMoves) {
            isDraw = true
            gameOver = true
        }
    }

    private fun hasAnyValidMoves(color: PieceColor): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val validMoves = getValidMoves(Position(row, col))
                    if (validMoves.isNotEmpty()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun findKing(color: PieceColor): Position {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return Position(row, col)
                }
            }
        }
        throw IllegalStateException("King not found")
    }

    private fun isPositionUnderAttack(position: Position, byColor: PieceColor): Boolean {
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val piece = board[row][col]
                if (piece != null && piece.color == byColor) {
                    val possibleMoves = getAllPossibleMoves(Position(row, col))
                    if (possibleMoves.contains(position)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isValidPosition(position: Position): Boolean {
        return position.row in 0..7 && position.col in 0..7
    }

    fun getGameState(): String {
        return when {
            isCheckmate -> if (winner == PieceColor.WHITE) "White wins!" else "Black wins!"
            isDraw -> "Draw!"
            isCheck -> "Check!"
            else -> ""
        }
    }
}

