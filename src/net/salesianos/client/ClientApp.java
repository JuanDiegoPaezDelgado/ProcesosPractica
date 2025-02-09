package net.salesianos.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;
import net.salesianos.utils.Constants;

public class ClientApp {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce tu nombre :");
        String name = scanner.nextLine();

        Socket socket = new Socket("localhost", Constants.SERVER_PORT);

        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        outputStream.writeUTF(name);
        outputStream.flush();

        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        // ServerListener serverListenerThread = new ServerListener(inputStream); // No need for ServerListener in this version
        // serverListenerThread.start();

        boolean votingFinished = false; // Flag to control client loop
        System.out.println("Bienvenido al sistema de votacion para elegir al representante de DAM");
        while (!votingFinished) { // Loop until voting is finished or client exits manually
            
            System.out.println("Elige una opcion :");
            System.out.println("1. Votar");
            System.out.println("2. Ver resultados");
            System.out.println("3. Salir"); 
            System.out.print("-> ");

            int option;
            try {
                option = scanner.nextInt();
                scanner.nextLine(); // Consumir la nueva línea
                outputStream.writeInt(option); // Enviar la opción elegida al servidor
                outputStream.flush();

                switch (option) {
                    case 1: // Votar
                        votingFinished = handleVoting(inputStream, outputStream, scanner); // Handle vote and check if voting ended
                        if (votingFinished) {break;} // Exit loop if voting finished
                    case 2: // Ver resultados
                        handleResults(inputStream);
                        break;
                    case 3: // Salir
                        System.out.println("Saliendo del programa.");
                        votingFinished = true; // Set flag to exit loop
                        break;
                    default:
                        System.out.println("Opción inválida. Elige 1, 2 o 3.");
                }


            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Introduce un número.");
                scanner.nextLine(); // Limpiar el buffer del scanner
            } catch (IOException e) {
                System.out.println("Error de comunicación con el servidor.");
                break; // Salir del bucle si hay un error de IO
            }
        }
        socket.close(); // Close socket when client is done
        scanner.close();
    }

    private static boolean handleVoting(DataInputStream inputStream, DataOutputStream outputStream, Scanner scanner) throws IOException {
        String serverMessage = inputStream.readUTF(); // Leer mensaje del servidor
        if (serverMessage.equals("OPCIONES")) {
            int numOptions = inputStream.readInt();
            System.out.println("Opciones de votación:");
            for (int i = 0; i < numOptions; i++) {
                System.out.println(inputStream.readUTF()); // Imprimir opciones numeradas
            }

            System.out.print("Elige el número de opción por la que quieres votar: ");
            int voteOption;
            try {
                voteOption = scanner.nextInt();
                scanner.nextLine(); // Consumir la nueva línea
                outputStream.writeInt(voteOption); // Enviar la opción de voto elegida al servidor
                outputStream.flush();

                String voteConfirmation = inputStream.readUTF();
                if (voteConfirmation.equals("OK")) {
                    System.out.println("Voto registrado correctamente.");
                    String voteEndedMessage = inputStream.readUTF(); // Expecting "VOTE_ENDED" message
                    if (voteEndedMessage.equals("ENDED")) {
                        System.out.println("Tu turno ha terminado. Gracias por votar.");
                        return true; // Voting finished for this client
                    } else {
                        System.out.println("Error inesperado del servidor: se esperaba VOTE_ENDED pero se recibió: " + voteEndedMessage);
                    }

                } else if (voteConfirmation.startsWith("ERROR")) {
                    System.out.println(voteConfirmation); // Mostrar mensaje de error del servidor
                }


            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Introduce un número.");
                scanner.nextLine();
            }
        } else if (serverMessage.startsWith("ERROR")) {
            System.out.println(serverMessage); // Mostrar mensaje de error del servidor
        }
        return false; // Voting not finished yet or error occurred
    }

    private static void handleResults(DataInputStream inputStream) throws IOException {
        String serverMessage = inputStream.readUTF();
        if (serverMessage.equals("RESULTADOS")) {
            String resultsString = inputStream.readUTF();
            System.out.println("Votos:");
            String[] individualResults = resultsString.split("  "); // Separar por doble espacio
            // System.out.println(resultsString);
            for (int i = 0; i < individualResults.length; i++) {
                String resultPart = individualResults[i];
                String[] parts = resultPart.split(": "); // Separar opción y porcentaje
                if (parts.length == 2) {
                    String optionName = parts[0];
                    String percentage = parts[1];
                    System.out.println((i + 1) + ". " + optionName + " (" + percentage + ")");
                } else {
                    System.out.println("Error al parsear resultados: " + resultPart); // Por si hay problemas
                }
            }
        } else if (serverMessage.startsWith("ERROR")) {
             System.out.println(serverMessage); // Mostrar mensaje de error del servidor
        }
    }
}