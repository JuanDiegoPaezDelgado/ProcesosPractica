package net.salesianos.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hilo que maneja las conexiones de los clientes y gestiona sus interacciones para votar y ver resultados.
 */
public class ClientHandler extends Thread {

    private DataInputStream clientInputStream;
    private DataOutputStream clientOutputStream; 
    private String name;
    private ArrayList<DataOutputStream> clientsOutputs; 
    private List<String> votingOptions;
    private Map<String, Integer> voteCounts;
    private Set<DataOutputStream> votedClients; 

    /**
     * Constructor para inicializar los atributos de ClientHandler.
     *
     * @param clientInputStream   La corriente de datos de entrada desde el cliente.
     * @param clientOutputStream  La corriente de datos de salida hacia el cliente.
     * @param name                El nombre del cliente.
     * @param clientsOutputsStream La lista de corrientes de salida de todos los clientes conectados.
     * @param votingOptions       La lista de opciones de votación.
     * @param voteCounts          El mapa que lleva el conteo de votos para cada opción.
     * @param votedClients        El conjunto de corrientes de salida de los clientes que ya han votado.
     */
    public ClientHandler(DataInputStream clientInputStream, DataOutputStream clientOutputStream, String name,
            ArrayList<DataOutputStream> clientsOutputsStream,
            List<String> votingOptions,
            Map<String, Integer> voteCounts,
            Set<DataOutputStream> votedClients) {
        this.clientInputStream = clientInputStream;
        this.clientOutputStream = clientOutputStream; 
        this.name = name;
        this.clientsOutputs = clientsOutputsStream;
        this.votingOptions = votingOptions;
        this.voteCounts = voteCounts;
        this.votedClients = votedClients;
    }

    /**
     * Ejecuta el hilo y maneja las interacciones del cliente.
     */
    @Override
    public void run() {
        try {
            if (votedClients.contains(clientOutputStream)) { 
                clientOutputStream.writeUTF("ERROR: Ya has votado antes. Solo se permite un voto por usuario.");
                clientOutputStream.flush();
                return; 
            }

            while (true) {
                int option = clientInputStream.readInt(); 

                switch (option) {
                    case 1: // Votar
                        sendVotingOptions(clientOutputStream);
                        int voteOptionIndex = clientInputStream.readInt(); 
                        if (voteOptionIndex > 0 && voteOptionIndex <= votingOptions.size()) {
                            String selectedOption = votingOptions.get(voteOptionIndex - 1);
                            incrementVote(selectedOption);
                            clientOutputStream.writeUTF("OK"); 
                            clientOutputStream.flush();
                            votedClients.add(clientOutputStream); 
                            clientOutputStream.writeUTF("ENDED"); 
                            clientOutputStream.flush();
                            System.out.println(this.name + " ha votado."); 
                            return; 
                        } else {
                            clientOutputStream.writeUTF("ERROR: Opción de voto inválida.");
                            clientOutputStream.flush();
                        }
                        break;
                    case 2: // Ver resultados
                        sendVotingResults(clientOutputStream);
                        break;
                    default:
                        clientOutputStream.writeUTF("ERROR: Opción inválida. Elige 1 para votar o 2 para ver resultados.");
                        clientOutputStream.flush();
                        break;
                }
            }
        } catch (SocketException se) {
            System.out.println("Conexión cerrada con cliente " + this.name + ".");
            clientsOutputs.remove(clientOutputStream); 
            votedClients.remove(clientOutputStream); 
        } catch (IOException ioe) {
            System.out.println("Input output exception en ClientHandler para cliente " + this.name + ".");
            clientsOutputs.remove(clientOutputStream); 
            votedClients.remove(clientOutputStream); 
        } finally {
            try {
                clientOutputStream.close(); 
                clientInputStream.close(); 
            } catch (IOException e) {
                System.err.println("Error closing streams for client " + this.name + ": " + e.getMessage());
            }
        }
    }

    /**
     * Envía las opciones de votación al cliente.
     *
     * @param clientOutputStream La corriente de datos de salida hacia el cliente.
     * @throws IOException Si ocurre un error de I/O.
     */
    private void sendVotingOptions(DataOutputStream clientOutputStream) throws IOException {
        clientOutputStream.writeUTF("OPCIONES");
        clientOutputStream.writeInt(votingOptions.size()); 
        for (int i = 0; i < votingOptions.size(); i++) {
            clientOutputStream.writeUTF((i + 1) + ". " + votingOptions.get(i)); 
        }
        clientOutputStream.flush();
    }

    /**
     * Envía los resultados de la votación al cliente.
     *
     * @param clientOutputStream La corriente de datos de salida hacia el cliente.
     * @throws IOException Si ocurre un error de I/O.
     */
    private void sendVotingResults(DataOutputStream clientOutputStream) throws IOException {
        clientOutputStream.writeUTF("RESULTADOS");
        int totalVotes = 0;

        for (Integer count : voteCounts.values()) {
            totalVotes += count;
        }

        StringBuilder results = new StringBuilder();
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            String optionName = entry.getKey();
            int count = entry.getValue();
            double percentage = (totalVotes == 0) ? 0 : (double) count / totalVotes * 100;
            results.append(optionName).append(": ").append(String.format("%.2f", percentage)).append("%  ");
        }
        clientOutputStream.writeUTF(results.toString());
        clientOutputStream.flush();
    }

    /**
     * Incrementa el conteo de votos para una opción específica de manera segura.
     *
     * @param option La opción de votación que ha sido seleccionada.
     */
    private synchronized void incrementVote(String option) {
        voteCounts.put(option, voteCounts.get(option) + 1);
    }
}
