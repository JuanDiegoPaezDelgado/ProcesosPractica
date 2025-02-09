package net.salesianos.client.threads;

import java.io.DataInputStream;
import java.io.IOException;

public class ServerListener extends Thread {

    private DataInputStream inputStream;

    public ServerListener(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        while (true) {
            String serverMessage;
            try {
                serverMessage = this.inputStream.readUTF();
                System.out.println(serverMessage);
            } catch (IOException e) {
                System.out.println("Servidor desconectado o problema recibiendo mensaje.");
                break; // Salir del bucle si hay un error de IO, indicando que el servidor se ha desconectado o hay un problema grave
            }
        }
    }
}