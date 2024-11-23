INSERT INTO Users (username, password) VALUES
('alice', '123456'),
('bob', '123456'),
('carol', '123456'),
('dave', '123456');

-- Estado "pending" para solicitudes no aceptadas todavía
INSERT INTO Friends (sender, receiver, state) VALUES
('alice', 'bob', 'pending'),
('carol', 'alice', 'pending');

-- Estado "accepted" para amistades confirmadas
INSERT INTO Friends (sender, receiver, state) VALUES
('bob', 'carol', 'accepted'),
('alice', 'dave', 'accepted'),
('dave', 'bob', 'accepted');
