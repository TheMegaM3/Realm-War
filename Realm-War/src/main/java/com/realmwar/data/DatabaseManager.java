package com.realmwar.data;

import com.realmwar.engine.GameBoard;
import com.realmwar.engine.GameManager;
import com.realmwar.engine.GameTile;
import com.realmwar.engine.blocks.Block;
import com.realmwar.engine.blocks.EmptyBlock;
import com.realmwar.engine.blocks.ForestBlock;
import com.realmwar.engine.blocks.VoidBlock;
import com.realmwar.model.GameEntity;
import com.realmwar.model.Player;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages all database operations, including initialization, saving, and loading game states.
 * This class uses JDBC to connect to a PostgreSQL database.
 */
public final class DatabaseManager {

    // Database connection credentials.
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/realmwar_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "0000";

    // Private constructor to prevent instantiation of this utility class.
    private DatabaseManager() {}

    /**
     * Initializes the database by creating the necessary tables if they don't already exist.
     */
    public static void initializeDatabase() {
        // SQL statements to create tables. 'SERIAL PRIMARY KEY' creates an auto-incrementing ID.
        // 'ON DELETE CASCADE' ensures that when a save is deleted, all its associated tiles and entities are also deleted.
        String createSavesTable = "CREATE TABLE IF NOT EXISTS game_saves (" +
                "id SERIAL PRIMARY KEY," +
                "save_name TEXT NOT NULL UNIQUE," +
                "current_player_index INTEGER NOT NULL," +
                "board_width INTEGER NOT NULL," +
                "board_height INTEGER NOT NULL," +
                "winner_name TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createTilesTable = "CREATE TABLE IF NOT EXISTS game_board_tiles (" +
                "id SERIAL PRIMARY KEY," +
                "save_id INTEGER NOT NULL," +
                "x_coord INTEGER NOT NULL," +
                "y_coord INTEGER NOT NULL," +
                "block_class_name TEXT NOT NULL," +
                "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                ");";

        String createEntitiesTable = "CREATE TABLE IF NOT EXISTS game_entities (" +
                "id SERIAL PRIMARY KEY," +
                "save_id INTEGER NOT NULL," +
                "entity_class_name TEXT NOT NULL," +
                "owner_name TEXT NOT NULL," +
                "x_coord INTEGER NOT NULL," +
                "y_coord INTEGER NOT NULL," +
                "health INTEGER," +
                "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                ");";

