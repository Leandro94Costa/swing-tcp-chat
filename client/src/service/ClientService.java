package service;

import bean.ChatMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientService {

    private Socket socket;
    private ObjectOutputStream output;

    public Socket connect() {
        try {
            this.socket = new Socket("localhost", 5555);
            this.output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    public void send(ChatMessage message) {
        try {
            output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}