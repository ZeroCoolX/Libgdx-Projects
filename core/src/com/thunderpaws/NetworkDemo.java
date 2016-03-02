package com.thunderpaws;

/**
 * Created by dewit on 2/9/16.
 */

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NetworkDemo implements ApplicationListener{
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Skin skin;
    private Stage stage;
    private Label labelDetails;
    private Label labelMessage;
    private TextButton button;
    private TextArea textIPAddress;
    private TextArea textMessage;

    // Pick a resolution that is 16:9 but not unreadibly small
    public final static float VIRTUAL_SCREEN_HEIGHT = 960;
    public final static float VIRTUAL_SCREEN_WIDTH = 540;

    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();

        //Load LibGDX test UI skin from file.
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        stage = new Stage();
        //Wire the stage to receive input, as we are using Scene2d in this example
        Gdx.input.setInputProcessor(stage);


        //The following code loops through the avaliable network interfaces
        //Keep in mind there can be multiple interfaces per device for example
        //one per NIC, one per active wireless and the loopback
        //In this case we only care about IPv4 (x.x.x.x format)
        List<String> addresses = new ArrayList<String>();
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface ni : Collections.list(interfaces)){
                for(InetAddress address : Collections.list(ni.getInetAddresses())){
                    if(address instanceof Inet4Address){
                        addresses.add(address.getHostAddress());
                    }
                }
            }

        }catch(SocketException se){
            se.printStackTrace();
        }

        //Print the contents of our array to a stirng
        StringBuilder ipAddress = new StringBuilder();
        for(String str : addresses){
            ipAddress.append(str + "\n");
        }

        //Now setup our scene UI

        //Vertical group groups content vertically.
        VerticalGroup vg = new VerticalGroup().space(3).pad(5).fill();//.space(2).pad)(5).fill;//.space(3).reverse().fill

        //Set the bounds of the group to the entire virtual display
        vg.setBounds(0,0, VIRTUAL_SCREEN_WIDTH, VIRTUAL_SCREEN_HEIGHT);

        //Create our controls
        labelDetails = new Label(ipAddress, skin);
        labelMessage = new Label("No messages yet...", skin);
        button = new TextButton("Send Message", skin);
        textIPAddress = new TextArea("", skin);
        textMessage = new TextArea("", skin);

        //Add them to scene
        vg.addActor(labelDetails);
        vg.addActor(labelMessage);
        vg.addActor(textIPAddress);
        vg.addActor(textMessage);
        vg.addActor(button);

        //Add scene to stage
        stage.addActor(vg);

        //SEtup a viewport to map screen to a 480X640 virtual res
        //As otherwise this is waaaaaay too tiny on the 1080p android
        stage.getCamera().viewportWidth = VIRTUAL_SCREEN_WIDTH;
        stage.getCamera().viewportHeight = VIRTUAL_SCREEN_HEIGHT;
        stage.getCamera().position.set(VIRTUAL_SCREEN_WIDTH / 2, VIRTUAL_SCREEN_HEIGHT / 2, 0);

        //Now we create a thread that will listen for incoming socket connections
        new Thread(new Runnable(){
           @Override
            public void run(){
               ServerSocketHints serverSocketHint = new ServerSocketHints();
               //0 means no input. probably not the greatest idea in prod
               serverSocketHint.acceptTimeout = 0;

               //Create the socet server using TCP protocol and listening on 9021
               //Only one app can listen to a port at a time, keep in mind many ports are reserved
               ServerSocket serverSocket = Gdx.net.newServerSocket(Protocol.TCP, 9021, serverSocketHint);

               //Loop forever
               while(true){
                   //CReate a socket
                   Socket socket = serverSocket.accept(null);

                   //REad data from the socket into a buffer
                   BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                   try{
                       //read to the next newline (\n) and display that text on label message
                       labelMessage.setText(buffer.readLine());
                   }catch(IOException io){
                       io.printStackTrace();
                   }
               }
           }
        }).start();//aaaaaand, start the thread running

        //Wrie up a click listened to our button
        button.addListener(new ClickListener(){
           @Override
            public void clicked(InputEvent event, float x, float y){

               //When the button is clicked, get the message test or create a default string value
               String textToSend = new String();
               if(textMessage.getText().length() ==0){
                   textToSend = "Doesn't say much but likes clicking buttons\n";
               }else{
                   textToSend = textMessage.getText() + ("\n");//Brute for a newline so readline gets a line
               }

               SocketHints socketHints = new SocketHints();
               //Socek will time out in 4 seconds
               socketHints.connectTimeout = 4000;
               //create the socket and connect to the server entered in the text box on port 9021
               Socket socket = Gdx.net.newClientSocket(Protocol.TCP, textIPAddress.getText(), 9021, socketHints);
               try{
                   //write our entered message to the stream
                   socket.getOutputStream().write(textToSend.getBytes());
               }catch(IOException io){
                   io.printStackTrace();
               }
           }
        });
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        stage.draw();
        batch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
