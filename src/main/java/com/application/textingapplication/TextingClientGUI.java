package com.application.textingapplication;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TextingClientGUI extends Application {
    TextArea chatArea;
    ScrollPane chatScrollPane;
    TextField inputText;
    Button sendButton;
    Socket socket;
    String serverIPAddress;
    PrintWriter out;
    String username;
    @Override
    public void start(Stage stage) throws Exception {
        if(getParameters().getRaw().size() > 0) {
            serverIPAddress = getParameters().getRaw().get(0);
            if(getParameters().getRaw().size() > 1) {
                username = getParameters().getRaw().get(1);
            } else {
                username = "Anonymous";
            }
        } else {
            serverIPAddress = "localhost";
            username = "Anonymous";
        }
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        chatScrollPane = new ScrollPane(chatArea);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setFitToHeight(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        inputText = new TextField();
        inputText.setPromptText("Message...");
        inputText.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
                sendButton.fire();
        });

        sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        HBox hbox = new HBox(10, inputText, sendButton);
        hbox.setPrefHeight(40);
        HBox.setHgrow(inputText, Priority.ALWAYS);

        VBox root = new VBox(10, chatScrollPane, hbox);
        root.setPrefSize(500, 500);
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root);
        stage.setTitle("Groupchat Application");
        stage.setScene(scene);
        stage.show();

        boolean established = establishConnection();
        if(!established) stage.close();
    }

    private boolean establishConnection() {
        try {
            socket = new Socket(serverIPAddress, 8096);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(username);
            new TextReceiverAntenna(socket, chatArea);
            chatArea.appendText("Connected to server..." + "\n");
        } catch(Exception e) {
            System.out.println("Couldn't establish connection with server!");
            return false;
        }
        return true;
    }
    private void sendMessage() {
        String message = inputText.getText();
        if(!message.isEmpty()) {
            inputText.clear();
            chatArea.appendText(username + ": " + message + "\n");
            out.println(message);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

class TextReceiverAntenna extends Thread {
    Socket server;
    TextArea chatArea;
    public TextReceiverAntenna(Socket server, TextArea chatArea) {
        this.server = server;
        this.chatArea = chatArea;
    }
    @Override
    public void run() {
        while(true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));
                String input = br.readLine();
                chatArea.appendText(input + "\n");
            } catch(Exception e) {
                System.out.println("Socket: " + server + " is closed.");
            }
        }
    }
}
