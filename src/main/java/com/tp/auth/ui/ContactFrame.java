
package com.tp.auth.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.tp.auth.dao.ContactDao;
import com.tp.auth.model.Contact;
import com.tp.auth.model.User;

public class ContactFrame extends JFrame {

    private final User user;
    private final ContactDao dao = new ContactDao();

    // Modèle du tableau : ID masqué, cellules non modifiables
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Nom", "Prénom", "Email", "Téléphone"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    // Champ de recherche
    private final JTextField rechercheField = new JTextField(25);

    public ContactFrame(User user) {
        super("Gestion des contacts");

        if (user == null) {
            throw new IllegalArgumentException(
                "Un utilisateur connecté est obligatoire."
            );
        }

        this.user = user;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 450);
        setLocationRelativeTo(null);

        // Partie supérieure : utilisateur connecté et déconnexion
        JPanel haut = new JPanel(new BorderLayout(10, 10));

        JLabel bienvenue = new JLabel(
            "Connecté en tant que " + user.getUsername()
        );

        JButton deconnexionBtn = new JButton("Se déconnecter");

        haut.add(bienvenue, BorderLayout.WEST);
        haut.add(deconnexionBtn, BorderLayout.EAST);

        haut.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        deconnexionBtn.addActionListener(e -> deconnecter());

        // Tableau des contacts
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Masquer la colonne ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);

        // Recherche au-dessus du tableau
        JPanel recherchePanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        recherchePanel.add(new JLabel("Rechercher :"));
        recherchePanel.add(rechercheField);

        // Actualiser les résultats à chaque modification du texte
        rechercheField.getDocument().addDocumentListener(
            new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    chargerContacts();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    chargerContacts();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    chargerContacts();
                }
            }
        );

        // Zone centrale : recherche + tableau
        JPanel centre = new JPanel(new BorderLayout());
        centre.add(recherchePanel, BorderLayout.NORTH);
        centre.add(scrollPane, BorderLayout.CENTER);

        // Boutons du bas
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
        add(centre, BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);

        // Chargement initial
        chargerContacts();
    }

    // Afficher les contacts de l'utilisateur connecté
    // et filtrer selon la recherche
    private void chargerContacts() {
        try {
            List<Contact> contacts = dao.findByUser(user.getId());

            String recherche = rechercheField.getText()
                .trim()
                .toLowerCase();

            model.setRowCount(0);

            for (Contact c : contacts) {

                String nom = c.getNom() == null
                    ? "" : c.getNom().toLowerCase();

                String prenom = c.getPrenom() == null
                    ? "" : c.getPrenom().toLowerCase();

                String email = c.getEmail() == null
                    ? "" : c.getEmail().toLowerCase();

                // Champ vide : tous les contacts sont affichés
                // Sinon : recherche dans le nom, prénom ou email
                if (recherche.isEmpty()
                    || nom.contains(recherche)
                    || prenom.contains(recherche)
                    || email.contains(recherche)) {

                    model.addRow(new Object[]{
                        c.getId(),
                        c.getNom(),
                        c.getPrenom(),
                        c.getEmail(),
                        c.getTelephone()
                    });
                }
            }

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    // Ajouter un contact avec un formulaire vide
    private void ajouter() {
        ContactDialog dialog = new ContactDialog(
            this,
            user,
            null,
            this::chargerContacts
        );

        dialog.setVisible(true);
    }

    // Modifier le contact sélectionné
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

    // Supprimer un contact après confirmation
    private void supprimer() {
        int ligne = table.getSelectedRow();

        if (ligne == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Sélectionnez un contact à supprimer."
            );
            return;
        }

        try {
            Contact contact = obtenirContact(ligne);

            // Confirmation personnalisée
            int choix = JOptionPane.showConfirmDialog(
                this,
                "Supprimer "
                    + contact.getNom() + " "
                    + contact.getPrenom() + " ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            // Ne rien supprimer si l'utilisateur choisit Non
            if (choix != JOptionPane.YES_OPTION) {
                return;
            }

            // Suppression limitée à l'utilisateur connecté
            dao.delete(contact.getId(), user.getId());

            // Actualiser le tableau après suppression
            chargerContacts();

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    // Retrouver le contact sélectionné grâce à son ID
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

    // Déconnexion
    private void deconnecter() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Affichage des erreurs
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
