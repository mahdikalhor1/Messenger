package client;

import model.message.Message;
import model.message.MessageType;
import model.response.GetFileMsgRes;
import model.response.channel.GetChatHistoryRes;
import model.response.privateChat.GetPrivateChatHisRes;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class FileHandler
{
    private static FileHandler fileHandler;
    private final String messageUrl;
    private final String fileUrl;


    public static FileHandler getFileHandler()
    {
        if(null == fileHandler)
        {
            fileHandler = new FileHandler();
        }

        return fileHandler;
    }
    private FileHandler()
    {
        messageUrl = "client/message";
        fileUrl = "client/file";
        new File(messageUrl).mkdirs();
        new File(fileUrl).mkdirs();
    }

    /**
     * this method takes a message and saves it to the file specified
     * @param message, the message
     * @author mahdi kalhor
     */
    public void saveMessage(Message message)
    {
        try
        {
            LinkedList<Message> messages = null;

            String fileName;

            //for channel messages
            if(MessageType.CHANNEL == message.getType())
            {
                fileName = message.getReceiverId();
            }
            //for private chat messages
            else
            {
                fileName = message.getSenderId();
            }

            File file = new File(messageUrl + '/' + fileName);

            //checking that file exists or not
            if(file.createNewFile())
            {
                messages = new LinkedList<>();
            }
            else
            {
                try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file)))
                {
                    messages = (LinkedList<Message>) inputStream.readObject();
                }
                catch (FileNotFoundException e)
                {
                    System.out.println(e.getMessage());
                }
            }

            //write messages list in file if it wasn't null
            if(null != messages)
            {
                messages.add(message);

                try(ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file)))
                {
                    outputStream.writeObject(messages);
                }
                catch (FileNotFoundException e)
                {
                    System.out.println(e.getMessage());;
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * this method takes linked list of messages and then, saves linked list of messages
     * into the file
     * @param messages, linked list of messages
     * @author mahdi kalhor
     */
    public void saveMessage(LinkedList<Message> messages)
    {
        //if list was empty it will not be written in file
        if(messages.isEmpty())
        {
            return;
        }

        String fileName;

        Message sample = messages.get(0);

        //for channel messages
        if(MessageType.CHANNEL == sample.getType())
        {
            fileName = sample.getReceiverId();
        }
        //for private chat messages
        else
        {
            fileName = sample.getSenderId();
        }

        File file = new File(messageUrl + '/' + fileName);

        //creat or truncate the file to write the messages list
        try (PrintWriter pw = new PrintWriter(file))
        {

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try(ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file)))
        {
            outputStream.writeObject(messages);
        }
        catch (FileNotFoundException e)
        {
            System.out.println(e.getMessage());;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * takes the getFileResponse and saves it to the file name specified
     * @param response, response which contains content of file in bytes
     * @author mahdi kalhor
     */
    public void saveFile(GetFileMsgRes response)
    {
        //static url of file
        String url = fileUrl + '/' + response.getFileName();

        Path path = Paths.get(url);

        while (true)
        {

            try
            {
                //creates the file
                Files.createFile(path);

                //write content into file
                Files.write(path, response.getContent());

                System.out.println("file saved as : " + url);

                return;
            }
            catch (FileAlreadyExistsException e)
            {
                //if file with same name exists it will a '0' will be added to file name
                path = Paths.get(url + '0');
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

    }
}
