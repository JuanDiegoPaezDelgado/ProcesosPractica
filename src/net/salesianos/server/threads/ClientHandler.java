package net.salesianos.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientHandler extends Thread {

    private DataInputStream clientInputStream;
    private DataOutputStream clientOutputStream; // Store the OutputStream for this client
    private String name;
    private ArrayList<DataOutputStream> clientsOutputs; // Still keep this for broadcasting (optional)
    private List<String> votingOptions;
    private Map<String, Integer> voteCounts;
    private Set<DataOutputStream> votedClients; // Set of clients who have voted

    public ClientHandler(DataInputStream clientInputStream, DataOutputStream clientOutputStream, String name,
            ArrayList<DataOutputStream> clientsOutputsStream,
            List<String> votingOptions,
            Map<String, Integer> voteCounts,
            Set<DataOutputStream> votedClients) {
        this.clientInputStream = clientInputStream;
        this.clientOutputStream = clientOutputStream; // Assign the OutputStream for this client
        this.name = name;
        this.clientsOutputs = clientsOutputsStream;
        this.votingOptions = votingOptions;
        this.voteCounts = voteCounts;
        this.votedClients = votedClients;
    }

    @Override
    public void run() {
        try {
            if (votedClients.contains(clientOutputStream)) { // Check if client has already voted
                clientOutputStream.writeUTF("ERROR: Ya has votado antes. Solo se permite un voto por usuario.");
                clientOutputStream.flush();
                return; // End thread, client already voted
            }

            while (true) {
                int option = clientInputStream.readInt(); // Leer la opción del cliente como entero

                switch (option) {
                    case 1: // Votar
                        sendVotingOptions(clientOutputStream);
                        int voteOptionIndex = clientInputStream.readInt(); // Leer la opción de voto elegida
                        if (voteOptionIndex > 0 && voteOptionIndex <= votingOptions.size()) {
                            String selectedOption = votingOptions.get(voteOptionIndex - 1);
                            incrementVote(selectedOption);
                            clientOutputStream.writeUTF("OK"); // Confirmar voto
                            clientOutputStream.flush();
                            votedClients.add(clientOutputStream); // Add client to voted set
                            clientOutputStream.writeUTF("ENDED"); // Signal vote ended
                            clientOutputStream.flush();
                            System.out.println(this.name + " ha votado."); // Opcional: Broadcast anuncio de voto
                            return; // End thread after voting
                        } else {
                            clientOutputStream.writeUTF("ERROR: Opción de voto inválida.");
                            clientOutputStream.flush();
                        }
                        break;
                    case 2: // Ver resultados
                        sendVotingResults(clientOutputStream);
                        break;
                    default:
                        clientOutputStream
                                .writeUTF("ERROR: Opción inválida. Elige 1 para votar o 2 para ver resultados.");
                        clientOutputStream.flush();
                        break;
                }
            }
        } catch (SocketException se) {
            System.out.println("Conexión cerrada con cliente " + this.name + ".");
            clientsOutputs.remove(clientOutputStream); // Remover el outputStream al cerrar conexión
            votedClients.remove(clientOutputStream); // Remove from voted clients set if connection closes unexpectedly
                                                     // (optional)
        } catch (IOException ioe) {
            System.out.println("Input output exception en ClientHandler para cliente " + this.name + ".");
            clientsOutputs.remove(clientOutputStream); // Remover el outputStream al cerrar conexión por error
            votedClients.remove(clientOutputStream); // Remove from voted clients set if error occurs (optional)
        } finally {
            try {
                clientOutputStream.close(); // Close the OutputStream when the thread finishes
                clientInputStream.close(); // Close the InputStream when the thread finishes
            } catch (IOException e) {
                System.err.println("Error closing streams for client " + this.name + ": " + e.getMessage());
            }
        }
    }

    private void sendVotingOptions(DataOutputStream clientOutputStream) throws IOException {
        clientOutputStream.writeUTF("OPCIONES");
        clientOutputStream.writeInt(votingOptions.size()); // Enviar la cantidad de opciones
        for (int i = 0; i < votingOptions.size(); i++) {
            clientOutputStream.writeUTF((i + 1) + ". " + votingOptions.get(i)); // Enviar cada opción numerada
        }
        clientOutputStream.flush();
    }

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

    private synchronized void incrementVote(String option) {
        voteCounts.put(option, voteCounts.get(option) + 1);
    }

   
}