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
import com.realmwar.model.structures.Structure;
import com.realmwar.model.units.Unit;
import com.realmwar.model.structures.*;
import com.realmwar.model.units.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

/**
 * Manages all database operations for saving and loading game states using SQLite.
 * This version is updated to handle polymorphic Block types for terrain.
 */
public final class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:realmwar.db";

    private DatabaseManager() {}

    /**
     * Initializes the database by creating the necessary tables if they don't already exist.
     * This version includes a table to store the terrain of each tile.
     */
    public static void initializeDatabase() {
        String createSavesTable = "CREATE TABLE IF NOT EXISTS game_saves (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "save_name TEXT NOT NULL UNIQUE," +
                "current_player_index INTEGER NOT NULL," +
                "winner_name TEXT," + // Can be NULL if game is not over
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createTilesTable = "CREATE TABLE IF NOT EXISTS game_board_tiles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "save_id INTEGER NOT NULL," +
                "x_coord INTEGER NOT NULL," +
                "y_coord INTEGER NOT NULL," +
                "block_class_name TEXT NOT NULL," +
                "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                ");";

        String createEntitiesTable = "CREATE TABLE IF NOT EXISTS game_entities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "save_id INTEGER NOT NULL," +
                "entity_class_name TEXT NOT NULL," +
                "owner_name TEXT NOT NULL," +
                "x_coord INTEGER NOT NULL," +
                "y_coord INTEGER NOT NULL," +
                "health INTEGER," +
                "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSavesTable);
            stmt.execute(createTilesTable);
            stmt.execute(createEntitiesTable);
            GameLogger.log("Database initialized successfully.");
        } catch (SQLException e) {
            GameLogger.log("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Saves the current state of the game to the database.
     * @param gameManager The GameManager instance containing the state to save.
     * @param saveName The unique name for this save file.
     * @return true if the save was successful, false otherwise.
     */
    public static boolean saveGame(GameManager gameManager, String saveName) {
        deleteSave(saveName); // Clear any old save with the same name

        String insertSaveSQL = "INSERT INTO game_saves(save_name, current_player_index, winner_name) VALUES(?, ?, ?)";
        String insertTileSQL = "INSERT INTO game_board_tiles(save_id, x_coord, y_coord, block_class_name) VALUES(?, ?, ?, ?)";
        String insertEntitySQL = "INSERT INTO game_entities(save_id, entity_class_name, owner_name, x_coord, y_coord, health) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false); // Use transaction for atomicity

            // 1. Insert into game_saves and get the new save_id
            long saveId = insertSaveState(conn, insertSaveSQL, saveName, gameManager);

            // 2. Insert all board tiles
            GameBoard board = gameManager.getGameBoard();
            try (PreparedStatement ps = conn.prepareStatement(insertTileSQL)) {
                for (int x = 0; x < board.width; x++) {
                    for (int y = 0; y < board.height; y++) {
                        ps.setLong(1, saveId);
                        ps.setInt(2, x);
                        ps.setInt(3, y);
                        ps.setString(4, board.getTile(x,y).block.getClass().getSimpleName());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            // 3. Insert all entities
            try (PreparedStatement ps = conn.prepareStatement(insertEntitySQL)) {
                for (int x = 0; x < board.width; x++) {
                    for (int y = 0; y < board.height; y++) {
                        GameEntity entity = board.getTile(x,y).getEntity();
                        if (entity != null) {
                            ps.setLong(1, saveId);
                            ps.setString(2, entity.getClass().getSimpleName());
                            ps.setString(3, entity.getOwner().getName());
                            ps.setInt(4, entity.getX());
                            ps.setInt(5, entity.getY());

                            // Store current health for both Units and Structures
                            if (entity instanceof Unit) ps.setInt(6, ((Unit) entity).getHealth());
                            else if (entity instanceof Structure) ps.setInt(6, ((Structure) entity).getDurability());
                            else ps.setNull(6, Types.INTEGER);

                            ps.addBatch();
                        }
                    }
                }
                ps.executeBatch();
            }

            conn.commit(); // Finalize transaction
            GameLogger.log("Game state '" + saveName + "' saved successfully.");
            return true;
        } catch (SQLException e) {
            GameLogger.log("Error saving game state: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static long insertSaveState(Connection conn, String sql, String saveName, GameManager gm) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, saveName);
            ps.setInt(2, gm.getCurrentPlayerIndex());
            if (gm.winner != null) ps.setString(3, gm.winner.getName());
            else ps.setNull(3, Types.VARCHAR);

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getLong(1);
            else throw new SQLException("Failed to create save record.");
        }
    }

    /**
     * Loads a game state from the database. This method is complex as it must
     * fully reconstruct the entire game state from database records.
     * @param saveName The name of the save to load.
     * @return A new GameManager instance with the loaded state, or null if loading fails.
     */
    public static GameManager loadGame(String saveName) {
        List<String> playerNames = Arrays.asList("Player 1", "Player 2");
        GameManager gm = new GameManager(playerNames, 16, 16);

        String selectSaveSQL = "SELECT id, current_player_index, winner_name FROM game_saves WHERE save_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            long saveId;

            try (PreparedStatement ps = conn.prepareStatement(selectSaveSQL)) {
                ps.setString(1, saveName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    saveId = rs.getLong("id");
                    gm.setCurrentPlayerIndex(rs.getInt("current_player_index"));
                } else {
                    throw new SQLException("Save file not found: " + saveName);
                }
            }


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

    private static void loadAndSetTiles(Connection conn, long saveId, GameBoard board) throws SQLException {
        String sql = "SELECT x_coord, y_coord, block_class_name FROM game_board_tiles WHERE save_id = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saveId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                String className = rs.getString("block_class_name");

                board.setTile(x, y, new GameTile(createBlockFromString(className)));
            }
        }
    }

    private static void loadAndSetEntities(Connection conn, long saveId, GameManager gm) throws SQLException {
        String sql = "SELECT * FROM game_entities WHERE save_id = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saveId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String className = rs.getString("entity_class_name");
                String ownerName = rs.getString("owner_name");
                Player owner = gm.getPlayers().stream().filter(p -> p.getName().equals(ownerName)).findFirst().orElse(null);
                if (owner == null) continue;

                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                int health = rs.getInt("health");

                GameEntity entity = createEntityFromString(className, owner, x, y);
                if (entity instanceof Unit) ((Unit) entity).health = health;
                if (entity instanceof Structure) ((Structure) entity).setDurability(health);

                gm.getGameBoard().placeEntity(entity, x, y);
            }
        }
    }

    // --- Factory Helper Methods for Loading ---

    private static Block createBlockFromString(String className) {
        switch (className) {
            case "ForestBlock": return new ForestBlock();
            case "VoidBlock": return new VoidBlock();
            default: return new EmptyBlock();
        }
    }

    private static GameEntity createEntityFromString(String type, Player owner, int x, int y) {
        switch (type) {
            case "Peasant": return new Peasant(owner, x, y);
            case "Spearman": return new Spearman(owner, x, y);
            case "Swordsman": return new Swordsman(owner, x, y);
            case "Knight": return new Knight(owner, x, y);
            case "TownHall": return new TownHall(owner, x, y);
            case "Farm": return new Farm(owner, x, y);
            case "Barrack": return new Barrack(owner, x, y);
            case "Market": return new Market(owner, x, y);
            case "Tower": return new Tower(owner, x, y);
            default: return null;
        }
    }

    private static void deleteSave(String saveName) {
        String sql = "DELETE FROM game_saves WHERE save_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saveName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
