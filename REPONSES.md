Q1. Pourquoi la colonne user_id est-elle indispensable pour respecter la règle « un utilisateur ne voit
que ses contacts » ?

Lorsqu'un utilisateur se connecte, l'application récupère son identifiant et filtre les contacts avec une requête SQL :

SELECT * FROM contacts
WHERE user_id = ?;

Ce qui permet d'afficher que les contacts de l'utilisateur avec le id qui remplace "?".


Q2. Que se passerait-il, sans ON DELETE CASCADE , si on supprimait un utilisateur qui a des contacts ?

Sans le "ON DELETE CASCADE", l'utilisateur sera supprimé sans supprimé sa liste de contacts.