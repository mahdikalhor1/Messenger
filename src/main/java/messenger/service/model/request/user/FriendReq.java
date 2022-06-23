/**
 * this class is used to send friend request to a user
 *
 * @author mahdi
 */
package messenger.service.model.request.user;

import messenger.service.model.request.Request;

import java.io.Serializable;
import java.util.UUID;

public class FriendReq extends UserRequest implements Serializable
{
    private UUID friendRequest;
    private String receiver;
}