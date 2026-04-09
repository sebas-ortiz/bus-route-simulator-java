
package Servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JLabel;

public class Servidor {
    private static final int PUERTO = 12345; 
    private static final Bus[] autobuses = new Bus[10]; 
    private static final Reloj reloj = new Reloj();
    private static Interfaz interfaz;
    private static final Map<Integer, List<PrintWriter>> clientesConectados = new HashMap<>();
    private static boolean simulacionActiva = false; 
    private static boolean autobusesInicializados = false;

    public static void main(String[] args) {
        //Crear la interfaz gráfica
        if (interfaz == null) {
            interfaz = new Interfaz();
            //interfaz.habilitarModoConfiguracion(); // Método para obtener coordenadas, temporal
            reloj.setInterfaz(interfaz);
            interfaz.setSize(1050, 650);
            interfaz.setLocationRelativeTo(null);
        }
        interfaz.setVisible(true);
        interfaz.toFront();
        interfaz.requestFocus();
        
        // Inicializar los autobuses
        inicializarAutobuses();

        // Iniciar el servidor TCP
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor TCP iniciado en el puerto " + PUERTO);

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());
                new Thread(new ManejaCliente(clienteSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Inicia el moviemiento de los autobuses y el reloj
    public static synchronized void iniciarAutobuses() {
        if (!simulacionActiva) {
            simulacionActiva = true;
            reloj.iniciar();
            for (Bus bus : autobuses) {
                bus.iniciar();
            }
            notificarTodos("Simulación iniciada. Hora actual: " + reloj.getHoraActual());
        }
    }
     //Detiene los buses
    public static synchronized void detenerAutobuses() {
        if (simulacionActiva) {
            simulacionActiva = false;
            reloj.detener();
            for (Bus bus : autobuses) {
                bus.detener();
            }
            notificarTodos("Fin del día de servicio. Los autobuses han terminado su recorrido.");
        }
    }
    
    //Inicia los buses 
    private static synchronized void inicializarAutobuses() {
        if (autobusesInicializados) {
            return;
        }
        autobusesInicializados = true;
        for (int i = 0; i < autobuses.length; i++) {
            int busId = i + 1;
            JLabel busLabel = interfaz.obtenerEtiquetaBus(busId);
            Bus bus = new Bus(busId, obtenerParadas(busId), obtenerPuntoInicio(busId), busLabel);

            bus.setCambioParadaListener((nombreParada) -> {
                String mensaje = "[" + reloj.getHoraActual() + "] Bus " + busId + " llegó a: " + nombreParada;
                System.out.println(mensaje);
                interfaz.agregarLog(mensaje);
                notificarClientes(busId, mensaje);
                
                // Verificar si hay concurrencia (autobús "sentado")
                verificarConcurrencia(busId, nombreParada);
            });

            autobuses[i] = bus;
            autobuses[i].start();
        }
    }
    
    //Verifica si hay concurrencia de buses en una parada
    private static void verificarConcurrencia(int busId, String nombreParada) {
        Bus busActual = autobuses[busId -1];
        
        // Verificar si hay otro autobús en la misma parada
        for (Bus otroBus : autobuses) {
            if (otroBus.getBusId() != busId && otroBus.getParadaActual().equals(nombreParada)) {
                // El bus que alcanza al sentado debe esperar
                if (busActual.estaDelanteDe(otroBus)) {
                // El bus más atrasado se detiene
                otroBus.detenerTemporalmente();
                
                String mensaje = "¡CONCURRENCIA! Bus " + otroBus.getBusId() + 
                               " detenido por alcanzar a Bus " + busId + 
                               " en " + nombreParada + "(Manteniendo orden)";
                System.out.println(mensaje);
                notificarClientes(busId, mensaje);
            }
            break;
        }
    }
}
    
//Métodos para obtener las paradas
    public static int[][] obtenerCoordenadasParadas() {
        return obtenerParadas(1); // Reutiliza el método existente
    }

    public static String [] getParadas() {
        return new String[]{
            "Terminal Hatillo", 
            "Rest. La Fortuna, Hatillo 4", 
            "Plaza América", 
            "Taller Gernon", 
            "Cevichería Costa Azul", 
            "Parque De Monte Azul",
            "Abastecedor Los Sauces", 
            "Escuela República Dominicana",
            "Colegio Salesiano Don Bosco", 
            "Rotonda De Betania",
            "Acueductos Y Alcantarillados Guadalupe", 
            "Liceo Nocturno José Joaquín Jiménez Nuñez",
            "Parque San Francisco", 
            "Hotel Caribbean, Amón",
            "Escuela Juan Rafael Mora", 
            "Colegio María Auxiliadora",
            "Parqueo De Yamuni, San Francisco", 
            "Plásticos Tosso",
            "Super Lian, Hatillo", 
            "Ciudad Deportiva Rafael Ángel Pérez"
        };
    }
    
    public static int[][] obtenerPosicionesIniciales() {
        int[][] paradas = obtenerCoordenadasParadas();
        int[][] posiciones = new int[10][2];
        
        // Distribuir los 10 buses cada 2 paradas (20 paradas / 10 buses = 2)
        for (int i = 0; i < 10; i++) {
            int puntoInicio = i * 2; // Cada bus comienza en una parada diferente
            posiciones[i][0] = paradas[puntoInicio][0];
            posiciones[i][1] = paradas[puntoInicio][1];
        }
        return posiciones;
    }
    
     public static Reloj getReloj() {
        return reloj;
    }

    
     //Notifica a los cliebtes el bus que están siguiendo
    private static synchronized void notificarClientes(int busId, String mensaje) {
        List<PrintWriter> clientes = clientesConectados.getOrDefault(busId, new ArrayList<>());

        Iterator<PrintWriter> iterator = clientes.iterator();
        while (iterator.hasNext()) {
            PrintWriter salida = iterator.next();
            try {
                salida.println(mensaje);
                salida.flush();
            } catch (Exception e) {
                iterator.remove();
                System.out.println("Cliente desconectado del bus " + busId);
            }
        }
    }
    
   
    private static synchronized void notificarTodos(String mensaje) {
        for (List<PrintWriter> clientes : clientesConectados.values()) {
            for (PrintWriter salida : clientes) {
                try {
                    salida.println(mensaje);
                    salida.flush();
                } catch (Exception e) {
                    System.out.println("Error al notificar a cliente");
                }
            }
        }
    }
    
    //Devuelve las coordenadas de lasparadas y el bus que se encuentra en ellas
    public static int[][] obtenerParadas(int busId) {
        // Coordenadas del mapa real de la Ruta Periférica
        return new int[][]{
            {277, 343}, // Terminal Hatillo
            {206, 329}, // Rest. La Fortuna, Hatillo 4
            {173, 421}, // Plaza América
            {269, 397}, // Taller Gernon
            {247, 468}, // Cevichería Costa Azul
            {438, 456}, // Parque De Monte Azul
            {510, 490}, // Abastecedor Los Sauces
            {599, 455}, // Escuela Republica Dominicana
            {635, 463}, // Colegio Salesiano Don Bosco
            {651, 315}, // Rotonda De Betania
            {655, 229}, // Acueductos Y Alcantarillados Guadalupe
            {734, 194}, // Liceo Nocturno José Joaquín Jiménez Nuñez
            {682, 153}, // Parque San Francisco
            {677, 72}, // Hotel Caribbean, Amón
            {469, 136}, // Escuela Juan Rafael Mora
            {430, 168}, // Colegio María Auxiliadora
            {348, 134}, // Parqueo De Yamuni, San Francisco
            {258, 186}, // Plásticos Tosso
            {258, 186}, // Super Lian, Hatillo
            {366, 268}  // Ciudad Deportiva Rafael Ángel Pérez
        };
    }
    
    //Devuelve el nombre de las paradas según su índice
     public static String getNombreParada(int index) {
        String[] nombres = {
            "Terminal Hatillo", 
            "Rest. La Fortuna, Hatillo 4", 
            "Plaza América", 
            "Taller Gernon", 
            "Cevichería Costa Azul", 
            "Parque De Monte Azul",
            "Abastecedor Los Sauces", 
            "Escuela República Dominicana",
            "Colegio Salesiano Don Bosco", 
            "Rotonda De Betania",
            "Acueductos Y Alcantarillados Guadalupe", 
            "Liceo Nocturno José Joaquín Jiménez Nuñez",
            "Parque San Francisco", 
            "Hotel Caribbean, Amón",
            "Escuela Juan Rafael Mora", 
            "Colegio María Auxiliadora",
            "Parqueo De Yamuni, San Francisco", 
            "Plásticos Tosso",
            "Super Lian, Hatillo", 
            "Ciudad Deportiva Rafael Ángel Pérez"
        };
        return nombres[index];
    }

    //Calcula el punto de inicio de cada bus
     public static int obtenerPuntoInicio(int busId) {
        // Distribución equitativa de los buses en la ruta
        return (busId - 1) * 2;
    }
     
     //Maneja la conexión con los clientes
    private static class ManejaCliente implements Runnable {
        private final Socket clienteSocket;

        public ManejaCliente(Socket clienteSocket) {
            this.clienteSocket = clienteSocket;
        }

        @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                 PrintWriter salida = new PrintWriter(clienteSocket.getOutputStream(), true)) {
                
                // Enviar lista de autobuses disponibles a seguir
                salida.println("Autobuses disponibles: 1-10");
                
                String solicitud;
                while ((solicitud = entrada.readLine()) != null) {
                    if (solicitud.startsWith("SEGUIR")) {
                        try{ int numeroBus = Integer.parseInt(solicitud.split(" ")[1]);
                        
                        synchronized (clientesConectados) {
                            // Quita al cliente de cualquier otra lista de buses
                            for (List<PrintWriter> clientes: clientesConectados.values()){
                            clientes.remove(salida);
                        }
                         if (numeroBus >= 1 && numeroBus <= 10) {
                            // Agregar a la lista del bus seleccionado
                            clientesConectados.computeIfAbsent(numeroBus, k -> new ArrayList<>()).add(salida);
                        
                        
                        salida.println("[" + reloj.getHoraActual() + "] Siguiendo al Bus " + numeroBus);
                        
                        // Enviar estado actual del bus
                        
                            Bus bus = autobuses[numeroBus-1];
                            salida.println("Bus " + numeroBus + " actualmente en: " + bus.getParadaActual());
                        }else{
                          salida.println("ERROR: Número de bus inválido");  
                         }
                        }  
                    }catch (NumberFormatException e){
                        salida.println("ERROR");
                    }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error en la comunicación con el cliente.");
                e.printStackTrace();
            }
        }
        
    }
}
