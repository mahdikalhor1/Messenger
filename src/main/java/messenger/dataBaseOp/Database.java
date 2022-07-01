package messenger.dataBaseOp;


import java.sql.*;

public class Database {

    private static Database database;

    private final ChannelOp channelOp;
    private final FriendRequestOp friendRequestOp;
    private final MessageOp messageOp;
    private final PrivateChatOp privateChatOp;
    private final ServerOp serverOp;
    private final UserOp userOp;

    private Database()
    {
        try
        {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/Discord" ,
                    "postgres" , "postgres");

            channelOp = new ChannelOp(connection);
            friendRequestOp = new FriendRequestOp(connection);
            messageOp = new MessageOp(connection);
            serverOp = new ServerOp(connection);
            userOp = new UserOp(connection);
            privateChatOp = new PrivateChatOp(connection);

            System.out.println("database connected successfully.");
        }
        catch (SQLException | ClassNotFoundException e)
        {
            System.out.println("connection to database failed!");
            throw new RuntimeException(e);
        }
    }

    public static Database getDatabase()
    {
        if(null == database)
        {
            database = new Database();
        }

        return database;
    }

    public ChannelOp getChannelOp() {
        return channelOp;
    }

    public FriendRequestOp getFriendRequestOp() {
        return friendRequestOp;
    }

    public MessageOp getMessageOp() {
        return messageOp;
    }

    public PrivateChatOp getPrivateChatOp() {
        return privateChatOp;
    }

    public ServerOp getServerOp() {
        return serverOp;
    }

    public UserOp getUserOp() {
        return userOp;
    }
}
