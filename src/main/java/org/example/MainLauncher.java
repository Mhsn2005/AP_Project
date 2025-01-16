package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainLauncher {
    public static void main(String[] args) {
        List<Process> clients = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Collaborative Drawing Application");
        System.out.println("Type 'add' to launch a new client, or 'exit' to terminate all clients.");

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("add")) {
                // Launch a new client
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            "java", "-cp", "E:\\IdeaProjects\\ApProject2\\out\\artifacts\\ApProject2_jar\\ApProject2_jar.jar", "org.example.Client"
                    );
                    Process process = pb.start();
                    clients.add(process);
                    System.out.println("New client launched. Total clients: " + clients.size());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to launch client.");
                }
            } else if (command.equalsIgnoreCase("exit")) {
                // Terminate all clients
                for (Process process : clients) {
                    process.destroy();
                }
                System.out.println("All clients terminated.");
                break;
            } else {
                System.out.println("Unknown command. Use 'add' to add a client or 'exit' to quit.");
            }
        }

        scanner.close();
    }
}
