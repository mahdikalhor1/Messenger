package messenger.dataBaseOp;

import model.exception.ConfigNotFoundException;
import model.exception.UserNotFoundException;
import model.message.MessageType;
import model.message.TextMessage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class test {

    public static void main(String[] args){
        Database database = Database.getDatabase();
        try {


            database.getPrivateChatOp().updatePrivateChat(UpdateType.ADD, "messages",
                    "Arman-shahin", new TextMessage(UUID.randomUUID(), "shahin", "Arman",
                            MessageType.PRIVATE_CHAT, LocalDateTime.now(), "testing"));

            System.out.println(database.getPrivateChatOp().findById("Arman-shahin").getMessages());

        }
        catch(SQLException | IOException | ClassNotFoundException | ConfigNotFoundException e){
            e.printStackTrace();
        }
    }
}
