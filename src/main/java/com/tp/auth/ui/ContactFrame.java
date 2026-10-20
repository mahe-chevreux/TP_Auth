
package com.tp.auth.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.tp.auth.dao.ContactDao;
import com.tp.auth.model.Contact;
import com.tp.auth.model.User;
import com.tp.auth.service.ContactService;

public class ContactFrame extends JFrame {

    private final User user;
    private final ContactDao dao = new ContactDao();
    private final ContactService service = new ContactService();

    // Tableau non modifiable par double-clic
    // La colonne ID est conservée techniquement, mais masquée.
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
        setSize(700, 400);
        setLocationRelativeTo(null);

        // Haut : utilisateur connecté et déconnexion
        JPanel haut = new JPanel(new BorderLayout());

        JLabel bienvenue = new JLabel(
            "Connecté en tant que " + user.getUsername()
        );

        JButton deconnexionBtn = new JButton("Se déconnecter");

        haut.add(bienvenue, BorderLayout.WEST);
        haut.add(deconnexionBtn, BorderLayout.EAST);

        deconnexionBtn.addActionListener(e -> deconnecter());

        // Centre : tableau des contacts
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Masquer la colonne ID sans la supprimer du modèle
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

        // Chargement initial des contacts
        chargerContacts();
    }

    // Charger uniquement les contacts de l'utilisateur connecté
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

    // Ajouter un contact
    private void ajouter() {
        JTextField nom = new JTextField();
        JTextField prenom = new JTextField();
        JTextField email = new JTextField();
        JTextField telephone = new JTextField();

        Object[] champs = {
            "Nom :", nom,
            "Prénom :", prenom,
            "Email :", email,
            "Téléphone :", telephone
        };

        int choix = JOptionPane.showConfirmDialog(
            this,
            champs,
            "Ajouter un contact",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (choix != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Contact contact = new Contact(
                0,
                user.getId(),
                nom.getText().trim(),
                prenom.getText().trim(),
                email.getText().trim(),
                telephone.getText().trim()
            );

            service.insert(contact);
            chargerContacts();

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    // Modifier un contact sélectionné
    private void modifier() {
        int ligne = table.getSelectedRow();

        if (ligne == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Sélectionnez un contact à modifier."
            );
            return;
        }

        try {
            Contact contact = obtenirContact(ligne);

            JTextField nom = new JTextField(contact.getNom());
            JTextField prenom = new JTextField(contact.getPrenom());
            JTextField email = new JTextField(
                contact.getEmail() == null ? "" : contact.getEmail()
            );
            JTextField telephone = new JTextField(
                contact.getTelephone() == null
                    ? ""
                    : contact.getTelephone()
            );

            Object[] champs = {
                "Nom :", nom,
                "Prénom :", prenom,
                "Email :", email,
                "Téléphone :", telephone
            };

            int choix = JOptionPane.showConfirmDialog(
                this,
                champs,
                "Modifier un contact",
                JOptionPane.OK_CANCEL_OPTION
            );

            if (choix != JOptionPane.OK_OPTION) {
                return;
            }

            contact.setNom(nom.getText().trim());
            contact.setPrenom(prenom.getText().trim());
            contact.setEmail(email.getText().trim());
            contact.setTelephone(telephone.getText().trim());

            service.update(contact);
            chargerContacts();

        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    // Supprimer un contact sélectionné
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

    // Retrouver le contact correspondant à la ligne sélectionnée
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

    // Déconnexion : fermer cette fenêtre et revenir à la connexion
    private void deconnecter() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Afficher les erreurs
    private void afficherErreur(Exception e) {
        String message = e.getMessage();

        JOptionPane.showMessageDialog(
            this,
            message == null ? "Une erreur est survenue." : message,
            "Erreur",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
