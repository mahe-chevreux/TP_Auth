Q1. Pourquoi la colonne user_id est-elle indispensable pour respecter la règle « un utilisateur ne voit
que ses contacts » ?

Lorsqu'un utilisateur se connecte, l'application récupère son identifiant et filtre les contacts avec une requête SQL :

SELECT * FROM contacts
WHERE user_id = ?;

Ce qui permet d'afficher que les contacts de l'utilisateur avec le id qui remplace "?".


Q2. Que se passerait-il, sans ON DELETE CASCADE , si on supprimait un utilisateur qui a des contacts ?

Sans le "ON DELETE CASCADE", l'utilisateur sera supprimé sans supprimé sa liste de contacts.

Q3. Pourquoi update et delete doivent-ils aussi vérifier user_id dans leur WHERE , alors que l' id
du contact suffit à l'identifier ? Pensez à un utilisateur malveillant qui connaîtrait l' id d'un contact
d'un autre.

Ajouter "user_id" dans le "WHERE" constitue une protection contre les accès non autorisés. 
L'identifiant de l'utilisateur doit provenir de sa session authentifiée, et non d'une valeur librement choisie par le client.


