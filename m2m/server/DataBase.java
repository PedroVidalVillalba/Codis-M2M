package m2m.server;

public class DataBase {
    private static DataBase currentDB;
    private static Connection connection;
    private HashMap<String, String> users;
    private HashMap<String, List<String>> friends;

    public DataBase() {
        users = new HashMap<>();
        friends = new HashMap<>();

        Properties configuration = new Properties();
        FileInputStream configurationFile;

        try {
            configuration.load(configurationFile);
            configurationFile.close();

            Properties user = new Properties();

            String manager = configuration.getProperty("manager");
            this.connection = DriverManager.getConnection("jdbc:" + manager + "://" +
                            configuration.getProperty("server") + ":" +
                            configuration.getProperty("port") + "/" +
                            configuration.getProperty("dataBase"),
                    user);
        } catch(IOException | SQLException exception) {
            System.err.println(exception.getMessage());
        }

        readDB();

    }

    private void readDB(){
        String query = "SELECT username, password FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(resulSet.getString("username"), resultSet.getString("password"));
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        }
        for(String user: users.entrySet()){
            List<String> friends = new List<>;
            query = "SELECT sender, receiver FROM friends WHERE state = accepted AND (sender = " + user + " OR receiver = " + user + ")";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    if(resultSet.getString("sender") == user) {
                        friends.add(resultSet.getString("receiver"));
                    } else {
                        friends.add(resultSet.getString("sender"))
                    }
                }
                this.friends.add(user, friends);
            } catch (SQLException exception) {
                System.err.println(exception.getMessage());
            }
        }
    }

    public static DataBase getCurrentDB() {
        if (DataBase.currentDB == null) {
            DataBase.currentDB = new DataBase();
        }
        return DataBase.currentDB;
    }

    public static void closeCurrentDB() {
        try {
            if (currentDB != null && currentDB.connection != null) {
                currentDB.connection.close();
            }
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
