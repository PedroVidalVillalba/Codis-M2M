package m2m.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class DataBase {
    private static DataBase currentDB;
    private static Connection connection;


    public DataBase() {
        Properties configuration = new Properties();

        try {
            FileInputStream configurationFile = new FileInputStream("m2m/server/src/resources/DataBase.properties");;

            configuration.load(configurationFile);
            configurationFile.close();

            Properties user = new Properties();
            user.setProperty("user", configuration.getProperty("user"));
            user.setProperty("password", configuration.getProperty("password"));

            String manager = configuration.getProperty("manager");
            connection = DriverManager.getConnection("jdbc:" + manager + "://" +
                            configuration.getProperty("server") + ":" +
                            configuration.getProperty("port") + "/" +
                            configuration.getProperty("dataBase"),
                    user);
        } catch(IOException | SQLException exception) {
            System.err.println(exception.getMessage());
        }
    }

    /**
     * Lectura completa de la tabla Usuarios.
     * @return La lista de nombres de usuarios.
     */
    public ArrayList<String> getUsers() {
        ArrayList<String> users = new ArrayList<>();
        String query = "SELECT username FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(resultSet.getString("username"));
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        }

        return users;
    }

    /**
     * Lectura parcial de la tabla Amigos.
     * @param user Usuario al que se le buscan todos sus amigos.
     * @return La lista de amigos de {@code user}.
     */
    public ArrayList<String> getFriends(String user) {
        ArrayList<String> user_friends = new ArrayList<>();
        String query = "SELECT sender, receiver " +
                "FROM friends " +
                "WHERE state = 'accepted' AND (sender = ? OR receiver = ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, user);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                // Si el usuario es el que envió la solicitud, su amigo es el que la recibió, o viceversa
                if(resultSet.getString("sender").equals(user)) {
                    user_friends.add(resultSet.getString("receiver"));
                } else {
                    user_friends.add(resultSet.getString("sender"));
                }
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        }

        return user_friends;
    }


    public static DataBase getCurrentDB() {
        if (DataBase.currentDB == null) {
            DataBase.currentDB = new DataBase();
        }
        return DataBase.currentDB;
    }

    public static void closeCurrentDB() {
        try {
            if (currentDB != null && connection != null) {
                connection.close();
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        }
    }





    // Registro de un nuevo usuario
    public boolean registerUser(String username, String password) {
        ArrayList<String> users = getUsers();
        // Comprobación de que no existe un usuario registrado con el mismo nombre
        if(users.contains(username)) {
            return false;
        } else {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException exception) {
                System.err.println(exception.getMessage());
                return false;
            }
        }
    }

    // Inicio de sesión de un usuario
    public boolean loginUser(String username, String password) {
        ArrayList<String> users = getUsers();
        // Comprobación de que existe un usuario registrado con el mismo nombre
        if(!users.contains(username)) {
            return false;
        } else {
            String query = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                return password.equals(resultSet.getString("password"));
            } catch (SQLException exception) {
                System.err.println(exception.getMessage());
                return false;
            }
        }
    }

    // Envío de una solicitud de amistad
    public boolean friendRequest(String senderUsername, String receiverUsername) {
        ArrayList<String> users = getUsers();
        // Comprobación de que existe un usuario registrado con el mismo nombre
        if(!users.contains(receiverUsername)) {
            return false;
        } else {
            String query = "INSERT INTO friends (sender, receiver, state) VALUES (?, ?, 'pending')";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, senderUsername);
                preparedStatement.setString(2, receiverUsername);
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException exception) {
                System.err.println(exception.getMessage());
                return false;
            }
        }
    }

    // Aceptación de una solicitud de amistad
    public boolean friendAccept(String senderUsername, String receiverUsername) {
        ArrayList<String> users = getUsers();
        // Comprobación de que existe un usuario registrado con el mismo nombre
        if(!users.contains(receiverUsername)) {
            return false;
        } else {
            String query = "UPDATE friends SET state = 'accepted' " +
                    "WHERE sender = ? AND receiver = ? AND state = 'pending'";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, senderUsername);
                preparedStatement.setString(2, receiverUsername);

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException exception) {
                System.err.println(exception.getMessage());
                return false;
            }
        }
    }

    // Rechazo de una solicitud de amistad
    public boolean friendReject(String senderUsername, String receiverUsername) {
        ArrayList<String> users = getUsers();
        // Comprobación de que existe un usuario registrado con el mismo nombre
        if(!users.contains(receiverUsername)) {
            return false;
        } else {
            String query = "DELETE FROM friends " +
                    "WHERE sender = ? AND receiver = ? AND state = 'pending'";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, senderUsername);
                preparedStatement.setString(2, receiverUsername);

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException exception) {
                System.err.println(exception.getMessage());
                return false;
            }
        }
    }



}
