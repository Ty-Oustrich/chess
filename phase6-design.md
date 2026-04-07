# Phase 6: WebSocket

## Shared 

 commands/UserGameCommand.java | Base for outbound WebSocket commands 
 commands/MakeMoveCommand.java | Extends the base command with a ChessMove for json serialization. 
 messages/ServerMessage.java | Base for inbound server messages loadgame error etc.
 messages/LoadGameMessage.java | Server sends current game state- includes a type chessgame for the client to redraw
 messages/ErrorMessage.java | Server rejects invalid commands- sends  errorMessage 
 messages/NotificationMessage.java | messages like 'player moved piece to square' or checkmate

---

## Server


WebSocketHandler.java | Entry point for /ws 
ConnectionManager.java | Tracks which WebSocket sessions belong to which gameID

Integration: server/src/main/java/server/Server.java registers the WebSocket route and constructs the handler with shared DataAccess/services 

---

## Client


WebSocketFacade.java | Opens /ws, sends serialized UserGameCommand instances, and receives server json
GameHandler.java | Interface for gameplay UI
GameUI.java | holds WebSocketFacade. Implements GameHandler and the perspective 
---



Client: GameUI -> WebSocketFacade -> server /ws
Server: WebSocketHandler -> ConnectionManager -> broadcast to sessions in game
Shared: json types for commands and server messages- both sides deserialize
