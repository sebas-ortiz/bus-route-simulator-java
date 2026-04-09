package Servidor;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class Bus extends Thread {
    private final int id;
    private final int[][] paradas; //Coordenadas de las paradas en el mapa
    private volatile int index;// Índice de la parada actual
    private volatile boolean activo; // Estado del bus
    private final JLabel busLabel; //Para las .pngs de buses
    private CambioParadaListener listener;
    private final String[] nombresParadas; //Nombre de las paradas
    private volatile String paradaActual;
    private volatile boolean detenidoTemporalmente = false; //En caso de concurrencia
    private volatile long tiempoDetencion = 0;
    
    
    //Constructor
    public Bus(int id, int[][] paradas, int puntoInicio, JLabel busLabel) {
        this.id = id;
        this.paradas = paradas;
        this.index = puntoInicio % paradas.length;
        this.busLabel = busLabel;
        this.activo = false;
        
        this.nombresParadas = Servidor.getParadas();
        this.paradaActual = nombresParadas[index];
        actualizarPosicion();
    }
     
    
    // Hilo que controla el movimiento del bus
    @Override
    public void run() {
        while (true) {
            try {
                if (activo) {
                    if (detenidoTemporalmente) {
                        verificarDetencion();
                        Thread.sleep(500);
                        continue;
                    }
                    //Avanza siempre a la siguiente parada (en un circuito continuo)
                    index = (index + 1) % paradas.length;
                    paradaActual = nombresParadas[index];
                    actualizarPosicion();
                    
                    //Notifica el cambio de parada
                    if (listener != null) {
                        listener.CambioParada(paradaActual);
                    }
                    //Tiempo aleatorio de espera entre paradas
                    Thread.sleep(8000 + (int)(Math.random() * 4000));
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
            }
        }
    }
    
    
    //Actualiza la posición de las imágenes de los buses
    private void actualizarPosicion() {
        SwingUtilities.invokeLater(() -> {
            int x = paradas[index][0] - busLabel.getWidth()/2;
            int y = paradas[index][1] - busLabel.getHeight()/2;
            busLabel.setLocation(x, y);
            // Actualizar la con información del bus
            busLabel.setToolTipText("Bus " + id + " en " + paradaActual + 
                                  " - " + Servidor.getReloj().getHoraActual());
        });
    }
    
    
    //Verifica si hay un bus adelante
    public boolean estaDelanteDe(Bus otroBus) {
        if (this.index == otroBus.index) return false;
        
        if (this.index > otroBus.index) {
            return (this.index - otroBus.index) < paradas.length/2;
        } else {
            return (otroBus.index - this.index) > paradas.length/2;
        }
    }
    
    
    //Detiene un bus si hay concurrencia con otro
    public void detenerTemporalmente() {
        this.detenidoTemporalmente = true;
        this.tiempoDetencion = System.currentTimeMillis();
        interrupt();
        if (listener != null) {
            listener.CambioParada("Detenido temporalmente en: " + paradaActual);
        }
    }
    
    //Verifica si ya puede seguir caminando
    public void verificarDetencion() {
        if (System.currentTimeMillis() - tiempoDetencion > 5000) { // 5 segundos
            detenidoTemporalmente = false;
            if (listener != null) {
                listener.CambioParada("Reanudado: " + paradaActual);
            }
        }
    }

    //Métodos para iniciar y detener los buses
    public void iniciar() {
        this.activo = true;
        interrupt();
    }
    public void detener() {
        this.activo = false;
        this.detenidoTemporalmente = false;
        interrupt();
    }
    
    //Getters y Setters
    public String getParadaActual() {
        return paradaActual;
    }
    public int getBusId() {
        return id;
    }
    public void setCambioParadaListener(CambioParadaListener listener) {
        this.listener = listener;
    }
    
    //Notifica los cambios de paradas
    public interface CambioParadaListener {
        void CambioParada(String nombreParada);
    }
}
