package com.tp.auth.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.tp.auth.db.Database;
import com.tp.auth.model.Contact;

public class ContactDao {

    // Récupère tous les contacts d'un utilisateur,
    // triés par nom puis prénom
    public List<Contact> findByUser(int userId) throws Exception {
        List<Contact> contacts = new ArrayList<>();

        String sql = """
            SELECT id, user_id, nom, prenom, email, telephone
            FROM contacts
            WHERE user_id = ?
            ORDER BY nom, prenom
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contacts.add(new Contact(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("telephone")
                    ));
                }
            }
        }

        return contacts;
    }

    // Ajoute un contact
    public void insert(Contact c) throws Exception {
        String sql = """
            INSERT INTO contacts (user_id, nom, prenom, email, telephone)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, c.getUserId());
            ps.setString(2, c.getNom());
            ps.setString(3, c.getPrenom());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getTelephone());

            ps.executeUpdate();
        }
    }

    // Modifie le nom, le prénom, l'email et le téléphone
    public void update(Contact c) throws Exception {
        String sql = """
            UPDATE contacts
            SET nom = ?, prenom = ?, email = ?, telephone = ?
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getTelephone());
            ps.setInt(5, c.getId());
            ps.setInt(6, c.getUserId());

            ps.executeUpdate();
        }
    }

    // Supprime un contact appartenant à l'utilisateur indiqué
    public void delete(int id, int userId) throws Exception {
        String sql = """
            DELETE FROM contacts
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);

            ps.executeUpdate();
        }
    }

    // Recherche dans le nom, le prénom ou l'email
    public List<Contact> search(int userId, String text)
            throws Exception {

        List<Contact> contacts = new ArrayList<>();

        String sql = """
            SELECT id, user_id, nom, prenom, email, telephone
            FROM contacts
            WHERE user_id = ?
              AND (
                  nom LIKE ?
                  OR prenom LIKE ?
                  OR email LIKE ?
              )
            ORDER BY nom, prenom
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String recherche = "%" + text + "%";

            ps.setInt(1, userId);
            ps.setString(2, recherche);
            ps.setString(3, recherche);
            ps.setString(4, recherche);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contacts.add(new Contact(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("telephone")
                    ));
                }
            }
        }

        return contacts;
    }
}