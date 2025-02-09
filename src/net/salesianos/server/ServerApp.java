package net.salesianos.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.salesianos.server.threads.ClientHandler;
import net.salesianos.utils.Constants;

public class ServerApp {

    // Opciones de votación (puedes modificarlas aquí)
  public static final List<String> votingOptions = new ArrayList<>(Arrays.asList("Pepe", "Isra", "Luison"));

    // Contador de votos para cada opción
    public static final Map<String, Integer> voteCounts = new HashMap<>();
    // Set para rastrear clientes que ya han votado
    public static final Set<DataOutputStream> votedClients = new HashSet<>();

    public static void main(String[] args) throws IOException {
        // Inicializar el contador de votos para cada opción a 0
        for (String option : votingOptions) {
            voteCounts.put(option, 0);
        }

        ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT);
        System.out.println("Servidor levantado en el puerto " + serverSocket.getLocalPort());

        ArrayList<DataOutputStream> clientsOutputs = new ArrayList<>();

        while (true) {
            System.out.println("Esperando conexión entrante...");
            Socket clientSocket = serverSocket.accept();

            DataOutputStream clientOutputStream = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            clientsOutputs.add(clientOutputStream);

            DataInputStream clientInputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            String name = clientInputStream.readUTF();

            ClientHandler clientHandler = new ClientHandler(
                    clientInputStream,
                    clientOutputStream, // Pass clientOutputStream instead of ArrayList of all outputs
                    name,
                    clientsOutputs, // Still pass the list for broadcasting messages (optional feature)
                    votingOptions,
                    voteCounts,
                    votedClients // Pass the set of voted clients
            );
            clientHandler.start();
        }
    }
}