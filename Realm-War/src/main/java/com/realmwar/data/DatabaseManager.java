// DatabaseManager.java
// Manages database operations for the RealmWar game, handling game state persistence and retrieval using PostgreSQL.

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
import java.util.List;
import java.util.Map;

// Manages database operations for the RealmWar game, handling initialization, saving, loading, and retrieval of game saves.
// Uses PostgreSQL as the database backend with a singleton-like interface for static access.
public final class DatabaseManager {

    // Database connection configuration constants
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/realmwar_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "0000";

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    // Initializes the database by loading the JDBC driver and creating required tables
    public static void initializeDatabase() {
        // Attempt to load PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            GameLogger.log("CRITICAL: PostgreSQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        // Test database connection
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            GameLogger.log("Successfully connected to the database.");
        } catch (SQLException e) {
            GameLogger.log("CRITICAL: Failed to connect to the database.");
            e.printStackTrace();
            return;
        }

        // SQL statements for creating database tables
        String[] createTables = {
                // Stores game save metadata
                "CREATE TABLE IF NOT EXISTS game_saves (" +
                        "id SERIAL PRIMARY KEY," +
                        "save_name TEXT NOT NULL UNIQUE," +
                        "current_player_index INTEGER NOT NULL," +
                        "board_width INTEGER NOT NULL," +
                        "board_height INTEGER NOT NULL," +
                        "winner_name TEXT," +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                // Stores game board tile data
                "CREATE TABLE IF NOT EXISTS game_board_tiles (" +
                        "id SERIAL PRIMARY KEY," +
                        "save_id INTEGER NOT NULL," +
                        "x_coord INTEGER NOT NULL," +
                        "y_coord INTEGER NOT NULL," +
                        "block_class_name TEXT NOT NULL," +
                        "territory_owner_name TEXT," +
                        "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                        ")",

