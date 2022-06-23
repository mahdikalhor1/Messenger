package messenger.service.model.response;

public class Response
{
    //id of who receives this response
    private String receiverId;

    //specifies that the request is accepted or not
    private boolean isAccepted;

    //verification of request or description about failure of request
    private String message;
}