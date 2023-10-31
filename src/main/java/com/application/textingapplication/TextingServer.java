package com.application.textingapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


public class TextingServer {
    public static void main(String[] args) {
        try(ServerSocket ss = new ServerSocket(8096)) {
            System.out.println("Server looking for clients...");
            while(true) {
                Socket socket = ss.accept();
                UsernameAcceptor acceptingName = new UsernameAcceptor(socket);
                acceptingName.start();
                TextBroadcastAntenna antenna = new TextBroadcastAntenna(acceptingName, socket);
                antenna.start();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

class UsernameAcceptor extends Thread {
    Socket clientSocket;
    String clientName;
    public UsernameAcceptor(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    @Override
    public void run() {
        try {
            PrintWriter pr = new PrintWriter(clientSocket.getOutputStream(), true);
            pr.println("Please enter a username to continue: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientName = br.readLine();
            ClientCollection.addClient(clientSocket, clientName);
            System.out.printf("Client %s has connected. Socket details: %s.%n", clientName, clientSocket);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

class TextBroadcastAntenna extends Thread {
    private final UsernameAcceptor nameAcceptor;
    private final Socket clientSocket;

    public TextBroadcastAntenna(UsernameAcceptor nameAcceptor, Socket clientSocket) {
        this.nameAcceptor = nameAcceptor;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            nameAcceptor.join();
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String clientName = nameAcceptor.clientName;
            String input;
            while((input = br.readLine()) != null) { // this line needs checking
                ArrayList<Client> clients = ClientCollection.getClients();
                System.out.println(clientName + ": " + input);
                if(input.equals("network --clients")) {
                    System.out.printf("Connected clients (called by %s):%n", clientSocket);
                    for(Client client: clients) {
                        if(clientSocket.equals(client.socket())) continue;
                        System.out.printf("%s ------ %s%n", client.socket(), client.name());
                    }
                } else {
                    for(Client client: clients) {
                        if(clientSocket.equals(client.socket())) continue;
                        PrintWriter out = new PrintWriter(client.socket().getOutputStream(), true);
                        out.println(nameAcceptor.clientName + ": " +input);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

record Client(Socket socket, String name) {
}

class ClientCollection {
    static ArrayList<Client> clients = new ArrayList<>();
    synchronized static ArrayList<Client> getClients() {
        return clients;
    }
    synchronized static void addClient(Socket socket, String name) {
        clients.add(new Client(socket, name));
    }
    synchronized static void removeClient(Socket socket) {
        Iterator<Client> iterator = clients.iterator();
        if(iterator.hasNext()) {
            Client client = iterator.next();
            if(socket.equals(client.socket())) clients.remove(client);
        }
    }
}
