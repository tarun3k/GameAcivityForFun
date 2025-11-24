# GameActivityForFun - Chess Game

A fully-featured chess game for Android with multiple gameplay modes, built with Kotlin and Android SDK.

## 🎮 Features

- **Complete Chess Implementation**: Full chess rules with all piece types (Pawn, Rook, Knight, Bishop, Queen, King)
- **Play with Robot**: Challenge yourself against an AI opponent
- **Nearby Multiplayer**: Play with friends using Google Nearby Connections API
- **Score Tracking**: Automatic score updates to API after each game
- **Modern UI**: Clean, intuitive interface with visual feedback for valid moves
- **Game State Management**: Check, checkmate, and draw detection

## 📱 Game Modes

### 1. Play with Robot
- Single-player mode against AI opponent
- AI makes strategic moves (prioritizes captures)
- Perfect for practice and learning

### 2. Play with Nearby Person
- Multiplayer mode using Google Nearby Connections
- Automatic device discovery and connection
- Real-time move synchronization
- Works over WiFi and Bluetooth

## 🎯 How to Play

1. **Launch the app** and select "Chess" from the game selection screen
2. **Choose your mode**: Play with Robot or Play with Nearby Person
3. **Make moves**:
   - Tap a piece to select it (highlighted in yellow)
   - Valid moves are highlighted in green
   - Tap a valid destination to move
   - Tap the same piece again to deselect
4. **Game rules**:
   - White moves first
   - You cannot capture your own pieces
   - King cannot move into check
   - Pawns promote to Queen when reaching the opposite end

## 📋 Chess Piece Movement Rules

### Pawn (♙/♟)
- Moves forward 1 square (only if empty)
- Can move 2 squares forward from starting position
- Captures diagonally (only enemy pieces)

### Rook (♖/♜)
- Moves horizontally or vertically any number of squares
- Cannot jump over pieces
- Stops when blocked

### Knight (♘/♞)
- Moves in L-shape (2 squares one direction, 1 square perpendicular)
- Can jump over pieces
- Unique movement pattern

### Bishop (♗/♝)
- Moves diagonally any number of squares
- Cannot jump over pieces
- Stops when blocked

### Queen (♕/♛)
- Combines Rook and Bishop movements
- Most powerful piece
- Can move in any direction

### King (♔/♚)
- Moves one square in any direction
- Cannot move into check
- Protected by check detection

## 🛠️ Technical Details

### Architecture
- **Language**: Kotlin
- **UI**: Material Design Components
- **Architecture**: Activity-based with custom game logic
- **Networking**: Retrofit for API calls, Nearby Connections for multiplayer

### Dependencies
- AndroidX Core KTX
- Material Design Components
- Google Play Services (Nearby Connections)
- Retrofit & OkHttp (for API integration)
- Kotlin Coroutines

### Permissions
- `INTERNET` - For API calls
- `ACCESS_FINE_LOCATION` - For Nearby Connections
- `ACCESS_COARSE_LOCATION` - For Nearby Connections
- `BLUETOOTH` - For Nearby Connections
- `NEARBY_WIFI_DEVICES` - For Nearby Connections

### Minimum Requirements
- **Min SDK**: 30 (Android 11)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

## 📦 Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd GameAcivityForFun
```

2. Open in Android Studio:
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory

3. Build and Run:
   - Sync Gradle files
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift+F10`

## 🎥 Demo Video

Watch the demo video to see the game in action:

<div align="center">
  <video width="80%" controls autoplay muted loop>
    <source src="demo.mp4" type="video/mp4">
    Your browser does not support the video tag.
  </video>
</div>

## 📸 Screenshots

*Screenshots coming soon*

## 🎮 Game Features in Detail

### Visual Feedback
- **Selected Piece**: Highlighted in yellow
- **Valid Moves**: Highlighted in green
- **Check Status**: Displayed in status bar
- **Game Over**: Alert dialog with results

### Score System
- Automatically tracks wins, losses, and draws
- Sends game results to API after each game
- Includes player ID, game type, result, and opponent type

### AI Opponent
- Simple but effective AI
- Prioritizes capturing pieces
- Makes random valid moves when no captures available
- Can be extended with minimax algorithm for stronger play

## 🔧 Development

### Project Structure
```
app/src/main/
├── java/com/wheelseye/gameacivityforfun/
│   ├── GameSelectionActivity.kt      # Main game selection screen
│   ├── ChessGameActivity.kt          # Chess game implementation
│   ├── ChessGame.kt                  # Chess logic and rules
│   ├── ChessAI.kt                    # AI opponent logic
│   ├── ChessSquareAdapter.kt         # Board UI adapter
│   └── ScoreApiService.kt            # API integration
└── res/
    ├── layout/                       # UI layouts
    └── values/                       # Strings and resources
```

### Building from Source
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## 🐛 Known Issues

- Nearby Connections requires location permissions
- AI can be improved with better algorithms
- No undo/redo functionality yet

## 🚀 Future Enhancements

- [ ] Improved AI with minimax algorithm
- [ ] Undo/redo moves
- [ ] Move history
- [ ] Online multiplayer
- [ ] Chess puzzles
- [ ] Tutorial mode
- [ ] Custom themes
- [ ] Sound effects

## 📝 License

This project is open source and available for educational purposes.

## 👨‍💻 Author

Developed as a fun chess game project for Android.

## 🙏 Acknowledgments

- Chess piece Unicode symbols
- Google Nearby Connections API
- Material Design Components

---

**Enjoy playing chess!** 🎉

---

**Created by tarun3k**
