package client.controller.fxController;

import client.controller.fxController.type.AddUserType;
import client.controller.fxController.type.SetImageType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.exception.ResponseNotFoundException;
import model.request.server.GetRulesServerReq;
import model.request.server.GetServerInfoReq;
import model.response.server.GetRulesServerRes;
import model.response.server.GetServerInfoRes;
import model.server.Rule;
import model.server.RuleType;

import java.io.IOException;

public class ServerRClickController extends Controller
{
    private String serverId;
    private Stage stage;

    @FXML
    private GridPane pane;

    @FXML
    private Button addUser;

    @FXML
    private Button setting;

    @FXML
    private Button creatChannel;

    @FXML
    private void addUser(ActionEvent event) throws IOException
    {
        stage.focusedProperty().addListener((obs , oldFocus , newFocus) -> focusHandler(newFocus , stage));

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/AddUser.fxml"));
        Parent parent = loader.load();
        stage.setScene(new Scene(parent));

        AddUserController controller = loader.getController();
        controller.initialize(AddUserType.ADD_SERVER , serverId , null);

        stage.show();
    }

    @FXML
    private void setting(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/renameServer.fxml"));
        Parent parent = loader.load();

        stage.setScene(new Scene(parent));

        RenameServerController controller = loader.getController();
        controller.setServerId(serverId);

        stage.show();
    }

    @FXML
    private void creatChannel(ActionEvent event) throws IOException
    {
        stage.focusedProperty().addListener((obs , oldFocus , newFocus) -> focusHandler(newFocus , stage));

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/creatChannel.fxml"));
        Parent parent = loader.load();

        stage.setScene(new Scene(parent));

        CreatChannelController controller = loader.getController();
        controller.setServerId(serverId);

        stage.show();
    }

    public void initialize(String serverId)
    {
        this.serverId = serverId;

        stage = new Stage();

        stage.initStyle(StageStyle.UNDECORATED);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);

        //show edit role button for owner of server
        try
        {
            clientSocket.send(new GetServerInfoReq(clientSocket.getId(), serverId));

            GetServerInfoRes response = (GetServerInfoRes) clientSocket.getReceiver().getResponse();

            if(!response.isAccepted())
            {
                //close the scene if getting roles failed
                closeScene();
            }

            if(response.getOwnerId().equals(clientSocket.getId()))
            {
                setting.setVisible(true);
                creatChannel.setVisible(true);
                return;
            }
        }
        catch (ResponseNotFoundException e)
        {
            //close the scene if getting roles failed
            System.out.println(e.getMessage());
            closeScene();
        }

        try
        {
            clientSocket.send(new GetRulesServerReq(clientSocket.getId(), serverId));

            GetRulesServerRes response = (GetRulesServerRes)clientSocket.getReceiver().getResponse();

            if(!response.isAccepted())
            {
                //close the scene if getting roles failed
                closeScene();
            }

            Rule rule = response.getRules().get(clientSocket.getId());

            if(null == rule)
            {
                return;
            }

            if(rule.getRules().contains(RuleType.SET_IMAGE) || rule.getRules().contains(RuleType.RENAME_SERVER))
            {
                setting.setVisible(true);
            }

            if(rule.getRules().contains(RuleType.CREATE_CHANNEL))
            {
                creatChannel.setVisible(true);
            }

        }
        catch (ResponseNotFoundException e)
        {
            //close the scene if getting roles failed
            System.out.println(e.getMessage());
            closeScene();
        }
    }

    private void focusHandler(boolean isFocused , Stage stage)
    {
        if(!isFocused)
        {
            stage.close();
        }
    }

    private void closeScene()
    {
        Stage stage = (Stage)pane.getScene().getWindow();
        stage.close();
    }
}
