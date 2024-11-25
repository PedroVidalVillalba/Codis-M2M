package m2m.server;

import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class DataBase {
    private static DataBase currentDB;
    private static Connection connection;

    public static final String CONFIGURATION_FILE = "/database.properties";

    public DataBase() throws Exception {
        Properties configuration = new Properties();

        /* Cargar el archivo de configuración */
        try (InputStream configurationFile = DataBase.class.getResourceAsStream(CONFIGURATION_FILE)) {
            if (configurationFile == null) {
                throw new IOException("Fichero de configuración de la base de datos no encontrado: " + CONFIGURATION_FILE);
            }
            configuration.load(configurationFile);
        }

        Properties user = new Properties();
        user.setProperty("user", configuration.getProperty("user"));
        user.setProperty("password", configuration.getProperty("password"));

        String manager = configuration.getProperty("manager");
        connection = DriverManager.getConnection("jdbc:" + manager + "://" +
                        configuration.getProperty("server") + ":" +
                        configuration.getProperty("port") + "/" +
                        configuration.getProperty("dataBase"),
                        user);
    }

    public static DataBase getCurrentDB() throws Exception {
        if (DataBase.currentDB == null) {
            DataBase.currentDB = new DataBase();
        }
        return DataBase.currentDB;
    }

    public static void closeCurrentDB() throws SQLException {
        if (currentDB != null && connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Lectura completa de la tabla Usuarios.
     * @return La lista de nombres de usuarios.
     */
    public ArrayList<String> getUsers() throws SQLException {
        ArrayList<String> users = new ArrayList<>();

        @Language("SQL")
        String query = "SELECT username FROM users";

        try (PreparedStatement preparedStatement = prepareQuery(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                users.add(resultSet.getString("username"));
            }
        }

        return users;
    }

    /**
     * Lectura parcial de la tabla Amigos.
     * @param user Usuario al que se le buscan todos sus amigos.
     * @return La lista de amigos de {@code user}.
     */
    public ArrayList<String> getFriends(String user) throws SQLException {
        ArrayList<String> user_friends = new ArrayList<>();

        @Language("SQL")
        String query = "SELECT sender, receiver " +
                "FROM friends " +
                "WHERE state = 'accepted' AND (sender = ? OR receiver = ?)";

        try (PreparedStatement preparedStatement = prepareQuery(query, user, user);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                // Si el usuario es el que envió la solicitud, su amigo es el que la recibió, o viceversa
                if (resultSet.getString("sender").equals(user)) {
                    user_friends.add(resultSet.getString("receiver"));
                } else {
                    user_friends.add(resultSet.getString("sender"));
                }
            }
        }

        return user_friends;
    }


    // Registro de un nuevo usuario
    public void registerUser(String username, byte[] password) throws SQLException {
        ArrayList<String> users = getUsers();
        // Comprobación de que no existe un usuario registrado con el mismo nombre
        if (users.contains(username)) {
            throw new SQLException("El usuario " + username + " ya está registrado");
        }

        @Language("SQL")
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setBytes(2, password);
            preparedStatement.executeUpdate();
        }
    }

    // Inicio de sesión de un usuario
    public void loginUser(String username, byte[] password) throws SQLException {
        @Language("SQL")
        String query = "SELECT password FROM users WHERE username = ?";

        try (PreparedStatement preparedStatement = prepareQuery(query, username);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) { /* Asegurarse de que el resultado tiene por lo menos una fila */
                if (!Arrays.equals(password, resultSet.getBytes("password"))) {
                    throw new SQLException("La contraseña del usuario " + username + " es incorrecta");
                }
            } else {
                throw new SQLException("El usuario " + username + " no está registrado");
            }
        }
    }

    // Envío de una solicitud de amistad
    public void friendRequest(String senderUsername, String receiverUsername) throws SQLException {
        ArrayList<String> users = getUsers();
        // Comprobación de que existe un usuario registrado con el mismo nombre
        if(!users.contains(receiverUsername)) {
            throw new SQLException("El usuario " + receiverUsername + " no está registrado");
        }

        @Language("SQL")
        String query = "INSERT INTO friends (sender, receiver, state) VALUES (?, ?, 'pending')";

        executeUpdate(query, senderUsername, receiverUsername);
    }

    // Aceptación de una solicitud de amistad
    public void friendAccept(String senderUsername, String receiverUsername) throws SQLException {
        ArrayList<String> users = getUsers();
        // Comprobación de que existe un usuario registrado con el mismo nombre
        if(!users.contains(receiverUsername)) {
            throw new SQLException("El usuario " + receiverUsername + " no está registrado");
        }

        @Language("SQL")
        String query = "UPDATE friends SET state = 'accepted' " +
                "WHERE sender = ? AND receiver = ? AND state = 'pending'";

        if (executeUpdate(query, senderUsername, receiverUsername) == 0) {
            throw new SQLException("No existía ninguna solicitud de amistad pendiente de " + senderUsername + " a " + receiverUsername);
        }
    }

    // Rechazo de una solicitud de amistad
    public void friendReject(String senderUsername, String receiverUsername) throws SQLException {
        ArrayList<String> users = getUsers();
        // Comprobación de que existe un usuario registrado con el mismo nombre
        if(!users.contains(receiverUsername)) {
            throw new SQLException("El usuario " + receiverUsername + " no está registrado");
        }

        @Language("SQL")
        String query = "DELETE FROM friends " +
                "WHERE sender = ? AND receiver = ? AND state = 'pending'";

        if (executeUpdate(query, senderUsername, receiverUsername) == 0) {
            throw new SQLException("No existía ninguna solicitud de amistad pendiente de " + senderUsername + " a " + receiverUsername);
        }
    }

    private int executeUpdate(String query, String... missingFields) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < missingFields.length; i++) {
                preparedStatement.setString(i + 1, missingFields[i]);
            }
            return preparedStatement.executeUpdate();
        }
    }

    private PreparedStatement prepareQuery(String query, String... missingFields) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < missingFields.length; i++) {
            preparedStatement.setString(i + 1, missingFields[i]);
        }
        return preparedStatement;
    }

}
