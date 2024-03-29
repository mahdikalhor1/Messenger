package client;

import client.controller.fxController.HomeController;
import client.controller.fxController.cell.FxClient;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import model.Transferable;
import model.exception.InvalidObjectException;
import model.exception.ResponseNotFoundException;
import model.message.Message;
import model.response.Response;

public class Receiver
{
    //the latest response that was arrived from server
    private Response response;

    private FXMLLoader loader;
    private Stage stage;

    public Receiver(){
        loader = new FXMLLoader
                (FxClient.class.getResource("/fxml/Login.fxml"));
    }

    /**
     * gets parameter transferable and then checks the type of it
     * @param transferable, the response received from server
     * @throws InvalidObjectException, if type of object is invalid
     */
    public void getInput(Transferable transferable) throws InvalidObjectException {

        try
        {
            setResponse((Response) transferable);

        }
        catch (ClassCastException e)
        {
            try
            {

                receiveMessage((Message) transferable);

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {

                        HomeController controller = getLoader().getController();
                        controller.realTimeUpdate((Message) transferable);
                    }
                });

            }
            catch (ClassCastException ex)
            {
                throw new InvalidObjectException();
            }
            catch (IllegalArgumentException | NullPointerException | IllegalStateException ex)
            {

            }

        }
    }

    private void setResponse(Response response)
    {
        this.response = response;
    }


    /**
     * response from server might have some delay
     * because of that, there is a loop in the method runs after 50 m seconds
     * which updates the socked to see if anything is received from server.
     * @return response received from server
     * @throws ResponseNotFoundException, if the response time out occurs
     */
    public synchronized Response getResponse() throws ResponseNotFoundException
    {
        for (int i = 0; i < 1000; i++)
        {
            if (this.response != null)
            {
                Response response = this.response;

                //setting the response field to null, to store next responses
                this.response = null;

                return response;
            }
            else
            {
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new ResponseNotFoundException();
    }

    private void receiveMessage(Message message)
    {
        System.out.println(message);

        //store messages in client side
        FileHandler.getFileHandler().saveMessage(message);
    }


    public FXMLLoader getLoader() {
        return loader;
    }

    public void setLoader(FXMLLoader loader) {
        this.loader = loader;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
