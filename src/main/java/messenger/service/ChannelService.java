package messenger.service;

import messenger.dataBaseOp.Database;
import messenger.dataBaseOp.UpdateType;
import model.PrivateChat;
import model.exception.ConfigNotFoundException;
import model.message.FileMessage;
import model.message.FileMsgNotification;
import model.message.Message;
import model.request.Channel.*;
import model.response.Response;
import model.response.channel.GetChatHistoryRes;
import model.response.channel.GetPinnedMsgRes;
import model.server.*;
import model.user.ServerIDs;
import model.user.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.UUID;

/**
 * this class is handles requests related to channel and returns responses
 */
public class ChannelService
{
    private final Database database;

    /**
     * the constructor of class
     */
    public ChannelService()
    {
        database = Database.getDatabase();
    }

    /**
     * adds user to a channel
     * @param request the add user request
     * @return response related to request
     */
    public Response addUser(AddUserChannelReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            UUID channelId = server.getChannels().get(request.getChannelName());

            if(!server.getUsers().contains(request.getUserId()))
            {
                return new Response(request.getSenderId() , false ,
                        "user is not member of this server.");
            }

            if(null == channelId)
            {
                return new Response(request.getSenderId() , false ,
                        "channel : '" + request.getChannelName() + "' not found in server : '"
                                + request.getServerId() + "'");
            }

            if(!database.getChannelOp().findById(channelId.toString()).getUsers().contains(request.getUserId()))
            {
                return new Response(request.getSenderId() , false ,
                        "you cant add user to this channel!");
            }

            //add user to channel
            database.getChannelOp().updateChannelList(UpdateType.ADD , "users" ,
                    channelId.toString() , request.getUserId());

            //add channel to users list
            updateChannelsUser(request);

            return new Response(request.getSenderId() , true , "user added to channel successfully.");
        }
        catch (ConfigNotFoundException e)
        {
            return new Response(request.getSenderId() , false , e.getMessage());
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles request of creating channel
     * @param request the request
     * @return response related to request
     */
    public synchronized Response creatChannel(CreateChannelReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            if(!checkRule(request.getSenderId() , server.getId(), RuleType.CREATE_CHANNEL))
            {
                return new Response(request.getSenderId(), false ,
                        "you can't creat channel in this server!");
            }

            if(server.getChannels().containsKey(request.getChannelName()))
            {
                return new Response(request.getSenderId() , false ,
                        "channel with this name is exists already!");
            }

            //incomplete
            //voice channel section
            if(ChannelType.VOICE == request.getChannelType())
            {
                return new Response(request.getSenderId() , false , "voice channel is not allowed yet!");
            }
            else
            {
                TextChannel channel = new TextChannel(UUID.randomUUID() , request.getChannelName() , ChannelType.TEXT);

                database.getChannelOp().insertChannel(channel.getId().toString() , channel.getName(), ChannelType.TEXT);

                database.getPrivateChatOp().insertPrivateMessage(channel.getId().toString());

                database.getServerOp().updateServerHashList(UpdateType.ADD , "channels" , request.getServerId(), request.getChannelName() , channel.getId());

                //adding users of server into channel
                for(String userId : server.getUsers())
                {

                    database.getChannelOp().updateChannelList(UpdateType.ADD , "users" , channel.getId().toString() , userId);


                    User user = database.getUserOp().findById(userId);

                    ServerIDs serverIDs = user.getServers().get(user.getServers().indexOf(new ServerIDs(server.getId() , null)));
                    serverIDs.getChannels().add(channel.getName());

                    database.getUserOp().updateList(UpdateType.REMOVE , "servers" , userId , serverIDs);
                    database.getUserOp().updateList(UpdateType.ADD , "servers" , userId , serverIDs);

                }

                return new Response(request.getSenderId(), true , "channel added successfully.");

            }
        }
        catch (ConfigNotFoundException e)
        {
            return new Response(request.getSenderId() , false , e.getMessage());
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles request of deleting channel
     * @param request the request
     * @return response related to request
     */
    public Response deleteChannel(DeleteChannelReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            if(!server.getChannels().containsKey(request.getChannelName()))
            {
                return new Response(request.getSenderId() , false ,
                        "channel not found in server!");
            }

            if(!checkRule(request.getSenderId() , server.getId(), RuleType.DELETE_CHANNEL))
            {
                return new Response(request.getSenderId(), false ,
                        "you can't delete channel in this server!");
            }

            UUID channelId = server.getChannels().get(request.getChannelName());

            //delete channel
            database.getChannelOp().deleteChannelById(channelId.toString());

            database.getServerOp().updateServerHashList(UpdateType.REMOVE , "channels" , request.getServerId(), request.getChannelName() , channelId);
            //adding users of server into channel
            for(String userId : server.getUsers())
            {
                User user = database.getUserOp().findById(userId);

                ServerIDs serverIDs = user.getServers().get(user.getServers().indexOf(new ServerIDs(server.getId() , null)));
                serverIDs.getChannels().remove(request.getChannelName());

                database.getUserOp().updateList(UpdateType.REMOVE , "servers" , userId , serverIDs);
                database.getUserOp().updateList(UpdateType.ADD , "servers" , userId , serverIDs);

            }

            return new Response(request.getSenderId() , true ,
                    "channel deleted successfully.");
        }
        catch (ConfigNotFoundException e)
        {
            return new Response(request.getSenderId() , false , e.getMessage());
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles get chat history of channel
     * @param request the request
     * @return response related to request
     */
    public Response getChatHistory(GetChatHistoryReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            UUID channelId = server.getChannels().get(request.getChannelName());

            if(null == channelId)
            {
                return new GetChatHistoryRes(request.getSenderId() , false ,
                        "channel not found in server!" , null);
            }

            TextChannel channel = (TextChannel) database.getChannelOp().findById(channelId.toString());

            PrivateChat privateChat = database.getPrivateChatOp().findById(channelId.toString());

            if(channel.getUsers().contains(request.getSenderId()))
            {
                return new GetChatHistoryRes(request.getSenderId(), true ,
                        "chat history sent" , getMessages(privateChat.getMessages()));
            }
            else
            {
                return new GetChatHistoryRes(request.getSenderId(), false ,
                        "you don't have access to this channel." , null);
            }
        }
        catch (ConfigNotFoundException | ClassCastException e)
        {
            return new GetChatHistoryRes(request.getSenderId() , false , e.getMessage() , null);
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles pin message in channel request
     * @param request the request
     * @return response related to request
     */
    public Response pinMessage(PinMessageReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            if(!server.getChannels().containsKey(request.getChannelName()))
            {
                return new GetChatHistoryRes(request.getSenderId() , false ,
                        "channel not found in server!" , null);
            }

            if(!checkRule(request.getSenderId(), server.getId() , RuleType.PIN_MESSAGE))
            {
                return new Response(request.getSenderId() ,false ,"you can't pin message in this server");
            }

            UUID channelId = server.getChannels().get(request.getChannelName());

            database.getChannelOp().updateChannelList(UpdateType.ADD , "pinned_messages" ,
                    channelId.toString() , request.getMessageId());

            return new Response(request.getSenderId() , true ,
                    "message pinned successfully.");
        }
        catch (ConfigNotFoundException e)
        {
            return new Response(request.getSenderId() , false , e.getMessage());
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles get pinned message in channel request
     * @param request the request
     * @return response related to request
     */
    public Response getPinnedMessage(GetPinnedMsgReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            if(!server.getChannels().containsKey(request.getChannelName()))
            {
                return new GetPinnedMsgRes(request.getSenderId() , false ,
                        "channel not found in server!" , null);
            }

            UUID channelId = server.getChannels().get(request.getChannelName());

            TextChannel channel = (TextChannel) database.getChannelOp().findById(channelId.toString());

            if(channel.getUsers().contains(request.getSenderId()))
            {
                return new GetPinnedMsgRes(request.getSenderId(), true ,
                        "pinned messages sent" , getMessages(channel.getPinnedMessages()));
            }
            else
            {
                return new GetPinnedMsgRes(request.getSenderId(), false ,
                        "you don't have access to this channel." , null);
            }
        }
        catch (ConfigNotFoundException e)
        {
            return new GetPinnedMsgRes(request.getSenderId() , false , e.getMessage() , null);
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles remove user of channel request
     * @param request the request
     * @return response related to request
     */
    public Response removeUser(RemoveUserChannelReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            if(!server.getUsers().contains(request.getUserId()))
            {
                return new Response(request.getSenderId() , false ,
                        "user is not member of this server.");
            }

            if(!server.getChannels().containsKey(request.getChannelName()))
            {
                return new Response(request.getSenderId() , false ,
                        "channel not found in server!");
            }

            if(!checkRule(request.getSenderId(), server.getId() , RuleType.RESTRICT_MEMBER))
            {
                return new Response(request.getSenderId() ,false ,"you can't remove members of this channel");
            }

            UUID channelId = server.getChannels().get(request.getChannelName());

            //remove user from channel
            database.getChannelOp().updateChannelList(UpdateType.REMOVE , "users" ,
                    channelId.toString() , request.getUserId());

            //remove channel from users list
            updateChannelsUser(request);

            return new Response(request.getSenderId() , true ,
                    "user removed from channel successfully.");
        }
        catch (ConfigNotFoundException e)
        {
            return new Response(request.getSenderId() , false , e.getMessage());
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles rename channel request
     * @param request the request
     * @return response related to request
     */
    public Response renameChannel(RenameChannelReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            if(!server.getChannels().containsKey(request.getChannelName()))
            {
                return new Response(request.getSenderId() , false ,
                        "channel not found in server!");
            }

            if(!checkRule(request.getSenderId(), server.getId() , RuleType.RENAME_CHANNEL))
            {
                return new Response(request.getSenderId() ,false ,"you can't rename this channel");
            }

            UUID channelId = server.getChannels().get(request.getChannelName());

            database.getChannelOp().updateChannelConfig(channelId.toString() ,
                    "name" , request.getNewName());

            database.getServerOp().updateServerHashList(UpdateType.ADD , "channels" , request.getServerId() , request.getNewName() , channelId);

            for(String userId : server.getUsers())
            {
                User user = database.getUserOp().findById(userId);

                ServerIDs serverIDs = user.getServers().get(user.getServers().indexOf(new ServerIDs(server.getId() , null)));
                serverIDs.getChannels().remove(request.getChannelName());
                serverIDs.getChannels().add(request.getNewName());

                database.getUserOp().updateList(UpdateType.REMOVE , "servers" , userId , serverIDs);
                database.getUserOp().updateList(UpdateType.ADD , "servers" , userId , serverIDs);

            }

            return new Response(request.getSenderId() , true ,
                    "user renamed successfully.");
        }
        catch (ConfigNotFoundException e)
        {
            return new Response(request.getSenderId() , false , e.getMessage());
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    /**
     * gets and handles un pin a message of channel request
     * @param request the request
     * @return response related to request
     */
    public Response unPinMessage(UnpinMessageReq request)
    {
        try
        {
            Server server  = database.getServerOp().findByServerId(request.getServerId());

            if(!server.getChannels().containsKey(request.getChannelName()))
            {
                return new GetChatHistoryRes(request.getSenderId() , false ,
                        "channel not found in server!" , null);
            }

            if(!checkRule(request.getSenderId(), server.getId() , RuleType.PIN_MESSAGE))
            {
                return new Response(request.getSenderId() ,false ,"you can't un pin message in this server");
            }

            UUID channelId = server.getChannels().get(request.getChannelName());

            database.getChannelOp().updateChannelList(UpdateType.REMOVE , "pinned_messages" ,
                    channelId.toString() , request.getMessageId());

            return new Response(request.getSenderId() , true ,
                    "message un pinned successfully.");
        }
        catch (ConfigNotFoundException e)
        {
            return new Response(request.getSenderId() , false , e.getMessage());
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    private boolean checkRule(String userId , String serverId , RuleType ruleType)
    {
        try
        {
            Server server = database.getServerOp().findByServerId(serverId);

            if(server.getOwnerId().equals(userId))
            {
                return true;
            }

            if(server.getRules().get(userId).getRules().contains(ruleType))
            {
                return true;
            }

            return false;
        }
        catch (ConfigNotFoundException | NullPointerException e)
        {
            return false;
        }
        catch (SQLException | IOException | ClassNotFoundException e)
        {
            throw new RuntimeException();
        }
    }

    private LinkedList<Message> getMessages(LinkedList<UUID> messageIds)
    {
        LinkedList<Message> messages = new LinkedList<>();

        for(UUID id : messageIds)
        {
            try
            {
                Message message = database.getMessageOp().findById(id.toString());

                //if message was file message only a notification will be sent to receivers
                // , not whole the file
                if(message instanceof FileMessage)
                {
                    message = new FileMsgNotification((FileMessage) message);
                }

                messages.add(message);

            }
            catch (ConfigNotFoundException e)
            {

            }
            catch (IOException | ClassNotFoundException | SQLException e)
            {
                throw new RuntimeException(e);
            }
        }

        return messages;
    }

    /**
     * this is used to add channel in users channels list when it added
     */
    private void updateChannelsUser(AddUserChannelReq request) throws ConfigNotFoundException, SQLException, IOException, ClassNotFoundException {

        User user = database.getUserOp().findById(request.getUserId());

        ServerIDs serverId = new ServerIDs(request.getServerId(), new LinkedList<>());

        LinkedList<ServerIDs> serverIDs = user.getServers();

        serverId = serverIDs.get(serverIDs.indexOf(serverId));

        if(null != serverId)
        {
            database.getUserOp().updateList(UpdateType.REMOVE , "servers" , request.getUserId(), serverId);
            serverId.getChannels().add(request.getChannelName());
            database.getUserOp().updateList(UpdateType.ADD , "servers" , request.getUserId(), serverId);
        }
    }

    /**
     * this is used to remove channel of users channel list when it removed
     */
    private void updateChannelsUser(RemoveUserChannelReq request) throws ConfigNotFoundException, SQLException, IOException, ClassNotFoundException {
        User user = database.getUserOp().findById(request.getUserId());

        ServerIDs serverId = new ServerIDs(request.getServerId(), new LinkedList<>());

        LinkedList<ServerIDs> serverIDs = user.getServers();

        serverId = serverIDs.get(serverIDs.indexOf(serverId));

        if(null != serverId)
        {
            database.getUserOp().updateList(UpdateType.REMOVE , "servers" , request.getUserId(), serverId);
            serverId.getChannels().remove(request.getChannelName());
            database.getUserOp().updateList(UpdateType.ADD , "servers" , request.getUserId(), serverId);
        }
    }
}
