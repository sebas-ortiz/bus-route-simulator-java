package Servidor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Reloj {
    private LocalTime horaActual; // Hora simulada
    private boolean esDomingo;
    private Interfaz interfaz;
    private final List<RelojListener> listeners = new ArrayList<>();
    private boolean hiloIniciado = false; // Para evitar iniciar el hilo más de una vez
    private volatile boolean activo = false;
    private Thread hiloReloj;

    public Reloj() {
        this.horaActual = LocalTime.of(5, 0); // Empieza a las 5am
        this.esDomingo = false;
    }

    public void setInterfaz(Interfaz interfaz) {
        this.interfaz = interfaz;
    }

    public interface RelojListener {
        void horaActualizada(String hora);
    }

    public void agregarListener(RelojListener listener) {
        listeners.add(listener);
    }

    private void notificarCambioHora() {
        String horaFormateada = getHoraActual();
        for (RelojListener listener : listeners) {
            listener.horaActualizada(horaFormateada);
        }
    }

    public synchronized void iniciar() {
        activo = true;
        if (!hiloIniciado) {
            hiloIniciado = true;
            hiloReloj = new Thread(() -> {
                while (true) {
                    try {
                        if (!activo) {
                            Thread.sleep(200);
                            continue;
                        }
                        Thread.sleep(500); // 0.5 segundos reales = 1 minuto simulado
                        if (!activo) {
                            continue;
                        }
                        horaActual = horaActual.plusMinutes(1);
                        notificarCambioHora();

                        if (interfaz != null) {
                            interfaz.horaActualizada(getHoraActual());
                        }

                        // Inicio del servicio
                        if (horaActual.equals(LocalTime.of(5, 0))) {
                            Servidor.iniciarAutobuses();
                            if (interfaz != null) {
                                interfaz.agregarLog("Inicio del día de servicio a las " + getHoraActual());
                            }
                        }

                        // Fin del servicio
                        if ((!esDomingo && horaActual.equals(LocalTime.MIDNIGHT)) ||
                            (esDomingo && horaActual.equals(LocalTime.of(19, 0)))) {
                            Servidor.detenerAutobuses();
                            if (interfaz != null) {
                                interfaz.agregarLog("Fin del día de servicio a las " + getHoraActual());
                            }
                        }

                    } catch (InterruptedException e) {
                    }
                }
            });
            hiloReloj.setDaemon(true);
            hiloReloj.start();
        } else if (hiloReloj != null) {
            hiloReloj.interrupt();
        }
    }

    public synchronized void detener() {
        activo = false;
        if (hiloReloj != null) {
            hiloReloj.interrupt();
        }
    }

    public String getHoraActual() {
        return horaActual.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean isDiaActivo() {
        // Ya no se usa directamente, pero se puede modificar según la lógica de los buses
        return (horaActual.isAfter(LocalTime.of(4, 59)) &&
               (!esDomingo && horaActual.isBefore(LocalTime.MIDNIGHT)) ||
               (esDomingo && horaActual.isBefore(LocalTime.of(19, 0))));
    }

    public void setEsDomingo(boolean esDomingo) {
        this.esDomingo = esDomingo;
    }
}