        // 'try-with-resources' ensures the connection and statement are always closed.
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSavesTable);
            stmt.execute(createTilesTable);
            stmt.execute(createEntitiesTable);
            GameLogger.log("Database initialized successfully.");
        } catch (SQLException e) {
            GameLogger.log("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves the current game state to the database in a single transaction.
     * @param gameManager The current game manager instance.
     * @param saveName The name for the save file.
     * @return true if saving was successful, false otherwise.
     */
    public static boolean saveGame(GameManager gameManager, String saveName) {
        // Using PreparedStatement with '?' placeholders to prevent SQL injection.
        String saveGameSQL = "INSERT INTO game_saves(save_name, current_player_index, board_width, board_height, winner_name) VALUES(?, ?, ?, ?, ?)";
        String saveTilesSQL = "INSERT INTO game_board_tiles(save_id, x_coord, y_coord, block_class_name) VALUES(?, ?, ?, ?)";
        String saveEntitiesSQL = "INSERT INTO game_entities(save_id, entity_class_name, owner_name, x_coord, y_coord, health) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Start a transaction. All commands will either succeed together or fail together.
            conn.setAutoCommit(false);

            try {
                // --- Save the main game state ---
                int saveId;
                try (PreparedStatement ps = conn.prepareStatement(saveGameSQL, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, saveName);
                    ps.setInt(2, gameManager.getCurrentPlayerIndex());
                    ps.setInt(3, gameManager.getGameBoard().width);
                    ps.setInt(4, gameManager.getGameBoard().height);
                    ps.setString(5, gameManager.winner != null ? gameManager.winner.getName() : null);
                    ps.executeUpdate();

                    // Get the auto-generated ID for this save to use in other tables.
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        saveId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve save_id, rolling back.");
                    }
                }

                // --- Save all game board tiles using batch processing for performance ---
                try (PreparedStatement tilesPs = conn.prepareStatement(saveTilesSQL)) {
                    GameBoard board = gameManager.getGameBoard();
                    for (int x = 0; x < board.width; x++) {
                        for (int y = 0; y < board.height; y++) {
                            tilesPs.setInt(1, saveId);
                            tilesPs.setInt(2, x);
                            tilesPs.setInt(3, y);
                            tilesPs.setString(4, board.getTile(x, y).block.getClass().getSimpleName());
                            tilesPs.addBatch();
                        }
                    }
                    tilesPs.executeBatch();
                }

                // --- Save all game entities (units and structures) using batch processing ---
                try (PreparedStatement entitiesPs = conn.prepareStatement(saveEntitiesSQL)) {
                    GameBoard board = gameManager.getGameBoard();
                    for (int x = 0; x < board.width; x++) {
                        for (int y = 0; y < board.height; y++) {
                            GameEntity entity = board.getTile(x, y).getEntity();
                            if (entity != null) {
                                entitiesPs.setInt(1, saveId);
                                entitiesPs.setString(2, entity.getClass().getSimpleName());
                                entitiesPs.setString(3, entity.getOwner().getName());
                                entitiesPs.setInt(4, entity.getX());
                                entitiesPs.setInt(5, entity.getY());

                                // Save health for units and durability for structures in the same column.
                                if (entity instanceof Unit unit) {
                                    entitiesPs.setInt(6, unit.getHealth());
                                } else if (entity instanceof Structure structure) {
                                    entitiesPs.setInt(6, structure.getDurability());
                                } else {
                                    entitiesPs.setNull(6, Types.INTEGER);
                                }
                                entitiesPs.addBatch();
                            }
                        }
                    }
                    entitiesPs.executeBatch();
                }

                // If all commands were successful, commit the transaction to make changes permanent.
                conn.commit();
                GameLogger.log("Game saved successfully: " + saveName);
                return true;

            } catch (SQLException e) {
                // If any error occurred, roll back the entire transaction to prevent a corrupted save.
                conn.rollback();
                GameLogger.log("Error saving game, transaction rolled back: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            GameLogger.log("Failed to connect to the database for saving: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a game state from the database.
     * @param saveName The name of the game to load.
     * @return A new GameManager instance populated with the loaded state, or null on failure.
     */
    public static GameManager loadGame(String saveName) {
        // Note: Player names are hardcoded here. A more advanced system might save player names too.
        List<String> playerNames = new ArrayList<>();
        // In a real multi-player save system, we would save the number of players and their names.
        // For this project, we assume a loaded game can have up to 4 players and name them generically.
        playerNames.add("Player 1");
        playerNames.add("Player 2");
        playerNames.add("Player 3");
        playerNames.add("Player 4");


        String selectSaveSQL = "SELECT id, current_player_index, winner_name, board_width, board_height FROM game_saves WHERE save_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            long saveId;
            GameManager gm;

            // First, load the main save data to get board dimensions and create the GameManager.
            try (PreparedStatement ps = conn.prepareStatement(selectSaveSQL)) {
                ps.setString(1, saveName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    saveId = rs.getLong("id");
                    int boardWidth = rs.getInt("board_width");
                    int boardHeight = rs.getInt("board_height");

                    gm = new GameManager(playerNames, boardWidth, boardHeight);
                    gm.setCurrentPlayerIndex(rs.getInt("current_player_index"));
                    // We can also load the winner state here if needed
                } else {
                    throw new SQLException("Save file not found: " + saveName);
                }
            }

            // Then, populate the game board with tiles and entities.
            loadAndSetTiles(conn, saveId, gm.getGameBoard());
            loadAndSetEntities(conn, saveId, gm);

            GameLogger.log("Game state '" + saveName + "' loaded successfully.");
            return gm;
        } catch (Exception e) {
            GameLogger.log("Error loading game state: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A helper method to load and set all tiles for a given save.
     */
    private static void loadAndSetTiles(Connection conn, long saveId, GameBoard board) throws SQLException {
        String sql = "SELECT x_coord, y_coord, block_class_name FROM game_board_tiles WHERE save_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                String className = rs.getString("block_class_name");
                board.setTile(x, y, new GameTile(createBlockFromString(className), x, y));

            }
        }
    }

    /**
     * A helper method to load and set all entities for a given save.
     */
    private static void loadAndSetEntities(Connection conn, long saveId, GameManager gm) throws SQLException {
        String sql = "SELECT * FROM game_entities WHERE save_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String className = rs.getString("entity_class_name");
                String ownerName = rs.getString("owner_name");
                // Find the player object that matches the saved owner name.
                Player owner = gm.getPlayers().stream()
                        .filter(p -> p.getName().equals(ownerName))
                        .findFirst()
                        .orElse(null);

                if (owner == null) continue; // Skip if owner not found in current game setup.

                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                int health = rs.getInt("health");

                GameEntity entity = createEntityFromString(className, owner, x, y);
                if (entity == null) continue;

                // Set the loaded health/durability.
                if (entity instanceof Unit unit) unit.health = health;
                if (entity instanceof Structure structure) structure.setDurability(health);

                gm.getGameBoard().placeEntity(entity, x, y);
            }
        }
    }

    /**
     * Factory method to create Block objects from their class name string.
     */
    private static Block createBlockFromString(String className) {
        return switch (className) {
            case "ForestBlock" -> new ForestBlock();
            case "VoidBlock" -> new VoidBlock();
            default -> new EmptyBlock();
        };
    }

    /**
     * Factory method to create GameEntity objects from their class name string.
     */
    private static GameEntity createEntityFromString(String type, Player owner, int x, int y) {
        return switch (type) {
            case "Peasant" -> new Peasant(owner, x, y);
            case "Spearman" -> new Spearman(owner, x, y);
            case "Swordsman" -> new Swordsman(owner, x, y);
            case "Knight" -> new Knight(owner, x, y);
            case "TownHall" -> new TownHall(owner, x, y);
            case "Farm" -> new Farm(owner, x, y);
            case "Barrack" -> new Barrack(owner, x, y);
            case "Market" -> new Market(owner, x, y);
            case "Tower" -> new Tower(owner, x, y);
            default -> null;
        };
    }

    /**
     * Retrieves a list of all saved game names from the database.
     * @return An array of save game names, newest first.
     */
    public static String[] getSaveGames() {
        String sql = "SELECT save_name FROM game_saves ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<String> saves = new ArrayList<>();
            while (rs.next()) {
                saves.add(rs.getString("save_name"));
            }
            return saves.toArray(new String[0]);
        } catch (SQLException e) {
            GameLogger.log("Error fetching save games: " + e.getMessage());
            return new String[0]; // Return an empty array on error to prevent crashes.
        }
    }
}
