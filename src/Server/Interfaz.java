package Servidor;

import javax.swing.*;
import java.awt.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Interfaz extends JFrame implements Reloj.RelojListener {
    private final JButton iniciarBtn = new JButton("Iniciar Simulación");
    private final JButton detenerBtn = new JButton("Detener Simulación");
    private final JLabel[] etiquetasParadas = new JLabel[20]; //20 paradas
    private final JLabel[] etiquetasBuses = new JLabel[10]; //10 buses
    private JLabel mapaLabel;
    private JLayeredPane layeredPane;
    private final JLabel relojLabel = new JLabel("05:00", SwingConstants.CENTER);
    private final JTextArea logArea = new JTextArea();

    public Interfaz() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Simulador de Rutas de Autobuses Periféricos L1");
        
        // Panel dividio para tener el mapa a la izquierda y el log a la derecha
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerSize(5); // Grosor del divisor
        splitPane.setOneTouchExpandable(true); // Botón para expandir/contraer
        
        // Panel izquierdo (mapa y componentes)
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // Configuración del layeredPane para el mapa
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setPreferredSize(new Dimension(900, 600));
        
        //Carga y configurar el mapa de fondo
        mapaLabel = cargarMapa();
        mapaLabel.setBounds(0, 0, 900, 600);
        layeredPane.add(mapaLabel, JLayeredPane.DEFAULT_LAYER, 0);
        
        //carga paradas y buses
        cargarParadas();
        inicializarAutobuses();
        
        leftPanel.add(layeredPane, BorderLayout.CENTER);
        
        // Panel superior con controles
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        
        //Configuracion del reloj
        relojLabel.setFont(new Font("Arial", Font.BOLD, 24));
        relojLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        //Botones de control
        JPanel controles = new JPanel();
        controles.add(iniciarBtn);
        controles.add(detenerBtn);
        
        
        //Componenetes superiores
        panelSuperior.add(relojLabel);
        panelSuperior.add(Box.createVerticalStrut(10));
        panelSuperior.add(controles);
        
        leftPanel.add(panelSuperior, BorderLayout.NORTH);
        
        // Panel derecho (área de log)
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(240, 240, 240));
        logArea.setMargin(new Insets(5, 5, 5, 5));
        //Panel scroll
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setPreferredSize(new Dimension(300, 600));
        
        splitPane.setLeftComponent(leftPanel);//Mapa y controles a la izquierda
        splitPane.setRightComponent(scrollLog);//Log a la derecha
        splitPane.setDividerLocation(900); // Posición inicial del divisor
        
        add(splitPane);
        
        configurarListeners();
        
        setSize(1200, 650); // Tamaño de la ventana
        setLocationRelativeTo(null);
    }

    private JLabel cargarMapa() {
        try {// Carga la imagen del mapa
            ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("ruta_periferica.png"));
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(900, 600, Image.SCALE_SMOOTH); // Escala la imagen al tamaño deseado
            return new JLabel(new ImageIcon(imagenEscalada));
        } catch (Exception e) {
            System.err.println("Error cargando mapa: " + e.getMessage());
            
            JLabel label = new JLabel("Mapa no disponible");
            label.setLayout(null);
            label.setBackground(Color.WHITE);
            label.setOpaque(true);
            label.setBounds(0, 0, 900, 600);

            return label;
        }
    }

    private void cargarParadas() {
        int[][] coordenadasParadas = Servidor.obtenerCoordenadasParadas(); // Obtiene coordenadas de las paradas del servidor
        // Crea un pin para cada parada
        for (int i = 0; i < coordenadasParadas.length; i++) {
            try {// Intenta cargar imagen de parada
                ImageIcon iconoParada = new ImageIcon(getClass().getResource("parada.png"));
                iconoParada = escalarImagen(iconoParada, 40, 40);
                
                etiquetasParadas[i] = new JLabel(iconoParada);
                etiquetasParadas[i].setBounds(
                    coordenadasParadas[i][0] - 20,
                    coordenadasParadas[i][1] - 20, 40, 40);
                layeredPane.add(etiquetasParadas[i], JLayeredPane.PALETTE_LAYER, 0); //Se encuentran en la capa intermedia
                
                etiquetasParadas[i].setToolTipText(Servidor.getNombreParada(i));
                
            } catch (Exception e) {
                etiquetasParadas[i] = new JLabel("•");
                etiquetasParadas[i].setForeground(Color.BLUE);
                etiquetasParadas[i].setFont(new Font("Arial", Font.BOLD, 40));
                etiquetasParadas[i].setBounds(coordenadasParadas[i][0] - 5,
                        coordenadasParadas[i][1] - 5, 10, 10);
                layeredPane.add(etiquetasParadas[i], JLayeredPane.PALETTE_LAYER, 0);
            }
        }
    }
    //Inicia los buses en el mapa
    private void inicializarAutobuses() {
        int[][] posicionesIniciales = Servidor.obtenerPosicionesIniciales();
        
        for (int i = 0; i < etiquetasBuses.length; i++) {
            try { //Crea un label para cada imagen de bus
                ImageIcon iconoBus = new ImageIcon(getClass().getResource( "bus_" + (i+1) + ".png"));
                iconoBus = escalarImagen(iconoBus, 80, 80);
                
                etiquetasBuses[i] = new JLabel(iconoBus);
                etiquetasBuses[i].setBounds(
                    posicionesIniciales[i][0] - 40,
                    posicionesIniciales[i][1] - 40, 80, 80);
                layeredPane.add(etiquetasBuses[i], JLayeredPane.POPUP_LAYER, 0);
                
                //Añade número de bus
                JLabel numeroBus = new JLabel(String.valueOf(i+1));
                numeroBus.setForeground(Color.WHITE);
                numeroBus.setFont(new Font("Arial", Font.BOLD, 14));
                numeroBus.setBounds(12, 12, 16, 16);
                etiquetasBuses[i].add(numeroBus);
                
            } catch (Exception e) {
                etiquetasBuses[i] = new JLabel("" + (i+1));
                etiquetasBuses[i].setOpaque(true);
                etiquetasBuses[i].setBackground(new Color(50 + i*20, 100, 150));
                etiquetasBuses[i].setForeground(Color.WHITE);
                etiquetasBuses[i].setHorizontalAlignment(SwingConstants.CENTER);
                etiquetasBuses[i].setBounds(
                    posicionesIniciales[i][0] - 15, 
                    posicionesIniciales[i][1] - 15, 
                    30, 30);
                layeredPane.add(etiquetasBuses[i], JLayeredPane.POPUP_LAYER, 0);
            }
        }
    }
    
    private ImageIcon escalarImagen(ImageIcon icono, int ancho, int alto) {
        return new ImageIcon(icono.getImage()
            .getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
    }
    
    
    //Listeners y botones
    private void configurarListeners() {
        iniciarBtn.addActionListener(e -> Servidor.iniciarAutobuses());
        detenerBtn.addActionListener(e -> Servidor.detenerAutobuses());
        
        if (Servidor.getReloj() != null) {
            Servidor.getReloj().agregarListener(this);
        }
    }

    
    //Actualiza la hora en la interfax
    @Override
    public void horaActualizada(String hora) {
        SwingUtilities.invokeLater(() -> relojLabel.setText(hora));
    }

    public JLabel obtenerEtiquetaBus(int id) {
        if (id > 0 && id <= etiquetasBuses.length) {
            return etiquetasBuses[id - 1];
        }
        throw new IllegalArgumentException("ID de autobús no válido: " + id);
    }
    
    //Agrega el mensaje de actualización al log
    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensaje + "\n");
            // Auto-scroll al final
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
