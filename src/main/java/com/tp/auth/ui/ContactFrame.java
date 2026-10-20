
package com.tp.auth.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.tp.auth.dao.ContactDao;
import com.tp.auth.model.Contact;
import com.tp.auth.model.User;

public class ContactFrame extends JFrame {

    private final User user;
    private final ContactDao dao = new ContactDao();

    // Tableau non modifiable
    // La colonne ID est utilisée en interne et masquée à l'écran
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Nom", "Prénom", "Email", "Téléphone"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public ContactFrame(User user) {
        super("Gestion des contacts");

        if (user == null) {
            throw new IllegalArgumentException(
                "Un utilisateur connecté est obligatoire."
            );
        }

        this.user = user;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 400);
        setLocationRelativeTo(null);

        // Haut : utilisateur connecté et bouton de déconnexion
        JPanel haut = new JPanel(new BorderLayout(10, 10));

        JLabel bienvenue = new JLabel(
            "Connecté en tant que " + user.getUsername()
        );

        JButton deconnexionBtn = new JButton("Se déconnecter");

        haut.add(bienvenue, BorderLayout.WEST);
        haut.add(deconnexionBtn, BorderLayout.EAST);

        deconnexionBtn.addActionListener(e -> deconnecter());

        // Centre : tableau des contacts
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Masquer la colonne ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);

        // Bas : boutons d'action
        JPanel bas = new JPanel(new FlowLayout());

        JButton ajouterBtn = new JButton("Ajouter");
        JButton modifierBtn = new JButton("Modifier");
        JButton supprimerBtn = new JButton("Supprimer");

        bas.add(ajouterBtn);
        bas.add(modifierBtn);
        bas.add(supprimerBtn);

        ajouterBtn.addActionListener(e -> ajouter());
        modifierBtn.addActionListener(e -> modifier());
        supprimerBtn.addActionListener(e -> supprimer());

        // Assemblage de la fenêtre
        add(haut, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);

        // Chargement automatique des contacts
        chargerContacts();
    }

    // Charge uniquement les contacts de l'utilisateur connecté
    private void chargerContacts() {
        try {
            List<Contact> contacts = dao.findByUser(user.getId());

            model.setRowCount(0);

            for (Contact c : contacts) {
                model.addRow(new Object[]{
                    c.getId(),
                    c.getNom(),
                    c.getPrenom(),
                    c.getEmail(),
                    c.getTelephone()
                });
            }

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    // Ouvre un formulaire vide pour ajouter un contact
    private void ajouter() {
        ContactDialog dialog = new ContactDialog(
            this,
            user,
            null,
            this::chargerContacts
        );

        dialog.setVisible(true);
    }

    // Ouvre le formulaire prérempli pour modifier un contact
    private void modifier() {
        int ligne = table.getSelectedRow();

        if (ligne == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Sélectionnez un contact."
            );
            return;
        }

        try {
            Contact contact = obtenirContact(ligne);

            ContactDialog dialog = new ContactDialog(
                this,
                user,
                contact,
                this::chargerContacts
            );

            dialog.setVisible(true);

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    // Supprime le contact sélectionné après confirmation
    private void supprimer() {
        int ligne = table.getSelectedRow();

        if (ligne == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Sélectionnez un contact à supprimer."
            );
            return;
        }

        int choix = JOptionPane.showConfirmDialog(
            this,
            "Voulez-vous vraiment supprimer ce contact ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );

        if (choix != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Contact contact = obtenirContact(ligne);

            dao.delete(contact.getId(), user.getId());

            chargerContacts();

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    // Retrouve le contact sélectionné grâce à son identifiant
    private Contact obtenirContact(int ligne) throws Exception {
        int id = (int) model.getValueAt(ligne, 0);

        List<Contact> contacts = dao.findByUser(user.getId());

        for (Contact contact : contacts) {
            if (contact.getId() == id) {
                return contact;
            }
        }

        throw new Exception("Contact introuvable.");
    }

    // Ferme les contacts et retourne à la connexion
    private void deconnecter() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Affiche les messages d'erreur
    private void afficherErreur(Exception e) {
        String message = e.getMessage();

        JOptionPane.showMessageDialog(
            this,
            message == null
                ? "Une erreur est survenue."
                : message,
            "Erreur",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
