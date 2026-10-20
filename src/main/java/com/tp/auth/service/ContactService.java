
package com.tp.auth.service;

import com.tp.auth.dao.ContactDao;
import com.tp.auth.model.Contact;

public class ContactService {

    private final ContactDao dao = new ContactDao();

    // Valide les données avant d'ajouter un contact
    public void insert(Contact c) throws Exception {
        valider(c);
        dao.insert(c);
    }

    // Valide les données avant de modifier un contact
    public void update(Contact c) throws Exception {
        valider(c);
        dao.update(c);
    }

    // Vérifie les règles de validation
    private void valider(Contact c) {
        if (c == null) {
            throw new IllegalArgumentException("Contact invalide");
        }

        // Nom obligatoire
        if (c.getNom() == null || c.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        // Prénom obligatoire
        if (c.getPrenom() == null || c.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        // Email facultatif, mais doit contenir @ et .
        String email = c.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            email = email.trim();

            if (!email.contains("@") || !email.contains(".")) {
                throw new IllegalArgumentException("Email invalide");
            }
        }

        // Téléphone facultatif : chiffres, espaces,
        // et un signe + uniquement en première position
        String telephone = c.getTelephone();
        if (telephone != null && !telephone.trim().isEmpty()) {
            telephone = telephone.trim();

            if (!telephone.matches("\\+?[0-9 ]+")) {
                throw new IllegalArgumentException("Téléphone invalide");
            }
        }
    }
}
