package model.message;

/**
 * this class is used to send a notification of file message
 */
public class FileMsgNotification extends Message
{
    //file's length in bytes
    private final long fileSize;

    //makes notification for a file message
    public FileMsgNotification(FileMessage message)
    {
        super(message.getId() , message.getSenderId() , message.getReceiverId() ,
                message.getType() , message.getDate());
        fileSize = message.getContent().length;
    }

    @Override
    public String showMessage()
    {

       if(MessageType.CHANNEL == getType())
       {
           String[] id = getReceiverId().split("-");

           return "file message from : "+ getSenderId() +
                   "\nserverId : " + id[0] + " channel name : " + id[1] +
                   "\nsize : " + fileSize + " bytes.";
       }
       else
       {
           return "file message from : "+ getSenderId() +
                   "\nsize : " + fileSize + " bytes.";
       }
    }

    @Override
    public Object getContent()
    {
        return "file message , size : " + fileSize + " bytes.";
    }

    public long getFileLength() {
        return fileSize;
    }
}