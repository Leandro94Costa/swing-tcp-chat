package utils;

public class Validator {

    public static String isValidPort(String txtPort) {
        String message = null;
        try {
            int port = Integer.parseInt(txtPort);
            if (!(port > 0 && port <= 65535)) {
                message = "Porta TCP fora da faixa permitida (1 até 65535)";
            }
        } catch (Exception e) {
            message = "Porta inválida, favor tente novamente";
        }
        return message;
    }
}
