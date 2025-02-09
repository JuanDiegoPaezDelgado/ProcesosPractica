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

/**
 * Clase principal para la aplicación cliente.
 * Este cliente permite a un usuario votar por un representante y ver los resultados.
 */
public class ClientApp {
    /**
     * Método principal que ejecuta la lógica del cliente.
     *
     * @param args Argumentos de la línea de comandos (no utilizados).
     * @throws Exception Si ocurre un error al conectar con el servidor.
     */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce tu nombre :");
        String name = scanner.nextLine();

        Socket socket = new Socket("localhost", Constants.SERVER_PORT);

        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        outputStream.writeUTF(name);
        outputStream.flush();

        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        boolean votingFinished = false; // Flag to control client loop
        System.out.println("Bienvenido al sistema de votación para elegir al representante de DAM");
        while (!votingFinished) { // Loop until voting is finished or client exits manually
            
            System.out.println("Elige una opción:");
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
                        votingFinished = true;
                        break;
                    default:
                        System.out.println("Opción inválida. Elige 1, 2 o 3.");
                }

            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Introduce un número.");
                scanner.nextLine();
            } catch (IOException e) {
                System.out.println("Error de comunicación con el servidor.");
                break;
            }
        }
        socket.close();
        scanner.close();
    }

    /**
     * Maneja el proceso de votación.
     *
     * @param inputStream  La corriente de datos de entrada desde el servidor.
     * @param outputStream La corriente de datos de salida hacia el servidor.
     * @param scanner      El scanner para la entrada del usuario.
     * @return true si la votación ha terminado, false de lo contrario.
     * @throws IOException Si ocurre un error de I/O.
     */
    private static boolean handleVoting(DataInputStream inputStream, DataOutputStream outputStream, Scanner scanner) throws IOException {
        String serverMessage = inputStream.readUTF();
        if (serverMessage.equals("OPCIONES")) {
            int numOptions = inputStream.readInt();
            System.out.println("Opciones de votación:");
            for (int i = 0; i < numOptions; i++) {
                System.out.println(inputStream.readUTF());
            }

            System.out.print("Elige el número de opción por la que quieres votar: ");
            int voteOption;
            try {
                voteOption = scanner.nextInt();
                scanner.nextLine();
                outputStream.writeInt(voteOption);
                outputStream.flush();

                String voteConfirmation = inputStream.readUTF();
                if (voteConfirmation.equals("OK")) {
                    System.out.println("Voto registrado correctamente.");
                    String voteEndedMessage = inputStream.readUTF();
                    if (voteEndedMessage.equals("ENDED")) {
                        System.out.println("Tu turno ha terminado. Gracias por votar.");
                        return true;
                    } else {
                        System.out.println("Error inesperado del servidor: se esperaba VOTE_ENDED pero se recibió: " + voteEndedMessage);
                    }

                } else if (voteConfirmation.startsWith("ERROR")) {
                    System.out.println(voteConfirmation);
                }

            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Introduce un número.");
                scanner.nextLine();
            }
        } else if (serverMessage.startsWith("ERROR")) {
            System.out.println(serverMessage);
        }
        return false;
    }

    /**
     * Maneja el proceso de visualización de los resultados.
     *
     * @param inputStream La corriente de datos de entrada desde el servidor.
     * @throws IOException Si ocurre un error de I/O.
     */
    private static void handleResults(DataInputStream inputStream) throws IOException {
        String serverMessage = inputStream.readUTF();
        if (serverMessage.equals("RESULTADOS")) {
            String resultsString = inputStream.readUTF();
            System.out.println("Votos:");
            String[] individualResults = resultsString.split("  ");
            for (int i = 0; i < individualResults.length; i++) {
                String resultPart = individualResults[i];
                String[] parts = resultPart.split(": ");
                if (parts.length == 2) {
                    String optionName = parts[0];
                    String percentage = parts[1];
                    System.out.println((i + 1) + ". " + optionName + " (" + percentage + ")");
                } else {
                    System.out.println("Error al parsear resultados: " + resultPart);
                }
            }
        } else if (serverMessage.startsWith("ERROR")) {
            System.out.println(serverMessage);
        }
    }
}
