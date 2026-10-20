package com.tp.auth.ui;

import java.awt.*;
import javax.swing.*;
import com.tp.auth.model.User;
import com.tp.auth.service.AuthService;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel messageLabel = new JLabel(" ");
    private final AuthService auth = new AuthService();

    public LoginFrame() {
        super("Authentification");

        // 1. Le formulaire : 2 lignes, 2 colonnes
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 10));

        form.add(new JLabel("Nom d'utilisateur :"));
        form.add(usernameField);

        form.add(new JLabel("Mot de passe :"));
        form.add(passwordField);

        form.add(new JLabel("Rôle :"));
        form.add(new JLabel("Utilisateur"));

        // 2. Les boutons et leurs actions
        JButton loginBtn = new JButton("Se connecter");
        JButton registerBtn = new JButton("S'inscrire");

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());

        getRootPane().setDefaultButton(loginBtn);

        JPanel buttons = new JPanel();
        buttons.add(loginBtn);
        buttons.add(registerBtn);

        // 3. Assemblage : formulaire en haut, boutons au centre, message en bas
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(
            BorderFactory.createEmptyBorder(10, 15, 15, 15)
        );

        content.add(form, BorderLayout.NORTH);
        content.add(buttons, BorderLayout.CENTER);
        content.add(messageLabel, BorderLayout.SOUTH);

        setContentPane(content);
        pack(); // taille adaptée au contenu

        setLocationRelativeTo(null); // fenêtre centrée
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void login() {
        try {
            User user = auth.login(
                usernameField.getText().trim(),
                new String(passwordField.getPassword())
            );

            if (user == null) {
                showMessage(
                    "Identifiants invalides.",
                    Color.RED
                );
            } else {
                showMessage(
                    "Bienvenue " + user.getUsername() + " !",
                    new Color(0, 128, 0)
                );
            }

        } catch (Exception e) {
            showMessage(
                "Erreur : " + e.getMessage(),
                Color.RED
            );
        }
    }

    private void register() {
        try {
            String error = auth.register(
                usernameField.getText().trim(),
                new String(passwordField.getPassword())
            );

            if (error == null) {
                showMessage(
                    "Compte créé avec succès !",
                    new Color(0, 128, 0)
                );
                passwordField.setText("");
            } else {
                showMessage(error, Color.RED);
            }

        } catch (Exception e) {
            showMessage(
                "Erreur : " + e.getMessage(),
                Color.RED
            );
        }
    }

    private void showMessage(String text, Color color) {
        messageLabel.setForeground(color);
        messageLabel.setText(text);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new LoginFrame().setVisible(true)
        );
    }
}