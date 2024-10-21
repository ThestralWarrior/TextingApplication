package com.application.textingapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TextingClientCLI {
    public static void main(String[] args) {
        Socket soc = null;
        String ipAddress = "localhost";
        if(args.length > 0)
            ipAddress = args[0];
        try {
            soc = new Socket(ipAddress, 8096);
            System.out.println("Connected to server.");
            new TextReceiverAntenna1(soc).start();
            new TextTransmitterAntenna1(soc).start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

class TextTransmitterAntenna1 extends Thread {
    Socket server;
    public TextTransmitterAntenna1(Socket server) {
        this.server = server;
    }
    @Override
    public void run() {
        while(true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String input = in.readLine();
                PrintWriter out = new PrintWriter(server.getOutputStream(), true);
                out.println(input);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class TextReceiverAntenna1 extends Thread {
    Socket server;
    public TextReceiverAntenna1(Socket server) {
        this.server = server;
    }
    @Override
    public void run() {
        while(true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));
                String input = br.readLine();
                System.out.println(input);
            } catch(Exception e) {
                // e.printStackTrace();
                System.out.println("Socket: " + server + " is closed.");
            }
        }
    }
}
