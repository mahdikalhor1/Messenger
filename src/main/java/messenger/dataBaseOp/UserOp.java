package messenger.dataBaseOp;

import model.exception.ConfigNotFoundException;
import model.exception.UserNotFoundException;
import model.user.ServerIDs;
import model.user.User;
import model.user.UserStatus;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;



public class UserOp extends Op{


    public UserOp(Connection connection){
        super(connection);

    }

    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : findByConfigChannel
     * @author Arman sagharchi
     */
    private User findByConfigUser(String config, String columnName)
            throws SQLException, ClassNotFoundException, IOException, ConfigNotFoundException{
        ResultSet resultSet = findByConfig(config, columnName, "users");
        return createUserFromData(resultSet);
    }

    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : findById
     * @author Arman sagharchi
     */
    public User findById(String id) throws SQLException, IOException,
            ClassNotFoundException, ConfigNotFoundException{
        return findByConfigUser(id, "user_id");
    }

    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : updateChannel
     * @author Arman sagharchi
     */
    public void updateUserProfileImage(byte[] newImage ,String id)
            throws ConfigNotFoundException, SQLException{

        updateImage(newImage, id, "profile_image", "user",
                "user_id", "users");
    }

    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : insertChannel
     * @author Arman sagharchi
     */
    public void insertUser(String id, String name, String password, String email, String phoneNumber)
    throws SQLException, IOException{

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        ps.setString(1, id);
        ps.setString(2, name);
        ps.setString(3, password);
        ps.setString(4, email);
        ps.setString(5, phoneNumber);
        ps.setNull(6, Types.BINARY);
        ps.setString(7, "Online");
        ps.setBytes(8, objectConvertor(new LinkedList<String>()));
        ps.setBytes(9, objectConvertor(new LinkedList<String>()));
        ps.setBytes(10, objectConvertor(new LinkedList<ServerIDs>()));
        ps.setBytes(11, objectConvertor(new LinkedList<UUID>()));
        ps.setBytes(12, objectConvertor(new LinkedList<UUID>()));
        ps.setBytes(13, objectConvertor(new LinkedList<String>()));;


        ps.executeUpdate();
        ps.close();

        System.out.println("data has been inserted successfully.");

    }

    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : insertChannel(overloaded version)
     * @author Arman sagharchi
     */
    public void insertUser(User user)throws SQLException, IOException{
        insertUser(user.getId(), user.getName(), user.getPassword(),
                user.getEmail(), user.getPhoneNumber());
    }


    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : updateProfile
     * @author Arman sagharchi
     */
    public void updateProfile(String id, String type, String newValue)
            throws SQLException, ConfigNotFoundException{
        String query = "UPDATE users SET " + type +" = ? where user_id = ?";

        PreparedStatement st = connection.prepareStatement(query);

        st.setString(1, newValue);
        st.setString(2, id);


        int res = st.executeUpdate();


        if(res == 0){
            throw new ConfigNotFoundException(id, "id", "user");
        }



    }

    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : updateChannelList
     * @author Arman sagharchi
     */
    public <T> boolean updateList(UpdateType type, String columnName, String id, T t)
            throws SQLException, IOException, ClassNotFoundException, UserNotFoundException {

        LinkedList<T> targetList = null;

        String query = "SELECT * FROM users WHERE user_id = ?";
        PreparedStatement pst = connection.prepareStatement(query);

        pst.setString(1, id);
        ResultSet resultSet = pst.executeQuery();

        Object o = null;

        if(resultSet == null){
            throw new UserNotFoundException(id, columnName);
        }

        while (resultSet.next()) {
            o = byteConvertor(resultSet.getBytes(columnName));
        }

        if (o instanceof LinkedList<?>) {
            targetList = (LinkedList<T>) o;
        }


        switch (type.showValue()) {


            case "Add":
                targetList = addToLists(targetList, t);
                break;


            case "Remove":
                targetList = removeFromList(targetList, t);
                break;

            default:
                return false;


        }


        byte[] updatedList = objectConvertor(targetList);

        String query2 = "UPDATE users SET " + columnName + " = ? WHERE user_id = ?";

        PreparedStatement pst2 = connection.prepareStatement(query2);

        pst2.setBytes(1, updatedList);
        pst2.setString(2, id);


        pst2.executeUpdate();

        return true;
    }

    public boolean deleteUSerById(String id) throws SQLException, ConfigNotFoundException{
        return deleteById(id, "users", "user_id", "user");
    }


    public HashMap<String, String> findByUserStatus(String status) throws SQLException{
        String query = "SELECT * FROM users WHERE user_status = ?";

        PreparedStatement pst2 = connection.prepareStatement(query);
        pst2.setString(1, status);
        ResultSet resultSet = pst2.executeQuery();
         HashMap<String, String> users = new HashMap<>();

        while(resultSet.next()){

            users.put(resultSet.getString("user_id"), resultSet.getString("name"));

        }


        return users;
    }



    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : createChannelFromData
     * @author Arman sagharchi
     */
    private User createUserFromData(ResultSet resultSet) throws SQLException, IOException,
            ClassNotFoundException {


        String id = resultSet.getString("user_id");
        String name = resultSet.getString("name");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        String phoneNumber = resultSet.getString("phone_number");

        byte[] profileImage = resultSet.getBytes("profile_image");
        UserStatus us = UserStatus.getValueFromStatus(resultSet.getString("user_status"));
        LinkedList<String> friendList;
        LinkedList<String> blockedUsers;
        LinkedList<String> privateChats;
        LinkedList<ServerIDs> servers;
        LinkedList<UUID> unreadMessages;
        LinkedList<UUID> friendRequests;

        Object o;

        if((o = byteConvertor(resultSet.getBytes("friend_list"))) instanceof LinkedList<?>){
            friendList = (LinkedList<String>) o;
        }
        else{
            friendList = null;
        }
        if((o = byteConvertor(resultSet.getBytes("blocked_users"))) instanceof LinkedList<?>){
            blockedUsers = (LinkedList<String>) o;
        }
        else{
            blockedUsers = null;
        }
        if((o = byteConvertor(resultSet.getBytes("private_chats"))) instanceof LinkedList<?>){
            privateChats = (LinkedList<String>) o;
        }
        else{
            privateChats = null;
        }
        if((o = byteConvertor(resultSet.getBytes("servers"))) instanceof LinkedList<?>){
            servers = (LinkedList<ServerIDs>) o;
        }
        else{
            servers = null;
        }
        if((o = byteConvertor(resultSet.getBytes("unread_messages"))) instanceof LinkedList<?>){
            unreadMessages = (LinkedList<UUID>) o;
        }
        else{
            unreadMessages = null;
        }
        if((o = byteConvertor(resultSet.getBytes("friend_requests"))) instanceof LinkedList<?>){
            friendRequests = (LinkedList<UUID>) o;
        }
        else{
            friendRequests = null;
        }

        return new User(id, name, password, email, phoneNumber,
                profileImage, us, friendList, blockedUsers, privateChats, servers,
                unreadMessages, friendRequests);
    }

    /**
     * the same as channelOp
     * there is a parallel method is channelOp
     * @see ChannelOp class, method : isExists
     * @author mahdi kalhor
     */
    public boolean isExists(String id)
    {
        try
        {
            User user = findById(id);
            return true;
        }
        catch (ConfigNotFoundException e)
        {
            return false;
        }
        catch (ClassNotFoundException | SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
