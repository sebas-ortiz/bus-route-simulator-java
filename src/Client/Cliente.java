package Cliente;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Cliente extends JFrame {
    private final JTextArea logArea; //Mensajes del servidor
    private final JComboBox<String> selectorBus;
    private Socket socket; //Socket para comunicación con el servidor
    private PrintWriter salida;
    private BufferedReader entrada;

   //Constructor
    public Cliente() {
        //Ventana de Cliente
        setTitle("Cliente de Autobuses Periféricos L1");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Área de texto para mostrar los mensajes
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Panel inferior con selector de autobús
        JPanel panelInferior = new JPanel(new BorderLayout());
        
        // Selector de autobuses para seguir
        selectorBus = new JComboBox<>();
        for (int i = 1; i <= 10; i++) {
            selectorBus.addItem("Bus " + i);
        }
        
        //Botones
        JButton conectarBtn = new JButton("Conectar");
        conectarBtn.addActionListener(e -> conectarServidor());
        
        JButton seguirBtn = new JButton("Seguir Bus");
        seguirBtn.addActionListener(e -> seguirBus());
        
        //Componenetes
        panelInferior.add(selectorBus, BorderLayout.CENTER);
        panelInferior.add(conectarBtn, BorderLayout.WEST);
        panelInferior.add(seguirBtn, BorderLayout.EAST);
        
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    
    //Conexión y flujos de entrada y salida
    private void conectarServidor() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            //Nueva conexión
            socket = new Socket("localhost", 12345);
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            //Hilos para recibir mensajes del servidor
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = entrada.readLine()) != null) {
                        final String msg = mensaje;
                        // Actualiza la interfaz en el hilo de eventos
                        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> logArea.append("Conexión con el servidor perdida.\n"));
                }
            }).start();
            
            logArea.append("Conectado al servidor. Seleccione un autobús y haga clic en 'Seguir Bus'.\n");
        } catch (IOException e) {
            logArea.append("Error al conectar con el servidor: " + e.getMessage() + "\n");
        }
    }

    //Solicita al servidor seguir un autobús seleccionado
    private void seguirBus() {
        if (salida == null) {
            logArea.append("Primero debe conectarse al servidor.\n");
            return;
        }
        
        try {
            //Obtiene el bus seleccionado para recibir actualizaciones del servidor
            String busSeleccionado = (String) selectorBus.getSelectedItem();
            int busId = Integer.parseInt(busSeleccionado.split(" ")[1]);
            salida.println("SEGUIR " + busId);
            logArea.append("Solicitando seguir al Bus " + busId + "\n");
        } catch (Exception e) {
            logArea.append("Error al seleccionar el bus: " + e.getMessage() + "\n");
        }
    }

    
    //Llama la app cliente y la hace visible
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof Cliente && window.isDisplayable()) {
                    window.toFront();
                    window.requestFocus();
                    return;
                }
            }
            Cliente cliente = new Cliente();
            cliente.setVisible(true);
        });
        
    }//Main
    
}//Clase
