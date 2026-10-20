package com.tp.auth.dao;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import com.tp.auth.db.Database;
import com.tp.auth.model.User;

public class UserDao {

    // Le nom d'utilisateur existe-t-il déjà ?
    public boolean existsByUsername(String username) throws Exception {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Enregistre un utilisateur avec son mot de passe hashé
    public void save(String username, String plainPassword) throws Exception {
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
        }
    }

    // Retrouve un utilisateur par son nom (null s'il n'existe pas)
    public User findByUsername(String username) throws Exception {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                    );
                }
            }
        }

        return null;
    }
}