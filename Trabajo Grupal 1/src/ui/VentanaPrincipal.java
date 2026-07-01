package ui;

import efectos.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class VentanaPrincipal extends JFrame {

    // Paleta de colores
    private static final Color C_FONDO      = new Color(15,  17,  26);
    private static final Color C_PANEL      = new Color(22,  25,  38);
    private static final Color C_CARD       = new Color(30,  34,  52);
    private static final Color C_BORDE      = new Color(45,  50,  80);
    private static final Color C_ACENTO     = new Color(99,  179, 237);
    private static final Color C_ACENTO2    = new Color(154, 117, 234);
    private static final Color C_EXITO      = new Color(72,  187, 120);
    private static final Color C_PELIGRO    = new Color(229, 62,  62);
    private static final Color C_TEXTO      = new Color(226, 232, 240);
    private static final Color C_TEXTO_DIM  = new Color(100, 110, 140);
    private static final Color C_SELECCION  = new Color(45,  80,  130);
    private static final Color C_ADVERTENCIA= new Color(246, 173, 85);

    // Estado
    private BufferedImage imagenOriginal;
    private BufferedImage imagenResultado;
    private boolean       imagenOriginalEsPng = false;
    private File          ultimoDirectorioCarga;
    private String        filtroActual = "";
    private String        ultimoFiltroAplicado = ""; // [MEJORA 3] tracking del último filtro

    // Componentes principales
    private ImagePanel    panelOriginal;
    private ImagePanel    panelResultado;
    private JList<String> listaFiltros;
    private JLabel        lblEstado;
    private JLabel        lblIndicadorEstado;  // [MEJORA 4] círculo de color en barra
    private JLabel        lblInfoOriginal;
    private JLabel        lblInfoResultado;
    private JButton       btnAplicar;
    private JButton       btnGuardar;
    private JButton       btnVerAmanecer;
    private JPanel        panelParams;
    private JLabel        lblFiltroActivo;     // [MEJORA 3] etiqueta filtro activo visible

    // Parámetros de filtros
    private JSpinner           spinnerN;
    private JComboBox<String>  comboRetro2Modo;
    private JComboBox<String>  comboKernel;
    private JSpinner           spinnerBrillo;
    private JSpinner           spinnerAlpha;
    private JSpinner           spinnerSatFactor;
    private JSpinner           spinnerBriloFactor;
    private JButton            btnColor1;
    private JButton            btnColor2;
    private Color              color1 = Color.RED;
    private Color              color2 = Color.BLUE;
    private JSpinner           spinnerAncho;
    private JSpinner           spinnerAlto;
    private JLabel             lblKernelActual;
    private JLabel             lblRetro2Actual;
    private JComboBox<String>  comboDireccion;
    private JSpinner           spinnerMascara;
    private JCheckBox          chkEscalar;

    // Convolución Amanecer ×10
    private BufferedImage[]    imagenesAmanecer;

    // Para arrastrar la ventana sin barra de título nativa
    private Point puntoArrastre;

    public VentanaPrincipal() {
        super("ImageGen Studio — UCE");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        setUndecorated(true);
        getContentPane().setBackground(C_FONDO);
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(crearBarraTitulo(), BorderLayout.NORTH);  // barra de título personalizada
        add(crearCuerpo(),      BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);
        actualizarEstado("listo", "Listo. Carga una imagen o genera una nueva.");
    }

    /** Barra de título personalizada que reemplaza la nativa de Windows. */
    private JPanel crearBarraTitulo() {
        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(C_PANEL);
        barra.setBorder(new EmptyBorder(0, 0, 0, 0));

        // — Fila superior: título de color —
        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(C_ACENTO.darker().darker());
        filaTitulo.setBorder(new EmptyBorder(6, 12, 6, 8));

        JLabel lblTitulo = new JLabel("ImaGen Studio — UCE");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        // Botones de ventana dibujados con Graphics2D (sin texto, sin encoding)
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setBackground(C_ACENTO.darker().darker());
        controles.setOpaque(true);

        JButton btnMin   = crearBotonVentanaIcono("min");
        JButton btnMax   = crearBotonVentanaIcono("max");
        JButton btnClose = crearBotonVentanaIcono("close");

        btnMin.addActionListener(e -> setState(ICONIFIED));
        btnMax.addActionListener(e -> {
            if (getExtendedState() == MAXIMIZED_BOTH) setExtendedState(NORMAL);
            else setExtendedState(MAXIMIZED_BOTH);
        });
        btnClose.addActionListener(e -> System.exit(0));

        controles.add(btnMin);
        controles.add(btnMax);
        controles.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controles, BorderLayout.EAST);

        // Arrastrar ventana desde esta barra
        filaTitulo.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                puntoArrastre = e.getPoint();
            }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - puntoArrastre.x,
                            loc.y + e.getY() - puntoArrastre.y);
            }
        });

        // — Fila inferior: header con logo y botones de acción —
        JPanel header = crearHeader();

        barra.add(filaTitulo, BorderLayout.NORTH);
        barra.add(header,     BorderLayout.CENTER);
        return barra;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, C_BORDE),
            new EmptyBorder(10, 18, 10, 18)
        ));

        JLabel lblTitulo = new JLabel("● ImaGen Studio");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(C_ACENTO);

        JLabel lblSub = new JLabel("  Procesamiento de Imágenes — UCE");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_TEXTO_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(lblTitulo);
        izq.add(lblSub);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);

        // [MEJORA 5] Botones con iconos Unicode reales en vez de espacios vacíos
        JButton btnCargar  = crearBoton("Cargar Imagen",    C_ACENTO);
        btnGuardar          = crearBoton("Guardar Resultado", C_EXITO);
        JButton btnLimpiar = crearBoton("Limpiar",           C_PELIGRO);

        btnGuardar.setEnabled(false);
        btnCargar.addActionListener(e  -> cargarImagen());
        btnGuardar.addActionListener(e -> guardarResultado());
        btnLimpiar.addActionListener(e -> limpiar());

        derecha.add(btnCargar);
        derecha.add(btnGuardar);
        derecha.add(btnLimpiar);

        header.add(izq,     BorderLayout.WEST);
        header.add(derecha, BorderLayout.EAST);
        return header;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CUERPO
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel crearCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 0));
        cuerpo.setBackground(C_FONDO);
        cuerpo.add(crearPanelIzquierdo(), BorderLayout.WEST);
        cuerpo.add(crearPanelCentral(),   BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        // [MEJORA 2] Ancho reducido de 240 a 210px para dar más espacio al canvas
        panel.setPreferredSize(new Dimension(210, 0));
        panel.setBackground(C_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 0, 1, C_BORDE),
            new EmptyBorder(10, 8, 10, 8)
        ));
        panel.add(crearListaFiltros(),           BorderLayout.CENTER);
        panel.add(crearPanelInferiorIzquierdo(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane crearListaFiltros() {
        String[] filtros = {
            " GENERACIÓN DE IMÁGENES",
            "Imagen Aleatoria",
            "Copiar Imagen",
            " CONVOLUCIÓN",
            "Convolución Manual",
            "Convolución Op",
            "Convolución Amanecer ×10",
            " COLOR",
            "Blanco y Negro",
            "Escala de Grises",
            "Escala de Grises HSV",
            "Efecto Retro 1",
            "Efecto Retro 2",
            "Filtro Negativo",
            "Histograma RGB",
            " EFECTOS HSV",
            "Filtros HSV",
            "Saturación HSV",
            "Brillo por Canal",
            "Canal Alpha",
            " DEGRADADOS",
            "Degradado Horizontal",
            "Degradado Vertical",
            "Degradado Radial",
            "Gradiente Radial",
            " ESPECIALES",
            "Desvanecimiento Circular",
            "Vidrio Esmerilado",
            " MÁSCARA / RECORTE",
            "Recorte de Bits"
        };

        listaFiltros = new JList<>(filtros);
        listaFiltros.setBackground(C_CARD);
        listaFiltros.setForeground(C_TEXTO);
        listaFiltros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listaFiltros.setSelectionBackground(C_SELECCION);
        listaFiltros.setSelectionForeground(Color.WHITE);
        listaFiltros.setFixedCellHeight(26);
        listaFiltros.setBorder(new EmptyBorder(0, 0, 0, 0));

        listaFiltros.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String texto = value.toString();
                boolean esCabecera = texto.equals(texto.toUpperCase()) && texto.length() > 3;

                if (esCabecera) {
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    lbl.setForeground(new Color(107, 125, 179));
                    lbl.setBackground(new Color(10, 12, 20));
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(1, 0, 1, 0, new Color(30, 34, 53)),
                        new EmptyBorder(6, 10, 4, 0)
                    ));
                    lbl.setEnabled(false);
                } else {
                    // [MEJORA 3] Resaltar el último filtro aplicado en la lista
                    boolean esUltimoAplicado = texto.trim().equals(ultimoFiltroAplicado.trim());
                    lbl.setFont(new Font("Segoe UI", esUltimoAplicado ? Font.BOLD : Font.PLAIN, 12));
                    lbl.setBorder(new EmptyBorder(2, 20, 2, 0));
                    if (!isSelected) {
                        lbl.setBackground(esUltimoAplicado ? new Color(20, 40, 70) : C_CARD);
                        lbl.setForeground(esUltimoAplicado ? C_ACENTO : C_TEXTO);
                    }
                }
                return lbl;
            }
        });

        listaFiltros.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = listaFiltros.getSelectedValue();
                if (sel != null) {
                    boolean esCabecera = sel.equals(sel.toUpperCase()) && sel.length() > 3;
                    if (esCabecera) { listaFiltros.clearSelection(); return; }
                    filtroActual = sel;
                    actualizarPanelParams(sel);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(listaFiltros);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(C_CARD);
        return scroll;
    }

    private JPanel crearPanelInferiorIzquierdo() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 6));
        contenedor.setOpaque(false);

        // [MEJORA 3] Etiqueta de filtro activo encima del panel de parámetros
        lblFiltroActivo = new JLabel("Sin filtro aplicado");
        lblFiltroActivo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblFiltroActivo.setForeground(C_TEXTO_DIM);
        lblFiltroActivo.setBorder(new EmptyBorder(0, 2, 4, 0));
        lblFiltroActivo.setOpaque(false);

        panelParams = new JPanel();
        panelParams.setBackground(C_CARD);
        panelParams.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDE),
            new EmptyBorder(8, 8, 8, 8)
        ));
        panelParams.setLayout(new BoxLayout(panelParams, BoxLayout.Y_AXIS));

        // [MEJORA 1] Placeholder visual con instrucciones de uso
        mostrarPlaceholderParams();

        spinnerN           = crearSpinner(255, 2, 255, 1);
        comboRetro2Modo    = new JComboBox<>(new String[]{"RG", "RB", "GB"});
        comboKernel        = new JComboBox<>(new String[]{
            "Normal", "Enfoque", "Desenfoque 3x3", "Desenfoque 9x9",
            "Bordes 4v", "Bordes 8v", "Aclaracion", "Oscurecer"
        });
        spinnerBrillo      = crearSpinner(25, -255, 255, 5);
        spinnerAlpha       = crearSpinner(150, 0, 255, 10);
        spinnerSatFactor   = crearSpinnerFloat(1.5, 0.1, 5.0, 0.1);
        spinnerBriloFactor = crearSpinnerFloat(1.5, 0.1, 5.0, 0.1);
        btnColor1          = crearBotonColor("Color inicio", color1);
        btnColor2          = crearBotonColor("Color fin",    color2);
        spinnerAncho       = crearSpinner(400, 50, 2000, 50);
        spinnerAlto        = crearSpinner(300, 50, 2000, 50);

        lblKernelActual = new JLabel("● " + comboKernel.getSelectedItem());
        lblKernelActual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblKernelActual.setForeground(C_ACENTO);
        lblKernelActual.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboKernel.addActionListener(ev ->
            lblKernelActual.setText("● " + comboKernel.getSelectedItem())
        );

        lblRetro2Actual = new JLabel("●  Modo: " + comboRetro2Modo.getSelectedItem());
        lblRetro2Actual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRetro2Actual.setForeground(C_ACENTO2);
        lblRetro2Actual.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboRetro2Modo.addActionListener(ev ->
            lblRetro2Actual.setText("●  Modo: " + comboRetro2Modo.getSelectedItem())
        );

        comboDireccion = new JComboBox<>(new String[]{
            "→  Izquierda → Derecha",
            "←  Derecha → Izquierda",
        });

        spinnerMascara = crearSpinner(15, 1, 255, 1);
        chkEscalar = new JCheckBox("Estirar al rango 0–255");
        chkEscalar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkEscalar.setForeground(C_TEXTO);
        chkEscalar.setBackground(C_CARD);
        chkEscalar.setSelected(true);
        chkEscalar.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAplicar = crearBoton("Aplicar Filtro", C_ACENTO);
        btnAplicar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnAplicar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAplicar.addActionListener(e -> aplicarFiltro());

        btnVerAmanecer = crearBoton("Ver 10 imagenes", C_ACENTO2);
        btnVerAmanecer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnVerAmanecer.setVisible(false);
        btnVerAmanecer.addActionListener(e -> mostrarDialogoAmanecer());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lblFiltroActivo, BorderLayout.CENTER);

        contenedor.add(top,        BorderLayout.NORTH);
        contenedor.add(panelParams, BorderLayout.CENTER);
        contenedor.add(btnAplicar,  BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        wrapper.add(contenedor,     BorderLayout.CENTER);
        wrapper.add(btnVerAmanecer, BorderLayout.SOUTH);
        return wrapper;
    }

    /** [MEJORA 1] Placeholder visual con instrucciones en el panel de parámetros. */
    private void mostrarPlaceholderParams() {
        panelParams.removeAll();

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);

        JLabel ico = new JLabel("Parámetros");
        ico.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        ico.setForeground(new Color(50, 58, 90));
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l1 = labelParam("Selecciona un filtro");
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l2 = labelParam("de la lista para ver");
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l3 = labelParam("sus parámetros aquí.");
        l3.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mini guía de uso
        JLabel sep = new JLabel("──────────────");
        sep.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        sep.setForeground(new Color(40, 48, 75));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel h1 = new JLabel("1. Cargar imagen");
        h1.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        h1.setForeground(new Color(70, 80, 115));
        h1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel h2 = new JLabel("Aplicar → Guardar");
        h2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        h2.setForeground(new Color(70, 80, 115));
        h2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel h3 = new JLabel("Scroll: zoom en imagen");
        h3.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        h3.setForeground(new Color(70, 80, 115));
        h3.setAlignmentX(Component.CENTER_ALIGNMENT);

        centro.add(Box.createVerticalGlue());
        centro.add(ico);
        centro.add(Box.createVerticalStrut(6));
        centro.add(l1); centro.add(l2); centro.add(l3);
        centro.add(Box.createVerticalStrut(8));
        centro.add(sep);
        centro.add(Box.createVerticalStrut(6));
        centro.add(h1);
        centro.add(Box.createVerticalStrut(2));
        centro.add(h2);
        centro.add(Box.createVerticalStrut(2));
        centro.add(h3);
        centro.add(Box.createVerticalGlue());

        panelParams.add(centro);
        panelParams.revalidate();
        panelParams.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PANEL CENTRAL
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel crearPanelCentral() {
        JPanel central = new JPanel(new GridLayout(1, 2, 8, 0));
        central.setBackground(C_FONDO);
        central.setBorder(new EmptyBorder(10, 10, 10, 10));
        central.add(crearTarjetaImagen("Imagen Original",  true));
        central.add(crearTarjetaImagen("Imagen Resultado", false));
        return central;
    }

    private JPanel crearTarjetaImagen(String titulo, boolean esOriginal) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 6));
        tarjeta.setBackground(C_CARD);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDE),
            new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(esOriginal ? C_ACENTO : C_ACENTO2);

        JLabel lblInfo = new JLabel("Sin imagen");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(C_TEXTO_DIM);
        lblInfo.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setOpaque(false);
        headerCard.add(lblTitulo, BorderLayout.WEST);
        headerCard.add(lblInfo,   BorderLayout.EAST);

        ImagePanel imgPanel = new ImagePanel(
            esOriginal ? "Carga una imagen con el botón de arriba"
                       : "Aplica un filtro para ver el resultado"
        );

        if (esOriginal) { panelOriginal = imgPanel;  lblInfoOriginal  = lblInfo; }
        else            { panelResultado = imgPanel; lblInfoResultado = lblInfo; }

        tarjeta.add(headerCard, BorderLayout.NORTH);
        tarjeta.add(imgPanel,   BorderLayout.CENTER);
        return tarjeta;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BARRA DE ESTADO MEJORADA [MEJORA 4]
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(C_PANEL);
        barra.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDE));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        izq.setOpaque(false);

        lblIndicadorEstado = new JLabel("●");
        lblIndicadorEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblIndicadorEstado.setForeground(C_EXITO);

        lblEstado = new JLabel("Listo");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEstado.setForeground(C_TEXTO);   // [MEJORA 4] texto más brillante, no dim

        izq.add(lblIndicadorEstado);
        izq.add(lblEstado);

        // Hint de zoom en el lado derecho
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        der.setOpaque(false);
        JLabel lblZoomHint = new JLabel("Scroll: zoom  |  Doble clic: reset zoom");
        lblZoomHint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblZoomHint.setForeground(C_TEXTO_DIM);
        der.add(lblZoomHint);

        barra.add(izq, BorderLayout.WEST);
        barra.add(der, BorderLayout.EAST);
        return barra;
    }

    /**
     * [MEJORA 4] Actualiza barra de estado con color según tipo:
     * "listo", "procesando", "exito", "error", "advertencia"
     */
    private void actualizarEstado(String tipo, String msg) {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(msg);
            switch (tipo) {
                case "exito"      -> { lblIndicadorEstado.setForeground(C_EXITO);      lblEstado.setForeground(C_TEXTO); }
                case "error"      -> { lblIndicadorEstado.setForeground(C_PELIGRO);    lblEstado.setForeground(C_PELIGRO); }
                case "procesando" -> { lblIndicadorEstado.setForeground(C_ADVERTENCIA);lblEstado.setForeground(C_TEXTO); }
                default           -> { lblIndicadorEstado.setForeground(C_EXITO);      lblEstado.setForeground(C_TEXTO_DIM); }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PARÁMETROS DE FILTROS
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarPanelParams(String filtro) {
        panelParams.removeAll();
        btnVerAmanecer.setVisible(false);

        switch (filtro) {
            case "Imagen Aleatoria" -> {
                panelParams.add(labelParam("Tamaño de la imagen:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaVertical("Ancho (px):", spinnerAncho));
                panelParams.add(Box.createVerticalStrut(3));
                panelParams.add(filaVertical("Alto (px):", spinnerAlto));
            }
            case "Escala de Grises", "Efecto Retro 1" -> {
                panelParams.add(labelParam("Niveles de color (N):"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(spinnerN));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(labelParam("N = 2 → max efecto"));
                panelParams.add(labelParam("N = 255 → sin cambio"));
            }
            case "Efecto Retro 2" -> {
                panelParams.add(labelParam("Niveles (N):"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(spinnerN));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Canales activos:"));
                panelParams.add(Box.createVerticalStrut(4));
                estilizarCombo(comboRetro2Modo);
                panelParams.add(filaCompleta(comboRetro2Modo));
                panelParams.add(Box.createVerticalStrut(4));
                lblRetro2Actual.setText(" Modo: " + comboRetro2Modo.getSelectedItem());
                panelParams.add(lblRetro2Actual);
            }
            case "Convolución Manual", "Convolución Op" -> {
                panelParams.add(labelParam("Tipo de kernel:"));
                panelParams.add(Box.createVerticalStrut(4));
                estilizarCombo(comboKernel);
                panelParams.add(filaCompleta(comboKernel));
                panelParams.add(Box.createVerticalStrut(4));
                JLabel lblSel = new JLabel("● " + comboKernel.getSelectedItem());
                lblSel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblSel.setForeground(C_ACENTO);
                lblSel.setAlignmentX(Component.LEFT_ALIGNMENT);
                comboKernel.addActionListener(ev -> lblSel.setText("● " + comboKernel.getSelectedItem()));
                panelParams.add(lblSel);
            }
            case "Convolución Amanecer ×10" -> {
                panelParams.add(labelParam("Genera 10 imágenes con"));
                panelParams.add(labelParam("intensidad 0% → 100%"));
                btnVerAmanecer.setVisible(true);
            }
            case "Brillo por Canal" -> {
                panelParams.add(labelParam("Brillo por canal (−255 a 255):"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(spinnerBrillo));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(labelParam("+ aclara  /  − oscurece"));
            }
            case "Canal Alpha" -> {
                panelParams.add(labelParam("Transparencia (0 a 255):"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(spinnerAlpha));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(labelParam("0 = transparente"));
                panelParams.add(labelParam("255 = opaco"));
            }
            case "Filtros HSV", "Saturación HSV" -> {
                panelParams.add(labelParam("Saturación (factor):"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(spinnerSatFactor));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Brillo (factor):"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(spinnerBriloFactor));
            }
            case "Degradado Horizontal" -> {
                comboDireccion.setModel(new DefaultComboBoxModel<>(new String[]{
                    "← Izquierda a → Derecha",
                    "→ Derecha a ← Izquierda"
                }));
                comboDireccion.setSelectedIndex(0);
                estilizarCombo(comboDireccion);
                panelParams.add(labelParam("Dirección:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(comboDireccion));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Colores:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaVertical("Inicio:", btnColor1));
                panelParams.add(Box.createVerticalStrut(3));
                panelParams.add(filaVertical("Fin:", btnColor2));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Tamaño:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaVertical("Ancho:", spinnerAncho));
                panelParams.add(Box.createVerticalStrut(3));
                panelParams.add(filaVertical("Alto:", spinnerAlto));
            }
            case "Degradado Vertical" -> {
                comboDireccion.setModel(new DefaultComboBoxModel<>(new String[]{
                    "↑  Arriba hacia ↓ Abajo",
                    "↓  Abajo hacia ↑ Arriba"
                }));
                comboDireccion.setSelectedIndex(0);
                estilizarCombo(comboDireccion);
                panelParams.add(labelParam("Dirección:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(comboDireccion));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Colores:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaVertical("Inicio:", btnColor1));
                panelParams.add(Box.createVerticalStrut(3));
                panelParams.add(filaVertical("Fin:", btnColor2));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Tamaño:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaVertical("Ancho:", spinnerAncho));
                panelParams.add(Box.createVerticalStrut(3));
                panelParams.add(filaVertical("Alto:", spinnerAlto));
            }
            case "Degradado Radial" -> {
                panelParams.add(labelParam("Colores:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaVertical("Centro:", btnColor1));
                panelParams.add(Box.createVerticalStrut(3));
                panelParams.add(filaVertical("Borde:", btnColor2));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Tamaño:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaVertical("Ancho:", spinnerAncho));
                panelParams.add(Box.createVerticalStrut(3));
                panelParams.add(filaVertical("Alto:", spinnerAlto));
            }
            case "Recorte de Bits" -> {
                panelParams.add(labelParam("Máscara de bits:"));
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(spinnerMascara));
                panelParams.add(Box.createVerticalStrut(2));
                panelParams.add(labelParam("15 = 4 bits  |  3 = 2 bits"));
                panelParams.add(Box.createVerticalStrut(8));
                chkEscalar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                chkEscalar.setForeground(C_TEXTO);
                chkEscalar.setBackground(C_CARD);
                chkEscalar.setSelected(true);
                chkEscalar.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelParams.add(chkEscalar);
            }
            default -> {
                JLabel lbl = labelParam("Sin parámetros adicionales");
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelParams.add(lbl);
            }
        }

        panelParams.revalidate();
        panelParams.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APLICAR FILTRO
    // ─────────────────────────────────────────────────────────────────────────

    private void aplicarFiltro() {
        if (filtroActual.isEmpty()) {
            mostrarError("Selecciona un filtro de la lista.");
            return;
        }

        boolean generacion = filtroActual.equals("Imagen Aleatoria")
                          || filtroActual.equals("Degradado Horizontal")
                          || filtroActual.equals("Degradado Vertical")
                          || filtroActual.equals("Degradado Radial");

        if (!generacion && imagenOriginal == null) {
            mostrarError("Carga una imagen primero con el botón [+].");
            return;
        }

        if (filtroActual.equals("Canal Alpha") && !imagenOriginalEsPng) {
            JOptionPane.showMessageDialog(
                this,
                "El filtro 'Canal Alpha' solo admite imágenes PNG.",
                "Formato no permitido",
                JOptionPane.WARNING_MESSAGE
            );
            actualizarEstado("advertencia", "Canal Alpha requiere PNG.");
            return;
        }

        actualizarEstado("procesando", "Procesando: " + filtroActual + "...");
        btnAplicar.setEnabled(false);

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() { return ejecutarFiltro(); }

            @Override
            protected void done() {
                try {
                    BufferedImage resultado = get();
                    if (resultado != null) {
                        imagenResultado    = resultado;
                        ultimoFiltroAplicado = filtroActual; // [MEJORA 3]

                        panelResultado.setImagen(resultado);
                        panelResultado.setFiltroActivo(filtroActual); // [MEJORA 3]

                        lblInfoResultado.setText(
                            resultado.getWidth() + " × " + resultado.getHeight() + " px");
                        btnGuardar.setEnabled(true);

                        // [MEJORA 3] Etiqueta visible del filtro aplicado
                        lblFiltroActivo.setText("Aplicado: " + filtroActual);
                        lblFiltroActivo.setForeground(C_EXITO);

                        // [MEJORA 6] Título dinámico con filtro aplicado
                        actualizarTitulo();

                        // Repintar lista para resaltar el filtro activo
                        listaFiltros.repaint();

                        // [MEJORA 4] Estado con color verde
                        actualizarEstado("exito", "Filtro aplicado: " + filtroActual);
                    }
                } catch (Exception e) {
                    mostrarError("Error al aplicar filtro: " + e.getMessage());
                    actualizarEstado("error", "Error al procesar.");
                } finally {
                    btnAplicar.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private BufferedImage ejecutarFiltro() {
        int ancho = (int) spinnerAncho.getValue();
        int alto  = (int) spinnerAlto.getValue();
        int N     = (int) spinnerN.getValue();

        return switch (filtroActual) {
            case "Imagen Aleatoria"         -> ProcesadorImagenes.imagenAleatoria(ancho, alto);
            case "Copiar Imagen"            -> ProcesadorImagenes.copiarImagen(imagenOriginal);
            case "Convolución Manual"       -> ProcesadorImagenes.convolucionManual(imagenOriginal, kernelSeleccionado2D());
            case "Convolución Op"           -> ProcesadorImagenes.convolucionOp(imagenOriginal, kernelSeleccionado1D());
            case "Convolución Amanecer ×10" -> {
                imagenesAmanecer = ProcesadorImagenes.convolucionAmanecer(imagenOriginal);
                SwingUtilities.invokeLater(() -> btnVerAmanecer.setVisible(true));
                yield imagenesAmanecer[9];
            }
            case "Blanco y Negro"           -> ProcesadorImagenes.blancoNegro(imagenOriginal);
            case "Escala de Grises"         -> ProcesadorImagenes.escalaGrises(imagenOriginal, N);
            case "Escala de Grises HSV"     -> ProcesadorImagenes.escalaGrisesHSV(imagenOriginal);
            case "Efecto Retro 1"           -> ProcesadorImagenes.efectorRetro1(imagenOriginal, N);
            case "Efecto Retro 2"           -> ProcesadorImagenes.efectorRetro2(
                                                  imagenOriginal, N,
                                                  (String) comboRetro2Modo.getSelectedItem());
            case "Filtro Negativo"          -> ProcesadorImagenes.filtroNegativo(imagenOriginal);
            case "Histograma RGB"           -> ProcesadorImagenes.generarHistograma(imagenOriginal);
            case "Filtros HSV"              -> ProcesadorImagenes.filtrosHSV(imagenOriginal,
                                                  valorFloat(spinnerSatFactor),
                                                  valorFloat(spinnerBriloFactor));
            case "Saturación HSV"           -> ProcesadorImagenes.saturacionHSV(imagenOriginal,
                                                  valorFloat(spinnerSatFactor),
                                                  valorFloat(spinnerBriloFactor));
            case "Brillo por Canal"         -> ProcesadorImagenes.brilloPorCanal(imagenOriginal,
                                                  (int) spinnerBrillo.getValue());
            case "Canal Alpha"              -> ProcesadorImagenes.canalAlpha(imagenOriginal,
                                                  (int) spinnerAlpha.getValue() / 255f * 1.5f);
            case "Degradado Horizontal"     -> {
                String dir = (String) comboDireccion.getSelectedItem();
                boolean inv = dir != null && dir.startsWith("←");
                yield ProcesadorImagenes.degradadoHorizontal(ancho, alto,
                    inv ? color2 : color1, inv ? color1 : color2);
            }
            case "Degradado Vertical"       -> {
                String dir = (String) comboDireccion.getSelectedItem();
                boolean inv = dir != null && dir.startsWith("↑");
                yield ProcesadorImagenes.degradadoVertical(ancho, alto,
                    inv ? color2 : color1, inv ? color1 : color2);
            }
            case "Degradado Radial"         -> ProcesadorImagenes.degradadoRadial(ancho, alto, color1, color2);
            case "Gradiente Radial"         -> ProcesadorImagenes.gradienteRadial(imagenOriginal);
            case "Desvanecimiento Circular" -> ProcesadorImagenes.desvanecimientoCircular(imagenOriginal);
            case "Vidrio Esmerilado"        -> ProcesadorImagenes.vidrioEsmerilado(imagenOriginal);
            case "Recorte de Bits"          -> ProcesadorImagenes.recorteBits(
                                                  imagenOriginal,
                                                  ((Integer) spinnerMascara.getValue()),
                                                  chkEscalar.isSelected());
            default -> null;
        };
    }

    private float[][] kernelSeleccionado2D() {
        float[] k = kernelSeleccionado1D();
        return new float[][]{{k[0],k[1],k[2]},{k[3],k[4],k[5]},{k[6],k[7],k[8]}};
    }

    private float[] kernelSeleccionado1D() {
        return switch ((String) comboKernel.getSelectedItem()) {
            case "Enfoque"        -> Kernels.kEnfoque;
            case "Desenfoque 3x3" -> Kernels.kDesenfoque;
            case "Desenfoque 9x9" -> Kernels.kDesenfoque9;
            case "Bordes 4v"      -> Kernels.kBordes;
            case "Bordes 8v"      -> Kernels.kBordes8;
            case "Aclaracion"     -> Kernels.kAclaracion;
            case "Oscurecer"      -> Kernels.kOscurecer;
            default               -> Kernels.kNormal;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DIÁLOGO AMANECER
    // ─────────────────────────────────────────────────────────────────────────

    private void mostrarDialogoAmanecer() {
        if (imagenesAmanecer == null) {
            mostrarError("Primero aplica el filtro 'Convolución Amanecer ×10'.");
            return;
        }
        JDialog dialogo = new JDialog(this, "  10 Imágenes — Convolución Amanecer", true);
        dialogo.setSize(900, 340);
        dialogo.setLocationRelativeTo(this);
        dialogo.getContentPane().setBackground(C_FONDO);

        JPanel grid = new JPanel(new GridLayout(2, 5, 6, 6));
        grid.setBackground(C_FONDO);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 10; i++) {
            final int idx = i;
            ImagePanel mini = new ImagePanel("...");
            mini.setImagen(imagenesAmanecer[i]);
            mini.setPreferredSize(new Dimension(160, 120));
            mini.setToolTipText("Imagen " + (i+1) + " — " + (int)((i/9f)*100) + "%");

            mini.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    imagenResultado = imagenesAmanecer[idx];
                    panelResultado.setImagen(imagenResultado);
                    lblInfoResultado.setText("Amanecer imagen " + (idx+1));
                    btnGuardar.setEnabled(true);
                    dialogo.dispose();
                }
                @Override public void mouseEntered(MouseEvent e) {
                    mini.setBorder(BorderFactory.createLineBorder(C_ACENTO, 2));
                }
                @Override public void mouseExited(MouseEvent e) {
                    mini.setBorder(BorderFactory.createLineBorder(C_BORDE, 1));
                }
            });

            JPanel celda = new JPanel(new BorderLayout());
            celda.setBackground(C_CARD);
            celda.add(mini, BorderLayout.CENTER);
            JLabel lbl = new JLabel("Img " + (i+1) + " — " + (int)((i/9f)*100) + "%",
                                    SwingConstants.CENTER);
            lbl.setForeground(C_TEXTO_DIM);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            celda.add(lbl, BorderLayout.SOUTH);
            grid.add(celda);
        }

        dialogo.add(grid);
        dialogo.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARGA / GUARDADO / LIMPIEZA
    // ─────────────────────────────────────────────────────────────────────────

    private void cargarImagen() {
        File dirInicial = (ultimoDirectorioCarga != null && ultimoDirectorioCarga.exists())
            ? ultimoDirectorioCarga
            : directorioInicialChooser();
        JFileChooser fc = new JFileChooser(dirInicial);
        fc.setDialogTitle("Seleccionar imagen");
        fc.addChoosableFileFilter(new FileNameExtensionFilter(
            "Imágenes (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp"));
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File seleccionado = fc.getSelectedFile();
            File carpetaSeleccionada = seleccionado != null ? seleccionado.getParentFile() : null;
            if (carpetaSeleccionada != null && carpetaSeleccionada.exists()) {
                ultimoDirectorioCarga = carpetaSeleccionada;
            }

            try {
                imagenOriginal = ImageIO.read(seleccionado);
                if (imagenOriginal == null) throw new Exception("Formato no soportado.");
                imagenOriginalEsPng = esArchivoPng(seleccionado);
                panelOriginal.setImagen(imagenOriginal);

                String nombre = seleccionado.getName();
                lblInfoOriginal.setText(imagenOriginal.getWidth() + " × "
                        + imagenOriginal.getHeight() + " px");

                // [MEJORA 6] Título dinámico con nombre del archivo
                actualizarTitulo(nombre);

                actualizarEstado("exito", "Imagen cargada: " + nombre);
            } catch (Exception e) {
                mostrarError("No se pudo cargar la imagen: " + e.getMessage());
                actualizarEstado("error", "Error al cargar imagen.");
            }
        }
    }

    private void guardarResultado() {
        if (imagenResultado == null) {
            mostrarError("No hay imagen para guardar.");
            return;
        }

        JFileChooser chooser = new JFileChooser(directorioInicialChooser());
        chooser.setDialogTitle("Guardar imagen");

        // Filtros de archivo
        FileNameExtensionFilter filtroJPG = new FileNameExtensionFilter("JPG", "jpg", "jpeg");
        FileNameExtensionFilter filtroPNG = new FileNameExtensionFilter("PNG", "png");

        chooser.addChoosableFileFilter(filtroJPG);
        chooser.addChoosableFileFilter(filtroPNG);
        chooser.setFileFilter(filtroPNG); // por defecto PNG

        int opcion = chooser.showSaveDialog(this);

        if (opcion == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            String formato = "png";

            FileNameExtensionFilter filtroSeleccionado =
                (FileNameExtensionFilter) chooser.getFileFilter();

            if (filtroSeleccionado == filtroJPG) {
                formato = "jpg";

                if (ProcesadorImagenes.tieneTransparenciaReal(imagenResultado)) {
                    int respuesta = JOptionPane.showConfirmDialog(
                        this,
                        "La imagen tiene transparencia.\n" +
                        "Si guardas como JPG se perderá.\n\n" +
                        "¿Deseas continuar?",
                        "Advertencia",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );

                    if (respuesta != JOptionPane.YES_OPTION) {
                        return; // cancelar guardado
                    }
                }
            }

            if (!archivo.getName().toLowerCase().endsWith("." + formato)) {
                archivo = new File(archivo.getAbsolutePath() + "." + formato);
            }

            try {
                BufferedImage imgGuardar = imagenResultado;

                if (formato.equals("jpg")) {
                    BufferedImage sinAlpha = new BufferedImage(
                        imgGuardar.getWidth(),
                        imgGuardar.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                    );

                    Graphics2D g = sinAlpha.createGraphics();
                    g.drawImage(imgGuardar, 0, 0, null);
                    g.dispose();

                    imgGuardar = sinAlpha;
                }

                ImageIO.write(imgGuardar, formato, archivo);

                actualizarEstado("exito", "Imagen guardada como " + formato.toUpperCase());
            } catch (Exception e) {
                mostrarError("Error al guardar: " + e.getMessage());
            }
        }
    }

    private void limpiar() {
        imagenOriginal = null; imagenResultado = null; imagenesAmanecer = null;
        imagenOriginalEsPng = false;
        ultimoFiltroAplicado = "";
        panelOriginal.setImagen(null);
        panelResultado.setImagen(null);
        panelResultado.setFiltroActivo("");
        lblInfoOriginal.setText("Sin imagen");
        lblInfoResultado.setText("Sin imagen");
        btnGuardar.setEnabled(false);
        listaFiltros.clearSelection();
        filtroActual = "";
        lblFiltroActivo.setText("Sin filtro aplicado");
        lblFiltroActivo.setForeground(C_TEXTO_DIM);
        mostrarPlaceholderParams();
        btnVerAmanecer.setVisible(false);
        setTitle("ImaGen Studio — UCE");  // [MEJORA 6] reset título
        listaFiltros.repaint();
        actualizarEstado("listo", "Listo. Carga una imagen o genera una nueva.");
    }

    private File directorioInicialChooser() {
        String home = System.getProperty("user.home");
        if (home != null && !home.isBlank()) {
            File dir = new File(home);
            if (dir.exists() && dir.isDirectory()) return dir;
        }
        return new File(".");
    }

    private boolean esArchivoPng(File archivo) {
        if (archivo == null) return false;
        String nombre = archivo.getName();
        return nombre != null && nombre.toLowerCase().endsWith(".png");
    }

    /** Actualiza el título cuando se carga una imagen. */
    private void actualizarTitulo(String nombreArchivo) {
        String titulo = "ImaGen Studio — UCE  |  " + nombreArchivo;
        if (!ultimoFiltroAplicado.isEmpty()) titulo += "  →  " + ultimoFiltroAplicado;
        setTitle(titulo);
    }

    /** Actualiza el título cuando se aplica un filtro. */
    private void actualizarTitulo() {
        String titulo = getTitle();
        // Quitar el filtro anterior si ya había uno
        if (titulo.contains("  →  ")) titulo = titulo.substring(0, titulo.indexOf("  →  "));
        setTitle(titulo + "  →  " + ultimoFiltroAplicado);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS UI
    // ─────────────────────────────────────────────────────────────────────────

    /** Boton de ventana con icono dibujado con Graphics2D (sin texto, sin problemas de encoding). */
    private JButton crearBotonVentanaIcono(String tipo) {
        Color colorBase  = C_ACENTO.darker().darker();
        Color colorHover = tipo.equals("close") ? C_PELIGRO : new Color(80, 130, 180);

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.8f));

                // Fondo
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Icono
                g2.setColor(Color.WHITE);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                switch (tipo) {
                    case "min"   -> g2.drawLine(cx - 5, cy + 2, cx + 5, cy + 2);
                    case "max"   -> g2.drawRect(cx - 5, cy - 4, 10, 9);
                    case "close" -> {
                        g2.drawLine(cx - 5, cy - 4, cx + 5, cy + 4);
                        g2.drawLine(cx + 5, cy - 4, cx - 5, cy + 4);
                    }
                }
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };

        btn.setBackground(colorBase);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(36, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(colorHover); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(colorBase);  btn.repaint(); }
        });
        return btn;
    }

    /** Boton pequeño para controles de ventana (min/max/close). */
    private JButton crearBotonVentana(String texto, Color colorBase, Color colorHover) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorBase);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(36, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(colorHover); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(colorBase);  btn.repaint(); }
        });
        return btn;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color.darker()); btn.repaint(); }
        });
        return btn;
    }

    private JButton crearBotonColor(String texto, Color inicial) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(inicial);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        btn.addActionListener(e -> {
            JColorChooser chooser = new JColorChooser(inicial);
            chooser.setPreviewPanel(new JPanel());
            JDialog dialogo = JColorChooser.createDialog(this, "Seleccionar color — " + texto,
                true, chooser,
                ev2 -> {
                    Color c = chooser.getColor();
                    btn.setBackground(c);
                    if (texto.contains("nicio") || texto.contains("entro")) color1 = c;
                    else color2 = c;
                }, null);
            dialogo.setVisible(true);
        });
        return btn;
    }

    private JLabel labelParam(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(C_TEXTO_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel filaCompleta(JComponent componente) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        estiloComponente(componente);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    private JPanel filaVertical(String etiqueta, JComponent componente) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(C_TEXTO_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel campo = new JPanel(new BorderLayout());
        campo.setOpaque(false);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        estiloComponente(componente);
        campo.add(componente, BorderLayout.CENTER);
        p.add(lbl);
        p.add(Box.createVerticalStrut(2));
        p.add(campo);
        return p;
    }

    private void estiloComponente(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (c instanceof JSpinner s) {
            s.getEditor().getComponent(0).setBackground(new Color(15, 17, 26));
            ((JComponent) s.getEditor().getComponent(0)).setForeground(C_TEXTO);
            s.setBackground(new Color(15, 17, 26));
            s.setForeground(C_TEXTO);
        }
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(15, 17, 26));
        combo.setForeground(C_TEXTO);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (isSelected) { lbl.setBackground(C_SELECCION); lbl.setForeground(Color.WHITE); }
                else            { lbl.setBackground(new Color(22, 25, 38)); lbl.setForeground(C_TEXTO); }
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });
    }

    private JSpinner crearSpinner(int val, int min, int max, int paso) {
        return new JSpinner(new SpinnerNumberModel(val, min, max, paso));
    }

    private JSpinner crearSpinnerFloat(double val, double min, double max, double paso) {
        return new JSpinner(new SpinnerNumberModel(val, min, max, paso));
    }

    private float valorFloat(JSpinner s) {
        return ((Number) s.getValue()).floatValue();
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        actualizarEstado("error", "Error: " + msg);
    }
}