                // Stores game entity data
                "CREATE TABLE IF NOT EXISTS game_entities (" +
                        "id SERIAL PRIMARY KEY," +
                        "save_id INTEGER NOT NULL," +
                        "entity_class_name TEXT NOT NULL," +
                        "owner_name TEXT NOT NULL," +
                        "x_coord INTEGER NOT NULL," +
                        "y_coord INTEGER NOT NULL," +
                        "health INTEGER," +
                        "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                        ")",

                // Stores player unit counts
                "CREATE TABLE IF NOT EXISTS player_unit_counts (" +
                        "id SERIAL PRIMARY KEY," +
                        "save_id INTEGER NOT NULL," +
                        "player_name TEXT NOT NULL," +
                        "unit_type TEXT NOT NULL," +
                        "count INTEGER NOT NULL," +
                        "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                        ")",

                // Stores player resource data
                "CREATE TABLE IF NOT EXISTS player_resources (" +
                        "id SERIAL PRIMARY KEY," +
                        "save_id INTEGER NOT NULL," +
                        "player_name TEXT NOT NULL," +
                        "gold INTEGER NOT NULL," +
                        "food INTEGER NOT NULL," +
                        "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                        ")"
        };

        // Execute table creation statements using try-with-resources
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            for (String table : createTables) {
                stmt.execute(table);
            }
            GameLogger.log("Database initialized successfully.");
        } catch (SQLException e) {
            GameLogger.log("CRITICAL: Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Saves the current game state to the database
    public static boolean saveGame(GameManager gameManager, String saveName) {
        // Verify database connection
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        } catch (SQLException e) {
            GameLogger.log("CRITICAL: Cannot save game: Database connection failed.");
            return false;
        }

        // Prepared SQL statements for inserting game data
        String[] sqlStatements = {
                // Insert game save metadata
                "INSERT INTO game_saves(save_name, current_player_index, board_width, board_height, winner_name) VALUES(?, ?, ?, ?, ?)",
                // Insert game board tiles
                "INSERT INTO game_board_tiles(save_id, x_coord, y_coord, block_class_name, territory_owner_name) VALUES(?, ?, ?, ?, ?)",
                // Insert game entities
                "INSERT INTO game_entities(save_id, entity_class_name, owner_name, x_coord, y_coord, health) VALUES(?, ?, ?, ?, ?, ?)",
                // Insert player unit counts
                "INSERT INTO player_unit_counts(save_id, player_name, unit_type, count) VALUES(?, ?, ?, ?)",
                // Insert player resources
                "INSERT INTO player_resources(save_id, player_name, gold, food) VALUES(?, ?, ?, ?)"
        };

        // Use transaction to ensure data consistency
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            int saveId;

            // Save game metadata and retrieve generated save ID
            try (PreparedStatement ps = conn.prepareStatement(sqlStatements[0], Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, saveName);
                ps.setInt(2, gameManager.getCurrentPlayerIndex());
                ps.setInt(3, gameManager.getGameBoard().width);
                ps.setInt(4, gameManager.getGameBoard().height);
                ps.setString(5, gameManager.winner != null ? gameManager.winner.getName() : null);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    throw new SQLException("Failed to retrieve save_id");
                }
                saveId = rs.getInt(1);
            }

            // Save game board tiles using batch processing
            try (PreparedStatement ps = conn.prepareStatement(sqlStatements[1])) {
                GameBoard board = gameManager.getGameBoard();
                for (int x = 0; x < board.width; x++) {
                    for (int y = 0; y < board.height; y++) {
                        ps.setInt(1, saveId);
                        ps.setInt(2, x);
                        ps.setInt(3, y);
                        ps.setString(4, board.getTile(x, y).block.getClass().getSimpleName());
                        Player owner = board.getTile(x, y).getTerritoryOwner();
                        ps.setString(5, owner != null ? owner.getName() : null);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            // Save game entities using batch processing
            try (PreparedStatement ps = conn.prepareStatement(sqlStatements[2])) {
                GameBoard board = gameManager.getGameBoard();
                for (int x = 0; x < board.width; x++) {
                    for (int y = 0; y < board.height; y++) {
                        GameEntity entity = board.getTile(x, y).getEntity();
                        if (entity != null) {
                            ps.setInt(1, saveId);
                            ps.setString(2, entity.getClass().getSimpleName());
                            ps.setString(3, entity.getOwner().getName());
                            ps.setInt(4, entity.getX());
                            ps.setInt(5, entity.getY());
                            ps.setInt(6, entity instanceof Unit ? ((Unit)entity).getHealth() :
                                    entity instanceof Structure ? ((Structure)entity).getDurability() : 0);
                            ps.addBatch();
                        }
                    }
                }
                ps.executeBatch();
            }

            // Save player unit counts using batch processing
            try (PreparedStatement ps = conn.prepareStatement(sqlStatements[3])) {
                for (Player player : gameManager.getPlayers()) {
                    for (Map.Entry<String, Integer> entry : player.getUnitCounts().entrySet()) {
                        ps.setInt(1, saveId);
                        ps.setString(2, player.getName());
                        ps.setString(3, entry.getKey());
                        ps.setInt(4, entry.getValue());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            // Save player resources using batch processing
            try (PreparedStatement ps = conn.prepareStatement(sqlStatements[4])) {
                for (Player player : gameManager.getPlayers()) {
                    ps.setInt(1, saveId);
                    ps.setString(2, player.getName());
                    ps.setInt(3, player.getResourceHandler().getGold());
                    ps.setInt(4, player.getResourceHandler().getFood());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Commit transaction
            conn.commit();
            GameLogger.log("Game saved successfully: " + saveName);
            return true;
        } catch (SQLException e) {
            GameLogger.log("CRITICAL: Error saving game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Loads a game state from the database by save name
    public static GameManager loadGame(String saveName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Retrieve player names from entities and unit counts
            List<String> playerNames = new ArrayList<>();
            String getPlayersSQL = "SELECT DISTINCT owner_name FROM game_entities WHERE save_id = (SELECT id FROM game_saves WHERE save_name = ?) " +
                    "UNION SELECT DISTINCT player_name FROM player_unit_counts WHERE save_id = (SELECT id FROM game_saves WHERE save_name = ?)";
            try (PreparedStatement ps = conn.prepareStatement(getPlayersSQL)) {
                ps.setString(1, saveName);
                ps.setString(2, saveName);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    playerNames.add(rs.getString("owner_name"));
                }
            }

            // Validate player data
            if (playerNames.isEmpty()) {
                throw new SQLException("No players found in save file");
            }

            // Load game metadata
            String selectSaveSQL = "SELECT id, current_player_index, board_width, board_height FROM game_saves WHERE save_name = ?";
            int boardWidth, boardHeight, currentPlayerIndex;
            try (PreparedStatement ps = conn.prepareStatement(selectSaveSQL)) {
                ps.setString(1, saveName);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new SQLException("Save file not found");

                boardWidth = rs.getInt("board_width");
                boardHeight = rs.getInt("board_height");
                currentPlayerIndex = rs.getInt("current_player_index");
            }

            // Initialize GameManager with loaded metadata
            GameManager gm = new GameManager(playerNames, boardWidth, boardHeight);
            gm.setCurrentPlayerIndex(currentPlayerIndex);

            // Load game components
            loadAndSetTiles(conn, saveName, gm);
            loadAndSetEntities(conn, saveName, gm);
            loadAndSetUnitCounts(conn, saveName, gm);

            // Load player resources
            String loadResourcesSQL = "SELECT player_name, gold, food FROM player_resources WHERE save_id = (SELECT id FROM game_saves WHERE save_name = ?)";
            try (PreparedStatement ps = conn.prepareStatement(loadResourcesSQL)) {
                ps.setString(1, saveName);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String playerName = rs.getString("player_name");
                    int gold = rs.getInt("gold");
                    int food = rs.getInt("food");

                    gm.getPlayers().stream()
                            .filter(p -> p.getName().equals(playerName))
                            .findFirst()
                            .ifPresent(player -> {
                                player.getResourceHandler().addResources(
                                        gold - player.getResourceHandler().getGold(),
                                        food - player.getResourceHandler().getFood()
                                );
                            });
                }
            }

            GameLogger.log("Game loaded successfully: " + saveName);
            return gm;
        } catch (SQLException e) {
            GameLogger.log("CRITICAL: Error loading game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Loads and sets game board tiles from the database
    private static void loadAndSetTiles(Connection conn, String saveName, GameManager gm) throws SQLException {
        String sql = "SELECT x_coord, y_coord, block_class_name, territory_owner_name FROM game_board_tiles " +
                "WHERE save_id = (SELECT id FROM game_saves WHERE save_name = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saveName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                String className = rs.getString("block_class_name");
                String ownerName = rs.getString("territory_owner_name");

                GameTile tile = new GameTile(createBlockFromString(className), x, y);
                if (ownerName != null) {
                    gm.getPlayers().stream()
                            .filter(p -> p.getName().equals(ownerName))
                            .findFirst()
                            .ifPresent(tile::setTerritoryOwner);
                }
                gm.getGameBoard().setTile(x, y, tile);
            }
        }
    }

    // Loads and sets game entities from the database
    private static void loadAndSetEntities(Connection conn, String saveName, GameManager gm) throws SQLException {
        String sql = "SELECT * FROM game_entities WHERE save_id = (SELECT id FROM game_saves WHERE save_name = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saveName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String className = rs.getString("entity_class_name");
                String ownerName = rs.getString("owner_name");
                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                int health = rs.getInt("health");

                Player owner = gm.getPlayers().stream()
                        .filter(p -> p.getName().equals(ownerName))
                        .findFirst()
                        .orElse(null);

                if (owner != null) {
                    GameEntity entity = createEntityFromString(className, owner, x, y);
                    if (entity != null) {
                        if (entity instanceof Unit unit) unit.health = health;
                        if (entity instanceof Structure structure) structure.setDurability(health);
                        gm.getGameBoard().placeEntity(entity, x, y);
                    }
                }
            }
        }
    }

    // Loads and sets player unit counts from the database
    private static void loadAndSetUnitCounts(Connection conn, String saveName, GameManager gm) throws SQLException {
        String sql = "SELECT player_name, unit_type, count FROM player_unit_counts " +
                "WHERE save_id = (SELECT id FROM game_saves WHERE save_name = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, saveName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String playerName = rs.getString("player_name");
                String unitType = rs.getString("unit_type");
                int count = rs.getInt("count");

                gm.getPlayers().stream()
                        .filter(p -> p.getName().equals(playerName))
                        .findFirst()
                        .ifPresent(player -> player.getUnitCounts().put(unitType, count));
            }
        }
    }

    // Creates a Block instance based on the provided class name
    private static Block createBlockFromString(String className) {
        return switch (className) {
            case "ForestBlock" -> new ForestBlock();
            case "VoidBlock" -> new VoidBlock();
            default -> new EmptyBlock();
        };
    }

    // Creates a GameEntity instance based on the provided type, owner, and coordinates
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

    // Retrieves an array of all save game names, ordered by timestamp (newest first)
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
            GameLogger.log("CRITICAL: Error fetching save games: " + e.getMessage());
            return new String[0];
        }
    }
}