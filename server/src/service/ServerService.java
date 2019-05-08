package service;

import bean.ChatMessage;
import bean.ChatMessage.Action;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerService {

    private ServerSocket serverSocket;
    private Socket socket;
    private Map<String, ObjectOutputStream> mapOnline = new HashMap<>();

    public ServerService() {
        try {
            serverSocket = new ServerSocket(5555);

            System.out.println("Servidor iniciado!");

            while (true) {
                socket = serverSocket.accept();

                new Thread(new ListenerSocket(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ListenerSocket implements Runnable {

        private ObjectOutputStream output;
        private ObjectInputStream input;

        ListenerSocket(Socket socket) {
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            ChatMessage message = null;
            try {
                while (((message = (ChatMessage) input.readObject()) != null)) {
                    Action action = message.getAction();

                    switch (action) {
                        case CONNECT:
                            boolean isConnected = connect(message, output);
                            if (isConnected) {
                                mapOnline.put(message.getName(), output);
                                sendOnline();
                            }
                            break;
                        case DISCONNECT:
                            disconnect(message, output);
                            sendOnline();
                            //return;
                            break;
                        case SEND_ONE:
                            sendOne(message);
                            break;
                        case SEND_ALL:
                            sendAll(message);
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                disconnect(message, output);
                sendOnline();
                System.out.println(message.getName() + " fechou a janela e foi desconectado.");
            }
        }
    }

    private boolean connect(ChatMessage message, ObjectOutputStream output) {
        if (mapOnline.size() == 0) {
            message.setText("Hell Yeah!");
            send(message, output);
            return true;
        }
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) {
            if (kv.getKey().equals(message.getName())) {
                message.setText("Nope");
                send(message, output);
                return false;
            } else {
                message.setText("YEAH!");
                send(message, output);
                return true;
            }
        }
        return false;
    }

    private void disconnect(ChatMessage message, ObjectOutputStream output) {
        mapOnline.remove(message.getName());

        message.setText("deixou o chat!");
        message.setAction(Action.SEND_ONE);
        sendAll(message);

        System.out.println("Usuário: " + message.getName() + " saiu do chat.");
    }

    private void send(ChatMessage message, ObjectOutputStream output) {
        try {
            output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendOne(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) {
            if (kv.getKey().equals(message.getNameReserved())) {
                try {
                    kv.getValue().writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendAll(ChatMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) {
            if (!kv.getKey().equals(message.getName())) {
                message.setAction(Action.SEND_ONE);
                try {
                    kv.getValue().writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendOnline() {
        Set<String> setNames = new HashSet<>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) {
            setNames.add(kv.getKey());
        }
        ChatMessage message = new ChatMessage();
        message.setAction(Action.USERS_ONLINE);
        message.setSetOnlines(setNames);

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnline.entrySet()) {
            message.setName(kv.getKey());
            try {
                kv.getValue().writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
