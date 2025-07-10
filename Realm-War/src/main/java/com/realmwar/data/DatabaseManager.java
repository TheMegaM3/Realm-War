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

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/realmwar_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "0000";

    private DatabaseManager() {}

    public static void initializeDatabase() {
        // <<< استفاده از SERIAL PRIMARY KEY >>>
        String createSavesTable = "CREATE TABLE IF NOT EXISTS game_saves (" +
                "id SERIAL PRIMARY KEY," +
                "save_name TEXT NOT NULL UNIQUE," +
                "current_player_index INTEGER NOT NULL," +
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

        // < استفاده از اطلاعات اتصال >
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSavesTable);
            stmt.execute(createTilesTable);
            stmt.execute(createEntitiesTable);
            GameLogger.log("Database initialized successfully.");
        } catch (SQLException e) {
            GameLogger.log("Error initializing database: " + e.getMessage());
            e.printStackTrace(); // چاپ خطا برای دیباگ کردن بهتر
        }
    }

    public static boolean saveGame(GameManager gameManager, String saveName) {
        String saveGameSQL = "INSERT INTO game_saves(save_name, current_player_index, winner_name) VALUES(?, ?, ?)";
        String saveTilesSQL = "INSERT INTO game_board_tiles(save_id, x_coord, y_coord, block_class_name) VALUES(?, ?, ?, ?)";
        String saveEntitiesSQL = "INSERT INTO game_entities(save_id, entity_class_name, owner_name, x_coord, y_coord, health) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(saveGameSQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, saveName);
                ps.setInt(2, gameManager.getCurrentPlayerIndex());
                ps.setString(3, gameManager.winner != null ? gameManager.winner.getName() : null);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int saveId = -1;
                if (rs.next()) {
                    saveId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve save_id.");
                }

                // ذخیره کاشی‌های بازی
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

                // ذخیره موجودیت‌های بازی
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

                                if (entity instanceof Unit) {
                                    entitiesPs.setInt(6, ((Unit) entity).getHealth());
                                } else if (entity instanceof Structure) {
                                    entitiesPs.setInt(6, ((Structure) entity).getDurability());
                                } else {
                                    entitiesPs.setNull(6, Types.INTEGER);
                                }
                                entitiesPs.addBatch();
                            }
                        }
                    }
                    entitiesPs.executeBatch();
                }

                conn.commit();
                GameLogger.log("Game saved successfully: " + saveName); // مطابق نیازمندی فایل لاگ
                return true;

            } catch (SQLException e) {
                conn.rollback(); // در صورت بروز خطا، تمام تغییرات را لغو کن
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

    public static GameManager loadGame(String saveName) {
        List<String> playerNames = Arrays.asList("Player 1", "Player 2");
        GameManager gm = new GameManager(playerNames, 10, 10); // اندازه برد باید از جایی دیگر بیاید یا ثابت باشد
        String selectSaveSQL = "SELECT id, current_player_index, winner_name FROM game_saves WHERE save_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
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
            return new String[0];
        }
    }
}
