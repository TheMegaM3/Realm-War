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
import java.util.ArrayList;

public final class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:realmwar.db";

    private DatabaseManager() {}

    public static void initializeDatabase() {
        String createSavesTable = "CREATE TABLE IF NOT EXISTS game_saves (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "save_name TEXT NOT NULL UNIQUE," +
                "current_player_index INTEGER NOT NULL," +
                "winner_name TEXT," +
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

    public static boolean saveGame(GameManager gameManager, String saveName) {
        try {
            // 1. ذخیره وضعیت اصلی بازی
            String saveGameSQL = "INSERT INTO game_saves(save_name, current_player_index, winner_name) VALUES(?, ?, ?)";

            // 2. ذخیره زمین بازی
            String saveTilesSQL = "INSERT INTO game_board_tiles(save_id, x_coord, y_coord, block_class_name) VALUES(?, ?, ?, ?)";

            // 3. ذخیره موجودیت‌ها
            String saveEntitiesSQL = "INSERT INTO game_entities(save_id, entity_class_name, owner_name, x_coord, y_coord, health) VALUES(?, ?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.setAutoCommit(false);

                // ذخیره وضعیت اصلی
                PreparedStatement ps = conn.prepareStatement(saveGameSQL, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, saveName);
                ps.setInt(2, gameManager.getCurrentPlayerIndex());
                ps.setString(3, gameManager.winner != null ? gameManager.winner.getName() : null);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int saveId = rs.next() ? rs.getInt(1) : -1;

                // ذخیره زمین بازی
                ps = conn.prepareStatement(saveTilesSQL);
                GameBoard board = gameManager.getGameBoard();
                for (int x = 0; x < board.width; x++) {
                    for (int y = 0; y < board.height; y++) {
                        ps.setInt(1, saveId);
                        ps.setInt(2, x);
                        ps.setInt(3, y);
                        ps.setString(4, board.getTile(x, y).block.getClass().getSimpleName());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();

                // ذخیره موجودیت‌ها
                ps = conn.prepareStatement(saveEntitiesSQL);
                for (int x = 0; x < board.width; x++) {
                    for (int y = 0; y < board.height; y++) {
                        GameEntity entity = board.getTile(x, y).getEntity();
                        if (entity != null) {
                            ps.setInt(1, saveId);
                            ps.setString(2, entity.getClass().getSimpleName());
                            ps.setString(3, entity.getOwner().getName());
                            ps.setInt(4, entity.getX());
                            ps.setInt(5, entity.getY());

                            if (entity instanceof Unit) {
                                ps.setInt(6, ((Unit) entity).getHealth());
                            } else if (entity instanceof Structure) {
                                ps.setInt(6, ((Structure) entity).getDurability());
                            } else {
                                ps.setNull(6, Types.INTEGER);
                            }

                            ps.addBatch();
                        }
                    }
                }
                ps.executeBatch();

                conn.commit();
                GameLogger.log("Game saved successfully: " + saveName);
                return true;
            }
        } catch (SQLException e) {
            GameLogger.log("Error saving game: " + e.getMessage());
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

    public static String[] getSaveGames() {
        String sql = "SELECT save_name FROM game_saves ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<String> saves = new ArrayList<>();
            while (rs.next()) {
                saves.add(rs.getString("save_name"));
            }
            return saves.toArray(new String[0]);
        } catch (SQLException e) {
            GameLogger.log("Error fetching save games: " + e.getMessage());
            return new String[0];
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