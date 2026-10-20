package com.tp.auth.service;

import org.mindrot.jbcrypt.BCrypt;
import com.tp.auth.dao.UserDao;
import com.tp.auth.model.User;

public class AuthService {

    private final UserDao dao = new UserDao();

    // Retourne null si l'inscription réussit, sinon le message d'erreur
    public String register(String username, String password) throws Exception {
        if (username.isBlank())
            return "Nom d'utilisateur obligatoire";

        if (password.length() < 8)
            return "Mot de passe : 8 caractères minimum";

        if (dao.existsByUsername(username))
            return "Ce nom d'utilisateur existe déjà";

        dao.save(username, password);
        return null;
    }

    // Retourne l'utilisateur si les identifiants sont bons, sinon null
    public User login(String username, String password) throws Exception {
        User user = dao.findByUsername(username);

        if (user == null)
            return null;

        try {
            return BCrypt.checkpw(password, user.getPasswordHash()) ? user : null;
        } catch (IllegalArgumentException e) {
            return null; // Le mot de passe en base n'est pas un hash BCrypt
        }
    }
}