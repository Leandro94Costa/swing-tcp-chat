package view;

import utils.Validator;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ConnectorView extends JFrame {
    private JPanel rootPanel;
    private JButton btnConnect;
    private JTextField txtPort;
    private JTextField txtHost;
    private JLabel txtValidationMsg;

    public ConnectorView() {
        initComponents();
        listeners();
    }

    private void initComponents() {
        add(rootPanel);
        setTitle("Conectar");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void listeners() {
        btnConnect.addActionListener(e -> connect());
        txtPort.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    connect();
                }
            }
        });
    }

    private void connect() {
        String port = txtPort.getText();
        String validation = Validator.isValidPort(port);
        if (validation == null) {
            ClientView clientView = new ClientView(txtHost.getText(), Integer.parseInt(port));
            this.dispose();
            clientView.setVisible(true);
            //clientView.setFocus();
        } else {
            txtValidationMsg.setText(validation);
        }
    }
}