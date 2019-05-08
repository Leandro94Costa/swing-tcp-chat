package view;

import bean.ChatMessage;
import bean.ChatMessage.Action;
import service.ClientService;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Set;

public class ClientView extends JFrame {
    private JPanel rootPanel;
    private JTextField txtName;
    private JButton sairButton;
    private JTextArea textAreaReceive;
    private JTextArea textAreaSend;
    private JButton limparButton;
    private JButton enviarButton;
    private JList<String> listOnline;
    private JButton conectarButton;
    private JPanel ChatPanel;
    private JPanel onlinePanel;
    private JPanel conectarPanel;

    private Socket socket;
    private ChatMessage message;
    private ClientService service;

    public ClientView() {
        initComponents();
        listeners();
    }

    private void initComponents() {
        add(rootPanel);
        setTitle("Chat");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void listeners() {
        conectarButton.addActionListener(e -> {
            String name = this.txtName.getText();

            if (!name.isEmpty()) {
                this.message = new ChatMessage();
                this.message.setAction(Action.CONNECT);
                this.message.setName(name);

                this.service = new ClientService();
                this.socket = this.service.connect();

                new Thread(new ListenerSocket(this.socket)).start();

                this.service.send(message);
            }
        });
        sairButton.addActionListener(e -> {
            this.message.setAction(Action.DISCONNECT);
            this.service.send(this.message);
            disconnected();
        });
        enviarButton.addActionListener(e -> {
            String text = this.textAreaSend.getText();
            String name = this.message.getName();

            this.message = new ChatMessage();

            if (this.listOnline.getSelectedIndex() > -1) {
                this.message.setNameReserved(this.listOnline.getSelectedValue());
                this.message.setAction(Action.SEND_ONE);
                this.listOnline.clearSelection();
            } else {
                this.message.setAction(Action.SEND_ALL);
            }
            if (!text.isEmpty()) {
                this.message.setName(name);
                this.message.setText(text);

                this.textAreaReceive.append("Você: " + text + "\n");

                this.service.send(this.message);
            }
            this.textAreaSend.setText("");
        });
        limparButton.addActionListener(e -> {
            this.textAreaSend.setText("");
        });
    }

    private class ListenerSocket implements Runnable {

        private ObjectInputStream input;

        public ListenerSocket(Socket socket) {
            try {
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            ChatMessage message = null;
            try {
                while ((message = (ChatMessage) input.readObject()) != null) {
                    Action action = message.getAction();

                    switch (action) {
                        case CONNECT:
                            connected(message);
                            break;
                        case DISCONNECT:
                            disconnected();
                            socket.close();
                            break;
                        case SEND_ONE:
                            receive(message);
                            break;
                        case USERS_ONLINE:
                            refreshOnline(message);
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void connected(ChatMessage message) {
        if (message.getText().equals("Nope")) {
            this.txtName.setText("");
            JOptionPane.showMessageDialog(this, "Conexão não realizada. \nTente novamente com um outro nome.");
            return;
        }

        this.message = message;
        this.conectarButton.setEnabled(false);
        this.txtName.setEnabled(false);

        this.sairButton.setEnabled(true);
        this.textAreaSend.setEnabled(true);
        this.textAreaReceive.setEnabled(true);
        this.enviarButton.setEnabled(true);
        this.limparButton.setEnabled(true);

        JOptionPane.showMessageDialog(this, "Você está conectado!");
    }

    private void disconnected() {
        this.conectarButton.setEnabled(true);
        this.txtName.setEnabled(true);

        this.sairButton.setEnabled(false);
        this.textAreaSend.setEnabled(false);
        this.textAreaReceive.setEnabled(false);
        this.enviarButton.setEnabled(false);
        this.limparButton.setEnabled(false);

        this.textAreaReceive.setText("");
        this.textAreaSend.setText("");

        JOptionPane.showMessageDialog(this, "Desconectado com sucesso!");
    }

    private void receive(ChatMessage message) {
        this.textAreaReceive.append(message.getName() + ": " + message.getText() + "\n");
    }

    private void refreshOnline(ChatMessage message) {
        Set<String> names = message.getSetOnlines();

        names.remove(message.getName());

        String[] array = names.toArray(new String[0]);

        this.listOnline.setListData(array);
        this.listOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.listOnline.setLayoutOrientation(JList.VERTICAL);
    }
}