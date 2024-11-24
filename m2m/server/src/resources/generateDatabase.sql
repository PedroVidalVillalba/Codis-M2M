-- Nota: para crear un usuario hace falta un usuario con permisos de creación de usuarios, por lo que posiblemente se necesiten permisos de administrador. Probar desde una terminal con
-- sudo -i -u postgres
-- psql
-- CREATE ROLE m2m LOGIN CREATEDB PASSWORD 'm2m';
-- \q

-- Ahora hay que modificar el fichero de gestión de usuarios para que deje autenticarse con contraseña.
-- sudo nano /etc/postgresql/14/main/pg_hba.conf
-- Hay que sustituir en la línea indicada peer por md5
-- # "local" is for Unix domain socket connections only
-- local   all             all                                     peer

-- Reiniciamos el servicio y ya podemos iniciar sesión con m2m
-- sudo systemctl restart postgresql
-- psql -U m2m -d postgres
-- Podemos usar el comando SELECT current_user, session_user; para comprobar que estamos con el usuario m2m
-- CREATE DATABASE m2mdb;
-- \l para ver las bases de datos creadas
-- \q

-- Salimos y ahora ya entramos a la base de datos recién creada
-- psql -U m2m -d m2mdb
-- Para la generación de la base de datos, se puede copiar y pegar, o ejecutar el archivo mediante
-- \i /ruta_al_archivo/generateDatabase.sql
-- \i /ruta_al_archivo/insertUsersFriends.sql
-- Para ver que efectivamente se han creado:
-- \dt
-- SELECT * FROM friends/users

-- Configuración de IntelliJ:
-- A la derecha hay una pestaña con bases de datos. Click en el +, Data source, Postresql
-- Host: localhost, port:5432, user: m2m, password: m2m, database: m2mdb.
-- Test connection para verificar que está todo bien



CREATE TABLE Users (
    username VARCHAR(30) PRIMARY KEY,
    password CHAR(256)
);

CREATE TABLE Friends (
    sender VARCHAR REFERENCES Users(username),
    receiver VARCHAR REFERENCES Users(username),
    state VARCHAR(10),
    PRIMARY KEY (sender, receiver)
);
