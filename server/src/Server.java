import service.ServerService;

import java.util.Scanner;

public class Server {

    public static void main(String[] args) {
        System.out.print("Porta TCP: ");
        Scanner scanner = new Scanner(System.in);
        int port = scanner.nextInt();
        new ServerService(port);
    }
}