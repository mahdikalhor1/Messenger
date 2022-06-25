package messenger.service.model.response.user;

import messenger.service.model.response.GetInfoRes;

import java.util.LinkedList;

public class GetBlockedUsersRes extends GetInfoRes
{
    //id of blocked users
    private final LinkedList<String> blockedUsers;

    public GetBlockedUsersRes(String receiverId, boolean isAccepted, String message, LinkedList<String> blockedUsers) {
        super(receiverId, isAccepted, message);
        this.blockedUsers = blockedUsers;
    }
}
