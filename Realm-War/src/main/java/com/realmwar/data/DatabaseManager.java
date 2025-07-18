// DatabaseManager.java
// Manages database operations for the RealmWar game, handling game state persistence and retrieval using PostgreSQL.
// This class is designed as a singleton utility with static methods to initialize, save, load, and manage game states.

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

// Singleton utility class for managing database interactions in the RealmWar game
public final class DatabaseManager {
    // Database connection parameters (URL, username, password)
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/realmwar_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "0000";

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    // Initializes the database by checking the driver, connection, and creating necessary tables if they don't exist
    public static void initializeDatabase() {
        // Check if PostgreSQL driver is available
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            GameLogger.log("PostgreSQL JDBC Driver not found. Ensure the driver is included in your project dependencies.");
            e.printStackTrace();
            return;
        }

        // Try to connect to the database to ensure it's accessible
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            GameLogger.log("Successfully connected to the database.");
        } catch (SQLException e) {
            GameLogger.log("Failed to connect to the database. Please ensure PostgreSQL is running and the database 'realmwar_db' exists.");
            GameLogger.log("Connection error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // SQL statement to create the game_saves table
        String createSavesTable = "CREATE TABLE IF NOT EXISTS game_saves (" +
                "id SERIAL PRIMARY KEY," +
                "save_name TEXT NOT NULL UNIQUE," +
                "current_player_index INTEGER NOT NULL," +
                "board_width INTEGER NOT NULL," +
                "board_height INTEGER NOT NULL," +
                "winner_name TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        // SQL(statement to create the game_board_tiles table
        String createTilesTable = "CREATE TABLE IF NOT EXISTS game_board_tiles (" +
                "id SERIAL PRIMARY KEY," +
                "save_id INTEGER NOT NULL," +
                "x_coord INTEGER NOT NULL," +
                "y_coord INTEGER NOT NULL," +
                "block_class_name TEXT NOT NULL," +
                "territory_owner_name TEXT," +
                "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                ");";

        // SQL statement to create the game_entities table
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

        // SQL statement to create the player_unit_counts table
        String createUnitCountsTable = "CREATE TABLE IF NOT EXISTS player_unit_counts (" +
                "id SERIAL PRIMARY KEY," +
                "save_id INTEGER NOT NULL," +
                "player_name TEXT NOT NULL," +
                "unit_type TEXT NOT NULL," +
                "count INTEGER NOT NULL," +
                "FOREIGN KEY (save_id) REFERENCES game_saves(id) ON DELETE CASCADE" +
                ");";

        // Execute table creation and check for missing columns
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            // Create tables
            stmt.execute(createSavesTable);
            stmt.execute(createEntitiesTable);
            stmt.execute(createUnitCountsTable);

            // Check if territory_owner_name column exists in game_board_tiles
            ResultSet rs = conn.getMetaData().getColumns(null, null, "game_board_tiles", "territory_owner_name");
            if (!rs.next()) {
                // Add territory_owner_name column if it doesn't exist
                stmt.execute("ALTER TABLE game_board_tiles ADD COLUMN territory_owner_name TEXT;");
                GameLogger.log("Added territory_owner_name column to game_board_tiles table.");
            }

            // Create game_board_tiles table (in case it doesn't exist)
            stmt.execute(createTilesTable);
            GameLogger.log("Database initialized successfully.");
        } catch (SQLException e) {
            GameLogger.log("Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Saves the current game state to the database
    public static boolean saveGame(GameManager gameManager, String saveName) {
        // Check database connection before attempting to save
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Connection successful, proceed with save
        } catch (SQLException e) {
            GameLogger.log("Cannot save game: Failed to connect to the database. " + e.getMessage());
            return false;
        }

        // SQL statements for inserting game data
        String saveGameSQL = "INSERT INTO game_saves(save_name, current_player_index, board_width, board_height, winner_name) VALUES(?, ?, ?, ?, ?)";
        String saveTilesSQL = "INSERT INTO game_board_tiles(save_id, x_coord, y_coord, block_class_name, territory_owner_name) VALUES(?, ?, ?, ?, ?)";
        String saveEntitiesSQL = "INSERT INTO game_entities(save_id, entity_class_name, owner_name, x_coord, y_coord, health) VALUES(?, ?, ?, ?, ?, ?)";
        String saveUnitCountsSQL = "INSERT INTO player_unit_counts(save_id, player_name, unit_type, count) VALUES(?, ?, ?, ?)";

        // Perform save operations within a transaction
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);

            try {
                // Insert game save metadata
                int saveId;
                try (PreparedStatement ps = conn.prepareStatement(saveGameSQL, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, saveName);
                    ps.setInt(2, gameManager.getCurrentPlayerIndex());
                    ps.setInt(3, gameManager.getGameBoard().width);
                    ps.setInt(4, gameManager.getGameBoard().height);
                    ps.setString(5, gameManager.winner != null ? gameManager.winner.getName() : null);
                    ps.executeUpdate();

                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        saveId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve save_id, rolling back.");
                    }
                }

                // Save game board tiles
                try (PreparedStatement tilesPs = conn.prepareStatement(saveTilesSQL)) {
                    GameBoard board = gameManager.getGameBoard();
                    for (int x = 0; x < board.width; x++) {
                        for (int y = 0; y < board.height; y++) {
                            tilesPs.setInt(1, saveId);
                            tilesPs.setInt(2, x);
                            tilesPs.setInt(3, y);
                            tilesPs.setString(4, board.getTile(x, y).block.getClass().getSimpleName());
                            Player territoryOwner = board.getTile(x, y).getTerritoryOwner();
                            tilesPs.setString(5, territoryOwner != null ? territoryOwner.getName() : null);
                            tilesPs.addBatch();
                        }
                    }
                    tilesPs.executeBatch();
                }

                // Save game entities (units and structures)
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

                // Save player unit counts
                try (PreparedStatement unitCountsPs = conn.prepareStatement(saveUnitCountsSQL)) {
                    for (Player player : gameManager.getPlayers()) {
                        for (Map.Entry<String, Integer> entry : player.getUnitCounts().entrySet()) {
                            unitCountsPs.setInt(1, saveId);
                            unitCountsPs.setString(2, player.getName());
                            unitCountsPs.setString(3, entry.getKey());
                            unitCountsPs.setInt(4, entry.getValue());
                            unitCountsPs.addBatch();
                        }
                    }
                    unitCountsPs.executeBatch();
                }

                // Commit the transaction
                conn.commit();
                GameLogger.log("Game saved successfully: " + saveName);
                return true;

            } catch (SQLException e) {
                // Rollback transaction on error
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

    // Loads a game state from the database by save name
    public static GameManager loadGame(String saveName) {
        // Default player names for initializing GameManager
        List<String> playerNames = new ArrayList<>();
        playerNames.add("Player 1");
        playerNames.add("Player 2");
        playerNames.add("Player 3");
        playerNames.add("Player 4");

        // SQL query to retrieve game save metadata
        String selectSaveSQL = "SELECT id, current_player_index, winner_name, board_width, board_height FROM game_saves WHERE save_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            long saveId;
            GameManager gm;

            // Retrieve game save metadata
            try (PreparedStatement ps = conn.prepareStatement(selectSaveSQL)) {
                ps.setString(1, saveName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    saveId = rs.getLong("id");
                    int boardWidth = rs.getInt("board_width");
                    int boardHeight = rs.getInt("board_height");

                    gm = new GameManager(playerNames, boardWidth, boardHeight);
                    gm.setCurrentPlayerIndex(rs.getInt("current_player_index"));
                } else {
                    throw new SQLException("Save file not found: " + saveName);
                }
            }

            // Load game components
            loadAndSetTiles(conn, saveId, gm);
            loadAndSetEntities(conn, saveId, gm);
            loadAndSetUnitCounts(conn, saveId, gm);

            GameLogger.log("Game state '" + saveName + "' loaded successfully.");
            return gm;
        } catch (SQLException e) {
            GameLogger.log("Error loading game state: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Loads and sets game board tiles from the database
    private static void loadAndSetTiles(Connection conn, long saveId, GameManager gm) throws SQLException {
        String sql = "SELECT x_coord, y_coord, block_class_name, territory_owner_name FROM game_board_tiles WHERE save_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                String className = rs.getString("block_class_name");
                String territoryOwnerName = rs.getString("territory_owner_name");
                GameTile tile = new GameTile(createBlockFromString(className), x, y);
                if (territoryOwnerName != null) {
                    Player territoryOwner = gm.getPlayers().stream()
                            .filter(p -> p.getName().equals(territoryOwnerName))
                            .findFirst()
                            .orElse(null);
                    tile.setTerritoryOwner(territoryOwner);
                }
                gm.getGameBoard().setTile(x, y, tile);
            }
        }
    }

    // Loads and sets game entities from the database
    private static void loadAndSetEntities(Connection conn, long saveId, GameManager gm) throws SQLException {
        String sql = "SELECT * FROM game_entities WHERE save_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String className = rs.getString("entity_class_name");
                String ownerName = rs.getString("owner_name");
                Player owner = gm.getPlayers().stream()
                        .filter(p -> p.getName().equals(ownerName))
                        .findFirst()
                        .orElse(null);

                if (owner == null) continue;

                int x = rs.getInt("x_coord");
                int y = rs.getInt("y_coord");
                int health = rs.getInt("health");

                GameEntity entity = createEntityFromString(className, owner, x, y);
                if (entity == null) continue;

                if (entity instanceof Unit unit) unit.health = health;
                if (entity instanceof Structure structure) structure.setDurability(health);

                gm.getGameBoard().placeEntity(entity, x, y);
            }
        }
    }

    // Loads and sets player unit counts from the database
    private static void loadAndSetUnitCounts(Connection conn, long saveId, GameManager gm) throws SQLException {
        String sql = "SELECT player_name, unit_type, count FROM player_unit_counts WHERE save_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String playerName = rs.getString("player_name");
                String unitType = rs.getString("unit_type");
                int count = rs.getInt("count");
                Player player = gm.getPlayers().stream()
                        .filter(p -> p.getName().equals(playerName))
                        .findFirst()
                        .orElse(null);
                if (player != null) {
                    player.getUnitCounts().put(unitType, count);
                }
            }
        }
    }

    // Creates a block instance based on its class name
    private static Block createBlockFromString(String className) {
        return switch (className) {
            case "ForestBlock" -> new ForestBlock();
            case "VoidBlock" -> new VoidBlock();
            default -> new EmptyBlock();
        };
    }

    // Creates a game entity instance based on its class name
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

    // Retrieves a list of all save game names
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
            e.printStackTrace();
            return new String[0];
        }
    }
}