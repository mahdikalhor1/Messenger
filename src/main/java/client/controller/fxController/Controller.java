package client.controller.fxController;

import client.ClientSocket;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.exception.ResponseNotFoundException;
import model.request.server.GetServerInfoReq;
import model.request.user.GetFriendListReq;
import model.request.user.GetServersReq;
import model.request.user.GetUserProfileReq;
import model.response.Response;
import model.response.server.GetServerInfoRes;
import model.response.user.GetFriendListRes;
import model.response.user.GetServersRes;
import model.response.user.GetUserProfileRes;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Controller
{
    protected final ClientSocket clientSocket = ClientSocket.getClientSocket();


    public FXMLLoader changeView(String newView, Event event){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/" + newView + ".fxml"));



            clientSocket.getReceiver().setLoader(loader);

            Parent homeParent = loader.load();

            Stage window = (Stage) ((Button)event.getSource()).getScene().getWindow();

            Scene scene = ((Button)event.getSource()).getScene();
            scene.setRoot(homeParent);

            window.setMinHeight(400);

            window.setMinWidth(800);

            window.setScene(scene);

            clientSocket.getReceiver().setStage(window);

            return loader;

        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("could not load " + newView + ".fxml class");
        }

        return null;
    }




    protected ArrayList<?> getIds(String nameSection){

        if(nameSection.equals("friends")) {

            clientSocket.send(new GetFriendListReq(clientSocket.getId()));
            try {
                Response response = clientSocket.getReceiver().getResponse();
                if (response instanceof GetFriendListRes && response.isAccepted()) {



                    return new ArrayList<>(((GetFriendListRes) response).getFriendList().keySet());
                }
            } catch (ResponseNotFoundException e) {
                e.printStackTrace();
                System.out.println("no response was receiver from server");
            }
        }
        else if(nameSection.equals("servers")){

            clientSocket.send(new GetServersReq(clientSocket.getId()));
            try{
                Response response = clientSocket.getReceiver().getResponse();

                if(response instanceof GetServersRes && response.isAccepted()){
                    return new ArrayList<>(((GetServersRes) response).getServers());
                }
            }
            catch(ResponseNotFoundException ex){
                ex.printStackTrace();
                System.out.println("no response was receiver from server");
            }
        }

        return new ArrayList<>();

    }

    protected void imageSaver(String id, String nameSection, byte[] file){

        Path path = Paths.get("resource/image/" + nameSection + "/" + id);

        try {
            if (file == null) {
                Files.deleteIfExists(path);
                return;
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        try {
            Files.createFile(path);
        }
        catch(FileAlreadyExistsException e){
            try {
                byte[] existingFile = Files.readAllBytes(path);

                if(!Arrays.equals(file, existingFile)){
                    Files.write(path, file);
                }
            }
            catch(IOException ex){
                e.printStackTrace();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public byte[] getImageById(String id, String nameSection){
        try {
            if (nameSection.equals("friends")) {
                clientSocket.send(new GetUserProfileReq(clientSocket.getId(), id));
                Response response = clientSocket.getReceiver().getResponse();

                if (response instanceof GetUserProfileRes) {
                    if (response.isAccepted()) {

                        return ((GetUserProfileRes) response).getProfileImage();

                    } else {
                        System.out.println("access denied to get " + id + "'s image");
                    }
                }

            } else if (nameSection.equals("servers")){
                clientSocket.send(new GetServerInfoReq(clientSocket.getId(), id));
                Response response = clientSocket.getReceiver().getResponse();

                if(response.isAccepted() && response instanceof GetServerInfoRes){
                    return ((GetServerInfoRes) response).getImage();
                }
            }
        }
        catch(ResponseNotFoundException e){
            e.printStackTrace();

            System.out.println("no response was receiver from server");;
        }

        return null;
    }


    protected void resultMaker(String resMes, String resTitle){

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Result.fxml"));
        try {

            Scene scene = new Scene(loader.load());
            scene.setFill(Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.setResizable(false);

            stage.setMinWidth(600);
            stage.setMinHeight(400);

            stage.initStyle(StageStyle.UNDECORATED);
            stage.initStyle(StageStyle.TRANSPARENT);

            ResultController controller = loader.getController();
            controller.setResLabel(resMes);
            controller.setResTitle(resTitle);

            stage.setScene(scene);

            stage.show();
        }
        catch(IOException e){
            e.printStackTrace();
        }


    }

    public FXMLLoader newStageMaker(String fxmlFile){

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile + ".fxml"));

        try {

            Scene scene = new Scene(loader.load());
            scene.setFill(Color.TRANSPARENT);

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setMinWidth(600);
            stage.setMinHeight(400);

            stage.initStyle(StageStyle.UNDECORATED);
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.setScene(scene);

            stage.show();
        }
        catch(IOException e){
            e.printStackTrace();
        }


        return loader;
    }
}
