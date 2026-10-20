package com.tp.auth.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;

import com.tp.auth.model.Contact;
import com.tp.auth.model.User;
import com.tp.auth.service.ContactService;

public class ContactDialog extends JDialog {

    private final User user;
    private final Contact contact;
    private final ContactService service = new ContactService();
    private final Runnable onSuccess;

    private final JTextField nomField = new JTextField(20);
    private final JTextField prenomField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField telephoneField = new JTextField(20);

    public ContactDialog(JFrame parent, User user,
                         Contact contact, Runnable onSuccess) {

        super(parent, contact == null
                ? "Ajouter un contact"
                : "Modifier un contact", true);

        this.user = user;
        this.contact = contact;
        this.onSuccess = onSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Formulaire
        JPanel formulaire = new JPanel(new GridLayout(4, 2, 8, 8));
        formulaire.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 10, 15)
        );

        formulaire.add(new JLabel("Nom :"));
        formulaire.add(nomField);

        formulaire.add(new JLabel("Prénom :"));
        formulaire.add(prenomField);

        formulaire.add(new JLabel("Email :"));
        formulaire.add(emailField);

        formulaire.add(new JLabel("Téléphone :"));
        formulaire.add(telephoneField);

        // Boutons
        JButton enregistrerBtn = new JButton("Enregistrer");
        JButton annulerBtn = new JButton("Annuler");

        JPanel boutons = new JPanel();
        boutons.add(enregistrerBtn);
        boutons.add(annulerBtn);

        add(formulaire, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);

        enregistrerBtn.addActionListener(e -> enregistrer());
        annulerBtn.addActionListener(e -> dispose());

        // Si un contact existe, préremplir le formulaire
        if (contact != null) {
            nomField.setText(contact.getNom());
            prenomField.setText(contact.getPrenom());
            emailField.setText(contact.getEmail());
            telephoneField.setText(contact.getTelephone());
        }

        getRootPane().setDefaultButton(enregistrerBtn);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void enregistrer() {
        try {
            Contact nouveauContact = new Contact(
                contact == null ? 0 : contact.getId(),
                user.getId(),
                nomField.getText().trim(),
                prenomField.getText().trim(),
                emailField.getText().trim(),
                telephoneField.getText().trim()
            );

            if (contact == null) {
                // Ajout
                service.insert(nouveauContact);
            } else {
                // Modification
                service.update(nouveauContact);
            }

            // Succès : fermer et rafraîchir le tableau
            dispose();

            if (onSuccess != null) {
                onSuccess.run();
            }

        } catch (Exception e) {
            // En cas d'erreur, le formulaire reste ouvert
            JOptionPane.showMessageDialog(
                this,
                e.getMessage() == null
                    ? "Une erreur est survenue."
                    : e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}