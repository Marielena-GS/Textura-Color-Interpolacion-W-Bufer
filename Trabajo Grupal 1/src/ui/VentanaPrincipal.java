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
    private static final Color C_FONDO = new Color(15, 17, 26);
    private static final Color C_PANEL = new Color(22, 25, 38);
    private static final Color C_CARD = new Color(30, 34, 52);
    private static final Color C_BORDE = new Color(45, 50, 80);
    private static final Color C_ACENTO = new Color(99, 179, 237);
    private static final Color C_ACENTO2 = new Color(154, 117, 234);
    private static final Color C_EXITO = new Color(72, 187, 120);
    private static final Color C_PELIGRO = new Color(229, 62, 62);
    private static final Color C_TEXTO = new Color(226, 232, 240);
    private static final Color C_TEXTO_DIM = new Color(100, 110, 140);
    private static final Color C_SELECCION = new Color(45, 80, 130);
    private static final Color C_ADVERTENCIA = new Color(246, 173, 85);

    // Estado
    private BufferedImage imagenOriginal;
    private BufferedImage imagenResultado;
    private BufferedImage imagenBlending;
    private boolean imagenOriginalEsPng = false;
    private File ultimoDirectorioCarga;
    private String filtroActual = "";
    private String ultimoFiltroAplicado = ""; // [MEJORA 3] tracking del último filtro

    // Componentes principales
    private ImagePanel panelOriginal;
    private ImagePanel panelResultado;
    private JList<String> listaFiltros;
    private JLabel lblEstado;
    private JLabel lblIndicadorEstado; // [MEJORA 4] círculo de color en barra
    private JLabel lblInfoOriginal;
    private JLabel lblInfoResultado;
    private JButton btnAplicar;
    private JButton btnGuardar;
    private JButton btnVerAmanecer;
    private JPanel panelParams;
    private JLabel lblFiltroActivo; // [MEJORA 3] etiqueta filtro activo visible

    // Parámetros de filtros
    private JSpinner spinnerN;
    private JComboBox<String> comboRetro2Modo;
    private JComboBox<String> comboKernel;
    private JSpinner spinnerBrillo;
    private JSpinner spinnerAlpha;
    private JSpinner spinnerSatFactor;
    private JSpinner spinnerBriloFactor;
    private JButton btnColor1;
    private JButton btnColor2;
    private Color color1 = Color.RED;
    private Color color2 = Color.BLUE;
    private JSpinner spinnerAncho;
    private JSpinner spinnerAlto;
    private JLabel lblKernelActual;
    private JLabel lblRetro2Actual;
    private JComboBox<String> comboDireccion;
    private JSpinner spinnerMascara;
    private JCheckBox chkEscalar;
    private JComboBox<String> comboBlendingModo;
    private JSlider sliderBlendingAlpha;
    private JLabel lblBlendingAlphaValor;
    private JButton btnImagenBlending;
    private JLabel lblImagenBlending;

    // Convolución Amanecer ×10
    private BufferedImage[] imagenesAmanecer;

    // Para arrastrar la ventana sin barra de título nativa
    private Point puntoArrastre;

    public VentanaPrincipal() {
        super("ImageGen Studio — UCE");
        try {
            File iconFile = new File("../assets/icon.png");
            if (!iconFile.exists()) iconFile = new File("assets/icon.png");
            setIconImage(ImageIO.read(iconFile));
        } catch (Exception e) { /* Ignorar si no se encuentra */ }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        setUndecorated(true);
        setExtendedState(MAXIMIZED_BOTH); // Iniciar maximizado
        getContentPane().setBackground(C_FONDO);
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(crearBarraTitulo(), BorderLayout.NORTH); // barra de título personalizada
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);
        actualizarEstado("listo", "Listo. Carga una imagen o genera una nueva.");
        configurarAtajosTeclado();
        configurarDragAndDrop();
    }

    private void configurarAtajosTeclado() {
        JRootPane root = getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("control O"), "cargar");
        actionMap.put("cargar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarImagen();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("control S"), "guardar");
        actionMap.put("guardar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarResultado();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "aplicar");
        actionMap.put("aplicar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(btnAplicar != null && btnAplicar.isEnabled()) {
                    btnAplicar.doClick();
                }
            }
        });
    }

    private void configurarDragAndDrop() {
        TransferHandler th = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
            }
            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                try {
                    java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File f = files.get(0);
                        BufferedImage img = ImageIO.read(f);
                        if (img != null) {
                            imagenOriginal = img;
                            panelOriginal.setImagen(imagenOriginal);
                            btnAplicar.setEnabled(true);
                            btnGuardar.setEnabled(false);
                            actualizarEstado("listo", "Imagen cargada desde arrastre: " + f.getName());
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Error al cargar imagen arrastrada", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        };
        setTransferHandler(th);
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
        try {
            File iconFile = new File("../assets/icon.png");
            if (!iconFile.exists()) iconFile = new File("assets/icon.png");
            Image img = ImageIO.read(iconFile).getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            lblTitulo.setIcon(new ImageIcon(img));
            lblTitulo.setIconTextGap(8);
        } catch (Exception e) {}

        // Botones de ventana dibujados con Graphics2D (sin texto, sin encoding)
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setBackground(C_ACENTO.darker().darker());
        controles.setOpaque(true);

        JButton btnMin = crearBotonVentanaIcono("min");
        JButton btnMax = crearBotonVentanaIcono("max");
        JButton btnClose = crearBotonVentanaIcono("close");

        btnMin.addActionListener(e -> setState(ICONIFIED));
        btnMax.addActionListener(e -> {
            if (getExtendedState() == MAXIMIZED_BOTH)
                setExtendedState(NORMAL);
            else
                setExtendedState(MAXIMIZED_BOTH);
        });
        btnClose.addActionListener(e -> System.exit(0));

        controles.add(btnMin);
        controles.add(btnMax);
        controles.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controles, BorderLayout.EAST);

        // Arrastrar ventana desde esta barra
        filaTitulo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                puntoArrastre = e.getPoint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (getExtendedState() == MAXIMIZED_BOTH) {
                        setExtendedState(NORMAL);
                    } else {
                        setExtendedState(MAXIMIZED_BOTH);
                    }
                }
            }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() == MAXIMIZED_BOTH) {
                    return; // No permitir arrastrar si está maximizada
                }
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - puntoArrastre.x,
                        loc.y + e.getY() - puntoArrastre.y);
            }
        });

        // — Fila inferior: header con logo y botones de acción —
        JPanel header = crearHeader();

        barra.add(filaTitulo, BorderLayout.NORTH);
        barra.add(header, BorderLayout.CENTER);
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
                new EmptyBorder(10, 18, 10, 18)));

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

        // [MEJORA 5] Botones
        JButton btnCargar = crearBoton("Cargar Imagen", C_ACENTO);
        btnCargar.setToolTipText("Cargar imagen desde el equipo (Ctrl + O)");
        btnGuardar = crearBoton("Guardar Resultado", C_EXITO);
        btnGuardar.setToolTipText("Guardar la imagen procesada (Ctrl + S)");
        JButton btnLimpiar = crearBoton("Limpiar", C_PELIGRO);

        btnGuardar.setEnabled(false);
        btnCargar.addActionListener(e -> cargarImagen());
        btnGuardar.addActionListener(e -> guardarResultado());
        btnLimpiar.addActionListener(e -> limpiar());

        derecha.add(btnCargar);
        derecha.add(btnGuardar);
        derecha.add(btnLimpiar);

        header.add(izq, BorderLayout.WEST);
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
        cuerpo.add(crearPanelCentral(), BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        // [MEJORA 2] Ancho reducido de 240 a 210px para dar más espacio al canvas
        panel.setPreferredSize(new Dimension(210, 0));
        panel.setBackground(C_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDE),
                new EmptyBorder(10, 8, 10, 8)));
        panel.add(crearPanelExposiciones(), BorderLayout.NORTH);
        panel.add(crearListaFiltros(), BorderLayout.CENTER);
        panel.add(crearPanelInferiorIzquierdo(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelExposiciones() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE),
                new EmptyBorder(8, 8, 8, 8)));

        JLabel titulo = new JLabel("MÓDULOS 3D / HARDWARE");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titulo.setForeground(new Color(107, 125, 179));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel("Técnicas avanzadas de renderizado");
        descripcion.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descripcion.setForeground(C_TEXTO_DIM);
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnGrupo1 = crearBoton("Rasterización 3D", C_ACENTO2);
        btnGrupo1.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrupo1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnGrupo1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGrupo1.addActionListener(e -> abrirGrupo1());

        JButton btnGrupo3 = crearBoton("Aceleración OpenGL", C_ACENTO2);
        btnGrupo3.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrupo3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnGrupo3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGrupo3.addActionListener(e -> mostrarDialogoGrupo3());

        JButton btnGrupo9 = crearBoton("Prueba de Profundidad", C_ACENTO2);
        btnGrupo9.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrupo9.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnGrupo9.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGrupo9.addActionListener(e -> mostrarDialogoGrupo9());

        JButton btnGrupo2 = crearBoton("Proyección W-Bufer", C_ACENTO2);
        btnGrupo2.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrupo2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnGrupo2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGrupo2.addActionListener(e -> mostrarDialogoGrupo2());

        JButton btnGrupo7 = crearBoton("Buffer Acumulación", C_ACENTO2);
        btnGrupo7.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrupo7.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnGrupo7.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGrupo7.addActionListener(e -> mostrarDialogoGrupo7());

        JButton btnGrupo8 = crearBoton("Prueba de Fragmentos", C_ACENTO2);
        btnGrupo8.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrupo8.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnGrupo8.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGrupo8.addActionListener(e -> mostrarDialogoGrupo8());

        card.add(titulo);
        card.add(Box.createVerticalStrut(4));
        card.add(descripcion);
        card.add(Box.createVerticalStrut(8));
        card.add(btnGrupo1);
        card.add(Box.createVerticalStrut(6));
        card.add(btnGrupo2);
        card.add(Box.createVerticalStrut(6));
        card.add(btnGrupo3);
        card.add(Box.createVerticalStrut(6));
        card.add(btnGrupo7);
        card.add(Box.createVerticalStrut(6));
        card.add(btnGrupo8);
        card.add(Box.createVerticalStrut(6));
        card.add(btnGrupo9);
        JButton btnEcualizador = crearBoton("Ecualizador e Histograma", C_ACENTO2);
        btnEcualizador.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEcualizador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnEcualizador.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnEcualizador.addActionListener(e -> mostrarDialogoEcualizador());

        card.add(Box.createVerticalStrut(6));
        card.add(btnEcualizador);
        return card;
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
                "Filtro Separable",
                " COLOR",
                "Blanco y Negro",
                "Escala de Grises",
                "Escala de Grises HSV",
                "Efecto Retro 1",
                "Efecto Retro 2",
                "Filtro Negativo",
                "Histograma RGB",
                "Blending / Mezcla",
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
                            new EmptyBorder(6, 10, 4, 0)));
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
                    if (esCabecera) {
                        listaFiltros.clearSelection();
                        return;
                    }
                    filtroActual = sel;
                    actualizarPanelParams(sel);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(listaFiltros);
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
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
                new EmptyBorder(8, 8, 8, 8)));
        panelParams.setLayout(new BoxLayout(panelParams, BoxLayout.Y_AXIS));

        // [MEJORA 1] Placeholder visual con instrucciones de uso
        mostrarPlaceholderParams();

        spinnerN = crearSpinner(255, 2, 255, 1);
        comboRetro2Modo = new JComboBox<>(new String[] { "RG", "RB", "GB" });
        comboKernel = new JComboBox<>(new String[] {
                "Normal", "Enfoque", "Desenfoque 3x3", "Desenfoque 9x9",
                "Bordes 4v", "Bordes 8v", "Aclaracion", "Oscurecer"
        });
        spinnerBrillo = crearSpinner(25, -255, 255, 5);
        spinnerAlpha = crearSpinner(150, 0, 255, 10);
        spinnerSatFactor = crearSpinnerFloat(1.5, 0.1, 5.0, 0.1);
        spinnerBriloFactor = crearSpinnerFloat(1.5, 0.1, 5.0, 0.1);
        btnColor1 = crearBotonColor("Color inicio", color1);
        btnColor2 = crearBotonColor("Color fin", color2);
        spinnerAncho = crearSpinner(400, 50, 2000, 50);
        spinnerAlto = crearSpinner(300, 50, 2000, 50);

        lblKernelActual = new JLabel("● " + comboKernel.getSelectedItem());
        lblKernelActual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblKernelActual.setForeground(C_ACENTO);
        lblKernelActual.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboKernel.addActionListener(ev -> lblKernelActual.setText("● " + comboKernel.getSelectedItem()));

        lblRetro2Actual = new JLabel("●  Modo: " + comboRetro2Modo.getSelectedItem());
        lblRetro2Actual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRetro2Actual.setForeground(C_ACENTO2);
        lblRetro2Actual.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboRetro2Modo
                .addActionListener(ev -> lblRetro2Actual.setText("●  Modo: " + comboRetro2Modo.getSelectedItem()));

        comboDireccion = new JComboBox<>(new String[] {
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

        comboBlendingModo = new JComboBox<>(new String[] { "Alpha", "Sumativa", "Multiplicativa" });
        sliderBlendingAlpha = crearSliderAlpha();
        lblBlendingAlphaValor = labelParam("Alpha: 50%");
        lblBlendingAlphaValor.setForeground(C_ACENTO);
        sliderBlendingAlpha.addChangeListener(
                e -> lblBlendingAlphaValor.setText("Alpha: " + sliderBlendingAlpha.getValue() + "%"));
        btnImagenBlending = crearBoton("Elegir segunda imagen", C_ACENTO2);
        lblImagenBlending = labelParam("Segunda imagen: no seleccionada");
        lblImagenBlending.setForeground(C_ADVERTENCIA);
        btnImagenBlending.addActionListener(e -> cargarImagenBlending());
        comboBlendingModo.addActionListener(e -> actualizarEstadoControlesBlending());

        btnAplicar = crearBoton("Aplicar Filtro", C_ACENTO);
        btnAplicar.setToolTipText("Procesar imagen con el filtro seleccionado (Enter)");
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

        contenedor.add(top, BorderLayout.NORTH);
        contenedor.add(panelParams, BorderLayout.CENTER);
        contenedor.add(btnAplicar, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        wrapper.add(contenedor, BorderLayout.CENTER);
        wrapper.add(btnVerAmanecer, BorderLayout.SOUTH);
        return wrapper;
    }

    /**
     * [MEJORA 1] Placeholder visual con instrucciones en el panel de parámetros.
     */
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
        centro.add(l1);
        centro.add(l2);
        centro.add(l3);
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
        central.add(crearTarjetaImagen("Imagen Original", true));
        central.add(crearTarjetaImagen("Imagen Resultado", false));
        return central;
    }

    private JPanel crearTarjetaImagen(String titulo, boolean esOriginal) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 6));
        tarjeta.setBackground(C_CARD);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE),
                new EmptyBorder(8, 8, 8, 8)));

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
        headerCard.add(lblInfo, BorderLayout.EAST);

        ImagePanel imgPanel = new ImagePanel(
                esOriginal ? "Carga una imagen con el botón de arriba"
                        : "Aplica un filtro para ver el resultado");

        if (esOriginal) {
            panelOriginal = imgPanel;
            lblInfoOriginal = lblInfo;
        } else {
            panelResultado = imgPanel;
            lblInfoResultado = lblInfo;
        }

        tarjeta.add(headerCard, BorderLayout.NORTH);
        tarjeta.add(imgPanel, BorderLayout.CENTER);
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
        lblEstado.setForeground(C_TEXTO); // [MEJORA 4] texto más brillante, no dim

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
                case "exito" -> {
                    lblIndicadorEstado.setForeground(C_EXITO);
                    lblEstado.setForeground(C_TEXTO);
                }
                case "error" -> {
                    lblIndicadorEstado.setForeground(C_PELIGRO);
                    lblEstado.setForeground(C_PELIGRO);
                }
                case "procesando" -> {
                    lblIndicadorEstado.setForeground(C_ADVERTENCIA);
                    lblEstado.setForeground(C_TEXTO);
                }
                default -> {
                    lblIndicadorEstado.setForeground(C_EXITO);
                    lblEstado.setForeground(C_TEXTO_DIM);
                }
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
            case "Blending / Mezcla" -> {
                panelParams.add(labelParam("Combina la imagen original"));
                panelParams.add(labelParam("con una segunda imagen."));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(lblImagenBlending);
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(btnImagenBlending));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(labelParam("Modo de mezcla:"));
                panelParams.add(Box.createVerticalStrut(4));
                estilizarCombo(comboBlendingModo);
                panelParams.add(filaCompleta(comboBlendingModo));
                panelParams.add(Box.createVerticalStrut(6));
                panelParams.add(lblBlendingAlphaValor);
                panelParams.add(Box.createVerticalStrut(4));
                panelParams.add(filaCompleta(sliderBlendingAlpha));
                panelParams.add(Box.createVerticalStrut(2));
                panelParams.add(labelParam("0% usa solo la original, 100% solo la segunda."));
                actualizarEstadoControlesBlending();
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
                comboDireccion.setModel(new DefaultComboBoxModel<>(new String[] {
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
                comboDireccion.setModel(new DefaultComboBoxModel<>(new String[] {
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
                    JOptionPane.WARNING_MESSAGE);
            actualizarEstado("advertencia", "Canal Alpha requiere PNG.");
            return;
        }

        if (filtroActual.equals("Blending / Mezcla") && imagenBlending == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una segunda imagen para usar Blending / Mezcla.",
                    "Falta segunda imagen",
                    JOptionPane.WARNING_MESSAGE);
            actualizarEstado("advertencia", "Blending / Mezcla requiere una segunda imagen.");
            return;
        }

        actualizarEstado("procesando", "Procesando: " + filtroActual + "...");
        btnAplicar.setEnabled(false);

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                return ejecutarFiltro();
            }

            @Override
            protected void done() {
                try {
                    BufferedImage resultado = get();
                    if (resultado != null) {
                        imagenResultado = resultado;
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
        int alto = (int) spinnerAlto.getValue();
        int N = (int) spinnerN.getValue();

        return switch (filtroActual) {
            case "Imagen Aleatoria" -> ProcesadorImagenes.imagenAleatoria(ancho, alto);
            case "Copiar Imagen" -> ProcesadorImagenes.copiarImagen(imagenOriginal);
            case "Convolución Manual" -> ProcesadorImagenes.convolucionManual(imagenOriginal, kernelSeleccionado2D());
            case "Convolución Op" -> ProcesadorImagenes.convolucionOp(imagenOriginal, kernelSeleccionado1D());
            case "Filtro Separable" -> ProcesadorImagenes.AplicarFiltroSeparable(imagenOriginal, new double[]{1.0/4.0, 2.0/4.0, 1.0/4.0});
            case "Convolución Amanecer ×10" -> {
                imagenesAmanecer = ProcesadorImagenes.convolucionAmanecer(imagenOriginal);
                SwingUtilities.invokeLater(() -> btnVerAmanecer.setVisible(true));
                yield imagenesAmanecer[9];
            }
            case "Blanco y Negro" -> ProcesadorImagenes.blancoNegro(imagenOriginal);
            case "Escala de Grises" -> ProcesadorImagenes.escalaGrises(imagenOriginal, N);
            case "Escala de Grises HSV" -> ProcesadorImagenes.escalaGrisesHSV(imagenOriginal);
            case "Efecto Retro 1" -> ProcesadorImagenes.efectorRetro1(imagenOriginal, N);
            case "Efecto Retro 2" -> ProcesadorImagenes.efectorRetro2(
                    imagenOriginal, N,
                    (String) comboRetro2Modo.getSelectedItem());
            case "Filtro Negativo" -> ProcesadorImagenes.filtroNegativo(imagenOriginal);
            case "Histograma RGB" -> ProcesadorImagenes.generarHistograma(imagenOriginal);
            case "Blending / Mezcla" -> ProcesadorImagenes.blending(
                    imagenOriginal,
                    imagenBlending,
                    (String) comboBlendingModo.getSelectedItem(),
                    sliderBlendingAlpha.getValue() / 100f);
            case "Filtros HSV" -> ProcesadorImagenes.filtrosHSV(imagenOriginal,
                    valorFloat(spinnerSatFactor),
                    valorFloat(spinnerBriloFactor));
            case "Saturación HSV" -> ProcesadorImagenes.saturacionHSV(imagenOriginal,
                    valorFloat(spinnerSatFactor),
                    valorFloat(spinnerBriloFactor));
            case "Brillo por Canal" -> ProcesadorImagenes.brilloPorCanal(imagenOriginal,
                    (int) spinnerBrillo.getValue());
            case "Canal Alpha" -> ProcesadorImagenes.canalAlpha(imagenOriginal,
                    (int) spinnerAlpha.getValue() / 255f * 1.5f);
            case "Degradado Horizontal" -> {
                String dir = (String) comboDireccion.getSelectedItem();
                boolean inv = dir != null && dir.startsWith("←");
                yield ProcesadorImagenes.degradadoHorizontal(ancho, alto,
                        inv ? color2 : color1, inv ? color1 : color2);
            }
            case "Degradado Vertical" -> {
                String dir = (String) comboDireccion.getSelectedItem();
                boolean inv = dir != null && dir.startsWith("↑");
                yield ProcesadorImagenes.degradadoVertical(ancho, alto,
                        inv ? color2 : color1, inv ? color1 : color2);
            }
            case "Degradado Radial" -> ProcesadorImagenes.degradadoRadial(ancho, alto, color1, color2);
            case "Gradiente Radial" -> ProcesadorImagenes.gradienteRadial(imagenOriginal);
            case "Desvanecimiento Circular" -> ProcesadorImagenes.desvanecimientoCircular(imagenOriginal);
            case "Vidrio Esmerilado" -> ProcesadorImagenes.vidrioEsmerilado(imagenOriginal);
            case "Recorte de Bits" -> ProcesadorImagenes.recorteBits(
                    imagenOriginal,
                    ((Integer) spinnerMascara.getValue()),
                    chkEscalar.isSelected());
            default -> null;
        };
    }

    private float[][] kernelSeleccionado2D() {
        float[] k = kernelSeleccionado1D();
        return new float[][] { { k[0], k[1], k[2] }, { k[3], k[4], k[5] }, { k[6], k[7], k[8] } };
    }

    private float[] kernelSeleccionado1D() {
        return switch ((String) comboKernel.getSelectedItem()) {
            case "Enfoque" -> Kernels.kEnfoque;
            case "Desenfoque 3x3" -> Kernels.kDesenfoque;
            case "Desenfoque 9x9" -> Kernels.kDesenfoque9;
            case "Bordes 4v" -> Kernels.kBordes;
            case "Bordes 8v" -> Kernels.kBordes8;
            case "Aclaracion" -> Kernels.kAclaracion;
            case "Oscurecer" -> Kernels.kOscurecer;
            default -> Kernels.kNormal;
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
            mini.setToolTipText("Imagen " + (i + 1) + " — " + (int) ((i / 9f) * 100) + "%");

            mini.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    imagenResultado = imagenesAmanecer[idx];
                    panelResultado.setImagen(imagenResultado);
                    lblInfoResultado.setText("Amanecer imagen " + (idx + 1));
                    btnGuardar.setEnabled(true);
                    dialogo.dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    mini.setBorder(BorderFactory.createLineBorder(C_ACENTO, 2));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mini.setBorder(BorderFactory.createLineBorder(C_BORDE, 1));
                }
            });

            JPanel celda = new JPanel(new BorderLayout());
            celda.setBackground(C_CARD);
            celda.add(mini, BorderLayout.CENTER);
            JLabel lbl = new JLabel("Img " + (i + 1) + " — " + (int) ((i / 9f) * 100) + "%",
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
                if (imagenOriginal == null)
                    throw new Exception("Formato no soportado.");
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

    private void cargarImagenBlending() {
        File dirInicial = (ultimoDirectorioCarga != null && ultimoDirectorioCarga.exists())
                ? ultimoDirectorioCarga
                : directorioInicialChooser();
        JFileChooser fc = new JFileChooser(dirInicial);
        fc.setDialogTitle("Seleccionar segunda imagen");
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
                BufferedImage imagen = ImageIO.read(seleccionado);
                if (imagen == null)
                    throw new Exception("Formato no soportado.");
                imagenBlending = imagen;
                lblImagenBlending.setText("Segunda imagen: " + seleccionado.getName());
                lblImagenBlending.setForeground(C_EXITO);
                actualizarEstado("exito", "Segunda imagen cargada: " + seleccionado.getName());
            } catch (Exception e) {
                mostrarError("No se pudo cargar la segunda imagen: " + e.getMessage());
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

            FileNameExtensionFilter filtroSeleccionado = (FileNameExtensionFilter) chooser.getFileFilter();

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
                            JOptionPane.WARNING_MESSAGE);

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
                            BufferedImage.TYPE_INT_RGB);

                    Graphics2D g = sinAlpha.createGraphics();
                    g.drawImage(imgGuardar, 0, 0, null);
                    g.dispose();

                    imgGuardar = sinAlpha;
                }

                ImageIO.write(imgGuardar, formato, archivo);
                JOptionPane.showMessageDialog(this, "Imagen guardada con éxito.", "Guardar", JOptionPane.INFORMATION_MESSAGE);
                actualizarEstado("exito", "Imagen guardada como " + formato.toUpperCase());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiar() {
        imagenOriginal = null;
        imagenResultado = null;
        imagenesAmanecer = null;
        imagenBlending = null;
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
        if (lblImagenBlending != null) {
            lblImagenBlending.setText("Segunda imagen: no seleccionada");
            lblImagenBlending.setForeground(C_ADVERTENCIA);
        }
        mostrarPlaceholderParams();
        btnVerAmanecer.setVisible(false);
        setTitle("ImaGen Studio — UCE"); // [MEJORA 6] reset título
        listaFiltros.repaint();
        actualizarEstado("listo", "Listo. Carga una imagen o genera una nueva.");
    }

    private void actualizarEstadoControlesBlending() {
        if (sliderBlendingAlpha == null || comboBlendingModo == null || lblBlendingAlphaValor == null)
            return;
        boolean usaAlpha = "Alpha".equals(comboBlendingModo.getSelectedItem());
        sliderBlendingAlpha.setEnabled(usaAlpha);
        lblBlendingAlphaValor.setEnabled(usaAlpha);
    }

    private File directorioInicialChooser() {
        String home = System.getProperty("user.home");
        if (home != null && !home.isBlank()) {
            File dir = new File(home);
            if (dir.exists() && dir.isDirectory())
                return dir;
        }
        return new File(".");
    }

    private boolean esArchivoPng(File archivo) {
        if (archivo == null)
            return false;
        String nombre = archivo.getName();
        return nombre != null && nombre.toLowerCase().endsWith(".png");
    }

    /** Actualiza el título cuando se carga una imagen. */
    private void actualizarTitulo(String nombreArchivo) {
        String titulo = "ImaGen Studio — UCE  |  " + nombreArchivo;
        if (!ultimoFiltroAplicado.isEmpty())
            titulo += "  →  " + ultimoFiltroAplicado;
        setTitle(titulo);
    }

    /** Actualiza el título cuando se aplica un filtro. */
    private void actualizarTitulo() {
        String titulo = getTitle();
        // Quitar el filtro anterior si ya había uno
        if (titulo.contains("  →  "))
            titulo = titulo.substring(0, titulo.indexOf("  →  "));
        setTitle(titulo + "  →  " + ultimoFiltroAplicado);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS UI
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Boton de ventana con icono dibujado con Graphics2D (sin texto, sin problemas
     * de encoding).
     */
    private JButton crearBotonVentanaIcono(String tipo) {
        Color colorBase = C_ACENTO.darker().darker();
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
                    case "min" -> g2.drawLine(cx - 5, cy + 2, cx + 5, cy + 2);
                    case "max" -> g2.drawRect(cx - 5, cy - 4, 10, 9);
                    case "close" -> {
                        g2.drawLine(cx - 5, cy - 4, cx + 5, cy + 4);
                        g2.drawLine(cx + 5, cy - 4, cx - 5, cy + 4);
                    }
                }
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
            }
        };

        btn.setBackground(colorBase);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(36, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(colorHover);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(colorBase);
                btn.repaint();
            }
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

            @Override
            protected void paintBorder(Graphics g) {
            }
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
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color.darker());
                btn.repaint();
            }
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

            @Override
            protected void paintBorder(Graphics g) {
            }
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
                        if (texto.contains("nicio") || texto.contains("entro"))
                            color1 = c;
                        else
                            color2 = c;
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
                if (isSelected) {
                    lbl.setBackground(C_SELECCION);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(new Color(22, 25, 38));
                    lbl.setForeground(C_TEXTO);
                }
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

    private JSlider crearSliderAlpha() {
        JSlider slider = new JSlider(0, 100, 50);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(false);
        slider.setBackground(C_CARD);
        slider.setForeground(C_TEXTO_DIM);
        slider.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        slider.setOpaque(false);
        return slider;
    }

    private float valorFloat(JSpinner s) {
        return ((Number) s.getValue()).floatValue();
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        actualizarEstado("error", "Error: " + msg);
    }

    private void abrirGrupo1() {
        actualizarEstado("procesando", "Abriendo exposición grupo 1...");
        SwingUtilities.invokeLater(() -> {
            try {
                efectos.exposiciones.grupo1.Main ventanaGrupo1 = new efectos.exposiciones.grupo1.Main();
                JRootPane root = ventanaGrupo1.getRootPane();
                InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                ActionMap am = root.getActionMap();
                im.put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
                am.put("cerrar", new AbstractAction() {
                    public void actionPerformed(ActionEvent e) { ventanaGrupo1.dispose(); }
                });
                ventanaGrupo1.setVisible(true);
                actualizarEstado("exito", "Exposición grupo 1 abierta.");
            } catch (Throwable e) {
                mostrarError("No se pudo abrir la exposición grupo 1: " + e.getMessage());
            }
        });
    }

    private void mostrarDialogoGrupo9() {
        JDialog dialog = new JDialog(this, "Exposición Grupo 9", true);
        dialog.setSize(1280, 780);
        dialog.setMinimumSize(new Dimension(960, 620));
        dialog.setUndecorated(true);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(C_FONDO);
        dialog.setLayout(new BorderLayout());

        // Header mimicking main window
        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(C_PANEL);
        barra.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(C_ACENTO.darker().darker());
        filaTitulo.setBorder(new EmptyBorder(6, 12, 6, 8));

        JLabel lblTitulo = new JLabel("ImaGen Studio — Grupo 9");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setBackground(C_ACENTO.darker().darker());
        controles.setOpaque(true);

        JButton btnClose = crearBotonVentanaIcono("close");
        btnClose.addActionListener(e -> dialog.dispose());
        controles.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controles, BorderLayout.EAST);

        final Point[] dragPoint = new Point[1];
        filaTitulo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = dialog.getLocation();
                dialog.setLocation(loc.x + e.getX() - dragPoint[0].x, loc.y + e.getY() - dragPoint[0].y);
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(10, 18, 10, 18)));

        JLabel lblH = new JLabel("● ImaGen Studio");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblH.setForeground(C_ACENTO2);

        JLabel lblSub = new JLabel("  Filtros Grupo 9 — Exposición");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_TEXTO_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(lblH);
        izq.add(lblSub);
        
        header.add(izq, BorderLayout.WEST);

        barra.add(filaTitulo, BorderLayout.NORTH);
        barra.add(header, BorderLayout.CENTER);
        
        dialog.add(barra, BorderLayout.NORTH);

        // Center Images
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setBackground(C_FONDO);
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        ImagePanel panelImg1 = new ImagePanel("Imagen Original");
        ImagePanel panelResult = new ImagePanel("Resultado Final");

        JPanel cardImg1 = new JPanel(new BorderLayout(0, 6));
        cardImg1.setBackground(C_CARD);
        cardImg1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblImg1 = new JLabel("Imagen 1 (Fondo/Principal)");
        lblImg1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblImg1.setForeground(C_TEXTO);
        cardImg1.add(lblImg1, BorderLayout.NORTH);
        cardImg1.add(panelImg1, BorderLayout.CENTER);

        JPanel cardResult = new JPanel(new BorderLayout(0, 6));
        cardResult.setBackground(C_CARD);
        cardResult.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblResult = new JLabel("Imagen Resultado");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResult.setForeground(C_ACENTO2);
        cardResult.add(lblResult, BorderLayout.NORTH);
        cardResult.add(panelResult, BorderLayout.CENTER);

        centerPanel.add(cardImg1);
        centerPanel.add(cardResult);
        dialog.add(centerPanel, BorderLayout.CENTER);

        // Left Panel
        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setBackground(C_PANEL);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDE),
                new EmptyBorder(15, 12, 15, 12)));
        leftPanel.setPreferredSize(new Dimension(280, 0));

        String[] filtros = {
                "Alpha Test",
                "Depth Test",
                "Logic Op",
                "Stencil Test",
                "Blending (G9)"
        };
        
        JList<String> listaFiltrosG9 = new JList<>(filtros);
        listaFiltrosG9.setBackground(C_CARD);
        listaFiltrosG9.setForeground(C_TEXTO);
        listaFiltrosG9.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listaFiltrosG9.setSelectionBackground(C_SELECCION);
        listaFiltrosG9.setSelectionForeground(Color.WHITE);
        listaFiltrosG9.setFixedCellHeight(30);
        listaFiltrosG9.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        listaFiltrosG9.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(2, 10, 2, 0));
                return lbl;
            }
        });
        
        JScrollPane scrollFiltros = new JScrollPane(listaFiltrosG9);
        scrollFiltros.setBorder(BorderFactory.createLineBorder(C_BORDE));
        scrollFiltros.getViewport().setBackground(C_CARD);
        scrollFiltros.setPreferredSize(new Dimension(260, 160));

        JPanel panelParamsG9 = new JPanel();
        panelParamsG9.setLayout(new BoxLayout(panelParamsG9, BoxLayout.Y_AXIS));
        panelParamsG9.setBackground(C_CARD);
        panelParamsG9.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(10, 10, 10, 10)));

        JSlider sUmbral = new JSlider(0, 255, 128);
        sUmbral.setOpaque(false);
        sUmbral.setForeground(C_TEXTO_DIM);
        JLabel lblUmbral = labelParam("Umbral: 128");
        sUmbral.addChangeListener(e -> lblUmbral.setText("Umbral: " + sUmbral.getValue()));

        JSlider sZ1 = new JSlider(-100, 100, 10);
        sZ1.setOpaque(false);
        sZ1.setForeground(C_TEXTO_DIM);
        JLabel lblZ1 = labelParam("Z Fondo: 10");
        sZ1.addChangeListener(e -> lblZ1.setText("Z Fondo: " + sZ1.getValue()));

        JSlider sZ2 = new JSlider(-100, 100, 5);
        sZ2.setOpaque(false);
        sZ2.setForeground(C_TEXTO_DIM);
        JLabel lblZ2 = labelParam("Z Frente: 5");
        sZ2.addChangeListener(e -> lblZ2.setText("Z Frente: " + sZ2.getValue()));

        JComboBox<String> cLogic = new JComboBox<>(new String[]{"XOR", "AND", "OR"});
        estilizarCombo(cLogic);
        
        JSlider sRadio = new JSlider(10, 1000, 150);
        sRadio.setOpaque(false);
        sRadio.setForeground(C_TEXTO_DIM);
        JLabel lblRadio = labelParam("Radio (px): 150");
        sRadio.addChangeListener(e -> lblRadio.setText("Radio (px): " + sRadio.getValue()));

        JSlider sAlpha = crearSliderAlpha();
        JLabel lblAlpha = labelParam("Alpha: 50%");
        sAlpha.addChangeListener(e -> lblAlpha.setText("Alpha: " + sAlpha.getValue() + "%"));

        JButton btnImg1 = crearBoton("Cargar Imagen 1", C_ACENTO);
        JButton btnImg2 = crearBoton("Cargar Imagen 2", C_ACENTO2);
        JButton btnAplicarG9 = crearBoton("Aplicar Filtro", C_EXITO);
        JButton btnGuardarG9 = crearBoton("Guardar Resultado", C_EXITO);
        btnGuardarG9.setEnabled(false);

        final BufferedImage[] imgs = new BufferedImage[2];
        final BufferedImage[] resultadoLocal = new BufferedImage[1];
        final String[] filtroActivo = {""};

        btnImg1.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(ultimoDirectorioCarga);
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    imgs[0] = ImageIO.read(fc.getSelectedFile());
                    panelImg1.setImagen(imgs[0]);
                    ultimoDirectorioCarga = fc.getSelectedFile().getParentFile();
                } catch (Exception ex) {
                }
            }
        });

        btnImg2.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(ultimoDirectorioCarga);
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    imgs[1] = ImageIO.read(fc.getSelectedFile());
                    ultimoDirectorioCarga = fc.getSelectedFile().getParentFile();
                    JOptionPane.showMessageDialog(dialog, "Imagen 2 (Frente) cargada correctamente: " + fc.getSelectedFile().getName(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                }
            }
        });
        
        btnGuardarG9.addActionListener(e -> {
            if (resultadoLocal[0] == null) return;
            JFileChooser chooser = new JFileChooser(ultimoDirectorioCarga);
            chooser.setDialogTitle("Guardar imagen");
            chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".png")) {
                    archivo = new File(archivo.getParentFile(), archivo.getName() + ".png");
                }
                try {
                    ImageIO.write(resultadoLocal[0], "png", archivo);
                    JOptionPane.showMessageDialog(dialog, "Guardado exitoso");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar");
                }
            }
        });

        Runnable updateParams = () -> {
            panelParamsG9.removeAll();
            String sel = listaFiltrosG9.getSelectedValue();
            if (sel == null) {
                panelParamsG9.add(labelParam("Selecciona un filtro."));
                panelParamsG9.revalidate();
                panelParamsG9.repaint();
                return;
            }
            filtroActivo[0] = sel;
            
            JLabel lblFiltro = labelParam("Parámetros: " + sel);
            lblFiltro.setForeground(C_ACENTO2);
            panelParamsG9.add(lblFiltro);
            panelParamsG9.add(Box.createVerticalStrut(10));
            btnImg2.setVisible(sel.equals("Depth Test") || sel.equals("Logic Op") || sel.equals("Blending (G9)"));

            if (sel.equals("Alpha Test")) {
                panelParamsG9.add(lblUmbral);
                panelParamsG9.add(Box.createVerticalStrut(4));
                panelParamsG9.add(filaCompleta(sUmbral));
            } else if (sel.equals("Depth Test")) {
                panelParamsG9.add(lblZ1);
                panelParamsG9.add(Box.createVerticalStrut(4));
                panelParamsG9.add(filaCompleta(sZ1));
                panelParamsG9.add(Box.createVerticalStrut(10));
                panelParamsG9.add(lblZ2);
                panelParamsG9.add(Box.createVerticalStrut(4));
                panelParamsG9.add(filaCompleta(sZ2));
            } else if (sel.equals("Logic Op")) {
                panelParamsG9.add(labelParam("Operación:"));
                panelParamsG9.add(Box.createVerticalStrut(4));
                panelParamsG9.add(filaCompleta(cLogic));
            } else if (sel.equals("Stencil Test")) {
                panelParamsG9.add(lblRadio);
                panelParamsG9.add(Box.createVerticalStrut(4));
                panelParamsG9.add(filaCompleta(sRadio));
            } else if (sel.equals("Blending (G9)")) {
                panelParamsG9.add(lblAlpha);
                panelParamsG9.add(Box.createVerticalStrut(4));
                panelParamsG9.add(filaCompleta(sAlpha));
            }
            panelParamsG9.revalidate();
            panelParamsG9.repaint();
        };

        listaFiltrosG9.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateParams.run();
        });
        
        listaFiltrosG9.setSelectedIndex(0);

        btnAplicarG9.addActionListener(e -> {
            if (imgs[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Por favor carga la Imagen 1 primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sel = filtroActivo[0];
            if (sel.isEmpty()) return;
            
            if ((sel.equals("Depth Test") || sel.equals("Logic Op") || sel.equals("Blending (G9)")) && imgs[1] == null) {
                JOptionPane.showMessageDialog(dialog, "Este filtro requiere la Imagen 2.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BufferedImage res = null;
                switch (sel) {
                    case "Alpha Test" ->
                            res = ProcesadorImagenes.alphaTestGrupo9(imgs[0], sUmbral.getValue());
                    case "Depth Test" ->
                            res = ProcesadorImagenes.depthTestGrupo9(imgs[0], imgs[1], (float) sZ1.getValue(), (float) sZ2.getValue());
                    case "Logic Op" ->
                            res = ProcesadorImagenes.logicOpGrupo9(imgs[0], imgs[1], (String) cLogic.getSelectedItem());
                    case "Stencil Test" ->
                            res = ProcesadorImagenes.stencilTestGrupo9(imgs[0], sRadio.getValue());
                    case "Blending (G9)" ->
                            res = ProcesadorImagenes.blendingGrupo9(imgs[0], imgs[1], sAlpha.getValue() / 100f);
                }
                if (res != null) {
                    resultadoLocal[0] = res;
                    panelResult.setImagen(res);
                    btnGuardarG9.setEnabled(true);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al procesar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panelAcciones = new JPanel();
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setOpaque(false);
        panelAcciones.add(filaCompleta(btnImg1));
        panelAcciones.add(Box.createVerticalStrut(6));
        panelAcciones.add(filaCompleta(btnImg2));
        panelAcciones.add(Box.createVerticalStrut(15));
        panelAcciones.add(panelParamsG9);
        panelAcciones.add(Box.createVerticalGlue());
        panelAcciones.add(filaCompleta(btnAplicarG9));
        panelAcciones.add(Box.createVerticalStrut(6));
        panelAcciones.add(filaCompleta(btnGuardarG9));

        leftPanel.add(scrollFiltros, BorderLayout.NORTH);
        leftPanel.add(panelAcciones, BorderLayout.CENTER);

        dialog.add(leftPanel, BorderLayout.WEST);
        JRootPane root = dialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
        am.put("cerrar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });
        im.put(KeyStroke.getKeyStroke("control O"), "cargar");
        am.put("cargar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { btnImg1.doClick(); }
        });
        im.put(KeyStroke.getKeyStroke("control S"), "guardar");
        am.put("guardar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (btnGuardar.isEnabled()) btnGuardar.doClick(); }
        });
        dialog.setVisible(true);
    }

    private void mostrarDialogoGrupo2() {
        JDialog dialog = new JDialog(this, "Exposición Grupo 2", true);
        dialog.setSize(1280, 780);
        dialog.setMinimumSize(new Dimension(960, 620));
        dialog.setUndecorated(true);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(C_FONDO);
        dialog.setLayout(new BorderLayout());

        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(C_PANEL);
        barra.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(C_ACENTO.darker().darker());
        filaTitulo.setBorder(new EmptyBorder(6, 12, 6, 8));

        JLabel lblTitulo = new JLabel("ImaGen Studio — Grupo 2");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setBackground(C_ACENTO.darker().darker());
        controles.setOpaque(true);

        JButton btnClose = crearBotonVentanaIcono("close");
        btnClose.addActionListener(e -> dialog.dispose());
        controles.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controles, BorderLayout.EAST);

        final Point[] dragPoint = new Point[1];
        filaTitulo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = dialog.getLocation();
                dialog.setLocation(loc.x + e.getX() - dragPoint[0].x, loc.y + e.getY() - dragPoint[0].y);
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(10, 18, 10, 18)));

        JLabel lblH = new JLabel("● ImaGen Studio");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblH.setForeground(C_ACENTO2);

        JLabel lblSub = new JLabel("  Cartel 3D (Textura, Color, W-Bufer) — Grupo 2");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_TEXTO_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(lblH);
        izq.add(lblSub);
        
        header.add(izq, BorderLayout.WEST);

        barra.add(filaTitulo, BorderLayout.NORTH);
        barra.add(header, BorderLayout.CENTER);
        
        dialog.add(barra, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setBackground(C_FONDO);
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        ImagePanel panelImg1 = new ImagePanel("Textura (Patrón)");
        ImagePanel panelResult = new ImagePanel("Resultado 3D");

        JPanel cardImg1 = new JPanel(new BorderLayout(0, 6));
        cardImg1.setBackground(C_CARD);
        cardImg1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblImg1 = new JLabel("Textura Original");
        lblImg1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblImg1.setForeground(C_TEXTO);
        cardImg1.add(lblImg1, BorderLayout.NORTH);
        cardImg1.add(panelImg1, BorderLayout.CENTER);
        
        JPanel cardResult = new JPanel(new BorderLayout(0, 6));
        cardResult.setBackground(C_CARD);
        cardResult.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblResult = new JLabel("Lienzo de Rasterizado 3D");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResult.setForeground(C_ACENTO2);
        cardResult.add(lblResult, BorderLayout.NORTH);
        cardResult.add(panelResult, BorderLayout.CENTER);
        
        centerPanel.add(cardImg1);
        centerPanel.add(cardResult);
        dialog.add(centerPanel, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setBackground(C_PANEL);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDE),
                new EmptyBorder(15, 12, 15, 12)));
        leftPanel.setPreferredSize(new Dimension(280, 0));

        String[] filtros = {
                "Textura",
                "Color Interpolado",
                "Interpolación Profundidad (1/W)",
                "W-Buffering",
                "Completo"
        };
        
        JList<String> listaModos = new JList<>(filtros);
        listaModos.setBackground(C_CARD);
        listaModos.setForeground(C_TEXTO);
        listaModos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listaModos.setSelectionBackground(C_SELECCION);
        listaModos.setSelectionForeground(Color.WHITE);
        listaModos.setFixedCellHeight(30);
        listaModos.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        listaModos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(2, 10, 2, 0));
                return lbl;
            }
        });
        
        JScrollPane scrollFiltros = new JScrollPane(listaModos);
        scrollFiltros.setBorder(BorderFactory.createLineBorder(C_BORDE));
        scrollFiltros.getViewport().setBackground(C_CARD);
        scrollFiltros.setPreferredSize(new Dimension(260, 160));

        JButton btnImgTex = crearBoton("Cargar Textura", C_ACENTO);
        JButton btnGuardar = crearBoton("Guardar Resultado", C_EXITO);
        btnGuardar.setEnabled(false);
        
        final BufferedImage[] texturaLocal = new BufferedImage[1];
        final BufferedImage[] resultadoLocal = new BufferedImage[1];

        // Draw default checkerboard so the left panel isn't empty initially
        BufferedImage defaultTex = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        int[] defaultTexPix = ((java.awt.image.DataBufferInt) defaultTex.getRaster().getDataBuffer()).getData();
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                boolean cuadro = ((x / 32) + (y / 32)) % 2 == 0;
                defaultTexPix[x + y * 256] = cuadro ? 0xFFFFFFFF : 0xFF000000;
            }
        }
        panelImg1.setImagen(defaultTex);

        Runnable ejecutarRasterizado = () -> {
            int modo = listaModos.getSelectedIndex() + 1; // 1 to 5
            listaModos.setEnabled(false); // disable while rendering
            
            SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    return ProcesadorImagenes.generarRasterizadoGrupo2(modo, 800, 600, texturaLocal[0]);
                }
                
                @Override
                protected void done() {
                    try {
                        BufferedImage res = get();
                        resultadoLocal[0] = res;
                        panelResult.setImagen(res);
                        btnGuardar.setEnabled(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error al generar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        listaModos.setEnabled(true);
                    }
                }
            };
            worker.execute();
        };
        
        listaModos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ejecutarRasterizado.run();
            }
        });

        btnImgTex.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(ultimoDirectorioCarga);
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage cargada = ImageIO.read(fc.getSelectedFile());
                    // Resize to 256x256 for the 3D engine texture
                    BufferedImage escalada = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = escalada.createGraphics();
                    g.drawImage(cargada, 0, 0, 256, 256, null);
                    g.dispose();

                    texturaLocal[0] = escalada;
                    panelImg1.setImagen(escalada);
                    ultimoDirectorioCarga = fc.getSelectedFile().getParentFile();
                    ejecutarRasterizado.run();
                } catch (Exception ex) {}
            }
        });

        listaModos.setSelectedIndex(4); // Completo por defecto
        
        btnGuardar.addActionListener(e -> {
            if (resultadoLocal[0] == null) return;
            JFileChooser chooser = new JFileChooser(ultimoDirectorioCarga);
            chooser.setDialogTitle("Guardar imagen");
            chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".png")) {
                    archivo = new File(archivo.getParentFile(), archivo.getName() + ".png");
                }
                try {
                    ImageIO.write(resultadoLocal[0], "png", archivo);
                    JOptionPane.showMessageDialog(dialog, "Guardado exitoso");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar");
                }
            }
        });

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBackground(C_PANEL);
        panelBotones.add(filaCompleta(btnImgTex));
        panelBotones.add(Box.createVerticalStrut(10));

        JPanel panelAcciones = new JPanel();
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setOpaque(false);
        panelAcciones.add(panelBotones);
        panelAcciones.add(Box.createVerticalGlue());
        panelAcciones.add(filaCompleta(btnGuardar));

        leftPanel.add(scrollFiltros, BorderLayout.NORTH);
        leftPanel.add(panelAcciones, BorderLayout.CENTER);

        dialog.add(leftPanel, BorderLayout.WEST);
        
        JRootPane root = dialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
        am.put("cerrar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });
        im.put(KeyStroke.getKeyStroke("control O"), "cargar");
        am.put("cargar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { btnImgTex.doClick(); }
        });
        im.put(KeyStroke.getKeyStroke("control S"), "guardar");
        am.put("guardar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (btnGuardar.isEnabled()) btnGuardar.doClick(); }
        });
        dialog.setVisible(true);
    }

    private void mostrarDialogoGrupo3() {
        JDialog dialog = new JDialog(this, "Exposición Grupo 3", true);
        dialog.setSize(600, 480);
        dialog.setUndecorated(true);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(C_FONDO);
        dialog.setLayout(new BorderLayout());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(C_PANEL);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDE));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel lblTitulo = new JLabel("Grupo 3: Fundamentos de Renderizado OpenGL");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        
        JButton btnCerrar = new JButton("×");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnCerrar.setForeground(C_TEXTO_DIM);
        btnCerrar.setBackground(C_PANEL);
        btnCerrar.setBorder(null);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dialog.dispose());
        btnCerrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnCerrar.setForeground(new Color(255, 85, 85)); }
            public void mouseExited(java.awt.event.MouseEvent evt)  { btnCerrar.setForeground(C_TEXTO_DIM); }
        });
        header.add(btnCerrar, BorderLayout.EAST);
        
        barra.add(header, BorderLayout.CENTER);
        dialog.add(barra, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(C_FONDO);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JEditorPane infoPane = new JEditorPane();
        infoPane.setContentType("text/html");
        infoPane.setEditable(false);
        infoPane.setOpaque(false);
        infoPane.setBackground(new Color(0,0,0,0));
        infoPane.setFocusable(false);
        
        String htmlContent = "<html><body style='font-family: \"Segoe UI\", sans-serif; color: #E0E6ED; margin: 0; padding: 10px;'>" +
            "<h2 style='color: #63B3ED; margin-top: 0;'>Aceleración por Hardware (OpenGL)</h2>" +
            "<p style='font-size: 14px; line-height: 1.5;'>El Grupo 3 procesa gráficos directamente en la tarjeta de video (GPU) utilizando <b>LWJGL 3</b> y GLFW.</p>" +
            "<div style='background-color: #1A1C29; padding: 15px; border-radius: 8px; border: 1px solid #3C4164; margin-bottom: 15px;'>" +
            "  <h3 style='color: #A0AEC0; margin-top: 0; font-size: 13px; letter-spacing: 1px;'>CARACTERÍSTICAS TÉCNICAS</h3>" +
            "  <ul style='font-size: 14px; color: #E0E6ED; margin-bottom: 0; padding-left: 25px;'>" +
            "    <li style='margin-bottom: 6px;'>Renderizado de primitivas 3D (Triángulos)</li>" +
            "    <li style='margin-bottom: 6px;'>Interpolación baricéntrica vía <b style='color:#63B3ED;'>Shaders (GLSL)</b></li>" +
            "    <li style='margin-bottom: 6px;'>Mapeo de Texturas (UV Mapping)</li>" +
            "    <li style='margin-bottom: 0;'><b>Z-Buffer (Depth Test)</b> acelerado por hardware</li>" +
            "  </ul>" +
            "</div>" +
            "<div style='background-color: #2D3748; padding: 15px; border-radius: 8px; border-left: 4px solid #F6E05E;'>" +
            "  <h3 style='color: #F6E05E; margin-top: 0; font-size: 13px; letter-spacing: 1px;'>CONTROLES NATIVOS (TECLADO)</h3>" +
            "  <table style='font-size: 14px; color: #E0E6ED; width: 100%; border-spacing: 0;'>" +
            "    <tr><td style='padding-bottom: 8px; width: 50px;'><b style='background-color: #1A202C; padding: 3px 8px; border-radius: 4px; border: 1px solid #4A5568;'> Z </b></td><td style='padding-bottom: 8px;'>Activa o desactiva el Z-Buffer interactivo</td></tr>" +
            "    <tr><td><b style='background-color: #1A202C; padding: 3px 8px; border-radius: 4px; border: 1px solid #4A5568;'>ESC</b></td><td>Cierra la demostración OpenGL</td></tr>" +
            "  </table>" +
            "</div>" +
            "</body></html>";
            
        infoPane.setText(htmlContent);

        JButton btnLanzar = crearBoton("Lanzar Motor OpenGL Nativo", C_EXITO);
        btnLanzar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLanzar.setPreferredSize(new Dimension(300, 45));
        btnLanzar.addActionListener(e -> {
            btnLanzar.setEnabled(false);
            btnLanzar.setText("Motor en ejecución...");
            new Thread(() -> {
                try {
                    efectos.exposiciones.grupo3.TriangulosDemo.main(new String[0]);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(dialog, "Error al ejecutar OpenGL: " + ex.getMessage(), "Error GLFW", JOptionPane.ERROR_MESSAGE);
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        btnLanzar.setEnabled(true);
                        btnLanzar.setText("Lanzar Motor OpenGL Nativo");
                    });
                }
            }).start();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        btnPanel.add(btnLanzar);

        centerPanel.add(infoPane, BorderLayout.CENTER);
        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(centerPanel, BorderLayout.CENTER);
        
        MouseAdapter ma = new MouseAdapter() {
            int pX, pY;
            public void mousePressed(MouseEvent e) { pX = e.getX(); pY = e.getY(); }
            public void mouseDragged(MouseEvent e) { dialog.setLocation(dialog.getLocation().x + e.getX() - pX, dialog.getLocation().y + e.getY() - pY); }
        };
        barra.addMouseListener(ma);
        barra.addMouseMotionListener(ma);

        JRootPane root = dialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
        am.put("cerrar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });
        dialog.setVisible(true);
    }

    private void mostrarDialogoGrupo8() {
        JDialog dialog = new JDialog(this, "Exposición Grupo 8", true);
        dialog.setSize(1280, 780);
        dialog.setMinimumSize(new Dimension(960, 620));
        dialog.setUndecorated(true);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(C_FONDO);
        dialog.setLayout(new BorderLayout());

        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(C_PANEL);
        barra.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(C_ACENTO.darker().darker());
        filaTitulo.setBorder(new EmptyBorder(6, 12, 6, 8));

        JLabel lblTitulo = new JLabel("ImaGen Studio — Grupo 8");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setBackground(C_ACENTO.darker().darker());
        controles.setOpaque(true);

        JButton btnClose = crearBotonVentanaIcono("close");
        btnClose.addActionListener(e -> dialog.dispose());
        controles.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controles, BorderLayout.EAST);

        final Point[] dragPoint = new Point[1];
        filaTitulo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = dialog.getLocation();
                dialog.setLocation(loc.x + e.getX() - dragPoint[0].x, loc.y + e.getY() - dragPoint[0].y);
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(10, 18, 10, 18)));

        JLabel lblH = new JLabel("● ImaGen Studio");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblH.setForeground(C_ACENTO2);

        JLabel lblSub = new JLabel("  Fragmentos (Stencil, Blending, XOR) — Grupo 8");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_TEXTO_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(lblH);
        izq.add(lblSub);
        
        header.add(izq, BorderLayout.WEST);
        barra.add(filaTitulo, BorderLayout.NORTH);
        barra.add(header, BorderLayout.CENTER);
        dialog.add(barra, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setBackground(C_FONDO);
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        ImagePanel panelImg1 = new ImagePanel("Carga Imagen 1 para comenzar");
        ImagePanel panelResult = new ImagePanel("Aquí verás el resultado");

        JPanel cardImg1 = new JPanel(new BorderLayout(0, 6));
        cardImg1.setBackground(C_CARD);
        cardImg1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblImg1 = new JLabel("Imagen 1 (Fondo/Principal)");
        lblImg1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblImg1.setForeground(C_TEXTO);
        cardImg1.add(lblImg1, BorderLayout.NORTH);
        cardImg1.add(panelImg1, BorderLayout.CENTER);
        
        JPanel cardResult = new JPanel(new BorderLayout(0, 6));
        cardResult.setBackground(C_CARD);
        cardResult.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblResult = new JLabel("Imagen Resultado");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResult.setForeground(C_ACENTO2);
        cardResult.add(lblResult, BorderLayout.NORTH);
        cardResult.add(panelResult, BorderLayout.CENTER);
        
        centerPanel.add(cardImg1);
        centerPanel.add(cardResult);
        dialog.add(centerPanel, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setBackground(C_PANEL);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDE),
                new EmptyBorder(15, 12, 15, 12)));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        String[] filtros = {
                "Stencil Circular",
                "Blending (Transparencia)",
                "XOR Lógico"
        };
        
        JList<String> listaModos = new JList<>(filtros);
        listaModos.setBackground(C_CARD);
        listaModos.setForeground(C_TEXTO);
        listaModos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listaModos.setSelectionBackground(C_SELECCION);
        listaModos.setSelectionForeground(Color.WHITE);
        listaModos.setFixedCellHeight(30);
        listaModos.setBorder(new EmptyBorder(0, 0, 0, 0));
        listaModos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(2, 10, 2, 0));
                return lbl;
            }
        });
        
        JScrollPane scrollFiltros = new JScrollPane(listaModos);
        scrollFiltros.setBorder(BorderFactory.createLineBorder(C_BORDE));
        scrollFiltros.getViewport().setBackground(C_CARD);
        scrollFiltros.setPreferredSize(new Dimension(280, 110));

        JButton btnImg1 = crearBoton("Cargar Imagen 1", C_ACENTO);
        JButton btnImg2 = crearBoton("Cargar Imagen 2", C_ACENTO2);
        JButton btnGuardar = crearBoton("Guardar Resultado", C_EXITO);
        btnGuardar.setEnabled(false);

        JSlider sliderRadio = new JSlider(50, 1000, 300);
        sliderRadio.setPaintTicks(true);
        sliderRadio.setPaintLabels(false);
        sliderRadio.setBackground(C_PANEL);
        sliderRadio.setForeground(C_TEXTO_DIM);
        JLabel lblRadio = new JLabel("Diámetro: 300px");
        lblRadio.setForeground(C_TEXTO_DIM);
        lblRadio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sliderRadio.addChangeListener(e -> lblRadio.setText("Diámetro: " + sliderRadio.getValue() + "px"));

        JSlider sliderAlpha = new JSlider(0, 100, 60);
        sliderAlpha.setPaintTicks(true);
        sliderAlpha.setPaintLabels(true);
        sliderAlpha.setMajorTickSpacing(50);
        sliderAlpha.setMinorTickSpacing(10);
        sliderAlpha.setBackground(C_PANEL);
        sliderAlpha.setForeground(C_TEXTO_DIM);
        JLabel lblAlpha = new JLabel("Nivel de Blending: 60%");
        lblAlpha.setForeground(C_TEXTO_DIM);
        lblAlpha.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sliderAlpha.addChangeListener(e -> lblAlpha.setText("Nivel de Blending: " + sliderAlpha.getValue() + "%"));

        JPanel panelParamsAlpha = new JPanel();
        panelParamsAlpha.setLayout(new BoxLayout(panelParamsAlpha, BoxLayout.Y_AXIS));
        panelParamsAlpha.setBackground(C_PANEL);
        panelParamsAlpha.add(lblAlpha);
        panelParamsAlpha.add(Box.createVerticalStrut(4));
        panelParamsAlpha.add(filaCompleta(sliderAlpha));
        panelParamsAlpha.setVisible(false);

        JPanel panelParamsRadio = new JPanel();
        panelParamsRadio.setLayout(new BoxLayout(panelParamsRadio, BoxLayout.Y_AXIS));
        panelParamsRadio.setBackground(C_PANEL);
        panelParamsRadio.add(lblRadio);
        panelParamsRadio.add(Box.createVerticalStrut(4));
        panelParamsRadio.add(filaCompleta(sliderRadio));
        panelParamsRadio.setVisible(false);

        JPanel panelParams = new JPanel();
        panelParams.setLayout(new BoxLayout(panelParams, BoxLayout.Y_AXIS));
        panelParams.setBackground(C_PANEL);
        panelParams.add(panelParamsAlpha);
        panelParams.add(panelParamsRadio);

        final BufferedImage[] imagenes = new BufferedImage[2];
        final BufferedImage[] resultadoLocal = new BufferedImage[1];
        
        Runnable ejecutarFiltro = () -> {
            int idx = listaModos.getSelectedIndex();
            if (idx == -1 || imagenes[0] == null) return;
            
            if ((idx == 1 || idx == 2) && imagenes[1] == null) {
                panelResult.setImagen(null);
                panelResult.setMensajePlaceholder("Este efecto requiere que cargues la Imagen 2.");
                return;
            }

            listaModos.setEnabled(false);
            sliderAlpha.setEnabled(false);
            sliderRadio.setEnabled(false);
            
            SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    if (idx == 0) return ProcesadorImagenes.stencilGrupo8(imagenes[0], sliderRadio.getValue());
                    if (idx == 1) return ProcesadorImagenes.blendingGrupo8(imagenes[0], imagenes[1], sliderAlpha.getValue() / 100f);
                    if (idx == 2) return ProcesadorImagenes.xorGrupo8(imagenes[0], imagenes[1]);
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        BufferedImage res = get();
                        if (res != null) {
                            resultadoLocal[0] = res;
                            panelResult.setImagen(res);
                            btnGuardar.setEnabled(true);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        listaModos.setEnabled(true);
                        sliderAlpha.setEnabled(true);
                        sliderRadio.setEnabled(true);
                    }
                }
            };
            worker.execute();
        };

        listaModos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = listaModos.getSelectedIndex();
                panelParamsAlpha.setVisible(idx == 1);
                panelParamsRadio.setVisible(idx == 0);
                ejecutarFiltro.run();
            }
        });

        sliderAlpha.addChangeListener(e -> {
            if (!sliderAlpha.getValueIsAdjusting()) ejecutarFiltro.run();
        });
        sliderRadio.addChangeListener(e -> {
            if (!sliderRadio.getValueIsAdjusting()) ejecutarFiltro.run();
        });

        btnImg1.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(ultimoDirectorioCarga);
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    imagenes[0] = ImageIO.read(fc.getSelectedFile());
                    panelImg1.setImagen(imagenes[0]);
                    ultimoDirectorioCarga = fc.getSelectedFile().getParentFile();
                    btnImg1.setText("Imagen 1: " + fc.getSelectedFile().getName());
                    ejecutarFiltro.run();
                } catch (Exception ex) {}
            }
        });

        btnImg2.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(ultimoDirectorioCarga);
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    imagenes[1] = ImageIO.read(fc.getSelectedFile());
                    ultimoDirectorioCarga = fc.getSelectedFile().getParentFile();
                    btnImg2.setText("Imagen 2: " + fc.getSelectedFile().getName());
                    ejecutarFiltro.run();
                } catch (Exception ex) {}
            }
        });

        btnGuardar.addActionListener(e -> {
            if (resultadoLocal[0] == null) return;
            JFileChooser chooser = new JFileChooser(ultimoDirectorioCarga);
            chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".png")) {
                    archivo = new File(archivo.getParentFile(), archivo.getName() + ".png");
                }
                try {
                    ImageIO.write(resultadoLocal[0], "png", archivo);
                    JOptionPane.showMessageDialog(dialog, "Guardado exitoso");
                } catch (Exception ex) {}
            }
        });

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBackground(C_PANEL);
        panelBotones.add(filaCompleta(btnImg1));
        panelBotones.add(Box.createVerticalStrut(6));
        panelBotones.add(filaCompleta(btnImg2));
        panelBotones.add(Box.createVerticalStrut(15));
        panelBotones.add(panelParams);

        JPanel panelAcciones = new JPanel();
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setOpaque(false);
        panelAcciones.add(Box.createVerticalGlue());
        panelAcciones.add(filaCompleta(btnGuardar));

        JPanel leftNorth = new JPanel(new BorderLayout());
        leftNorth.setOpaque(false);
        leftNorth.add(scrollFiltros, BorderLayout.NORTH);
        leftNorth.add(panelBotones, BorderLayout.CENTER);

        leftPanel.add(leftNorth, BorderLayout.NORTH);
        leftPanel.add(panelAcciones, BorderLayout.CENTER);

        dialog.add(leftPanel, BorderLayout.WEST);
        
        JRootPane root = dialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
        am.put("cerrar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });
        im.put(KeyStroke.getKeyStroke("control O"), "cargar");
        am.put("cargar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { btnImg1.doClick(); }
        });
        im.put(KeyStroke.getKeyStroke("control S"), "guardar");
        am.put("guardar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (btnGuardar.isEnabled()) btnGuardar.doClick(); }
        });
        dialog.setVisible(true);
    }
    
    private void mostrarDialogoGrupo7() {
        JDialog dialog = new JDialog(this, "Exposición Grupo 7", true);
        dialog.setSize(1280, 780);
        dialog.setMinimumSize(new Dimension(960, 620));
        dialog.setUndecorated(true);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(C_FONDO);
        dialog.setLayout(new BorderLayout());

        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(C_PANEL);
        barra.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(C_ACENTO.darker().darker());
        filaTitulo.setBorder(new EmptyBorder(6, 12, 6, 8));

        JLabel lblTitulo = new JLabel("ImaGen Studio — Grupo 7");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setBackground(C_ACENTO.darker().darker());
        controles.setOpaque(true);

        JButton btnClose = crearBotonVentanaIcono("close");
        btnClose.addActionListener(e -> dialog.dispose());
        controles.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controles, BorderLayout.EAST);

        final Point[] dragPoint = new Point[1];
        filaTitulo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = dialog.getLocation();
                dialog.setLocation(loc.x + e.getX() - dragPoint[0].x, loc.y + e.getY() - dragPoint[0].y);
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(10, 18, 10, 18)));

        JLabel lblH = new JLabel("● ImaGen Studio");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblH.setForeground(C_ACENTO2);

        JLabel lblSub = new JLabel("  Buffer de Acumulación (Efectos Multiplicativos) — Grupo 7");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_TEXTO_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(lblH);
        izq.add(lblSub);
        
        header.add(izq, BorderLayout.WEST);

        barra.add(filaTitulo, BorderLayout.NORTH);
        barra.add(header, BorderLayout.CENTER);
        
        dialog.add(barra, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setBackground(C_FONDO);
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        ImagePanel panelImg1 = new ImagePanel("Carga Imagen Original para comenzar");
        ImagePanel panelResult = new ImagePanel("Aquí verás el resultado del Buffer");

        JPanel cardImg1 = new JPanel(new BorderLayout(0, 6));
        cardImg1.setBackground(C_CARD);
        cardImg1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblImg1 = new JLabel("Imagen Original (GL_LOAD)");
        lblImg1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblImg1.setForeground(C_TEXTO);
        cardImg1.add(lblImg1, BorderLayout.NORTH);
        cardImg1.add(panelImg1, BorderLayout.CENTER);
        
        JPanel cardResult = new JPanel(new BorderLayout(0, 6));
        cardResult.setBackground(C_CARD);
        cardResult.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblResult = new JLabel("Buffer de Acumulación (GL_RETURN)");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResult.setForeground(C_ACENTO2);
        cardResult.add(lblResult, BorderLayout.NORTH);
        cardResult.add(panelResult, BorderLayout.CENTER);
        
        centerPanel.add(cardImg1);
        centerPanel.add(cardResult);
        dialog.add(centerPanel, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setBackground(C_PANEL);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDE),
                new EmptyBorder(15, 12, 15, 12)));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        String[] filtros = {
                "Factor: 1.0x",
                "Factor: 0.75x",
                "Factor: 0.50x",
                "Factor: 0.25x"
        };
        
        JList<String> listaModos = new JList<>(filtros);
        listaModos.setBackground(C_CARD);
        listaModos.setForeground(C_TEXTO);
        listaModos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listaModos.setSelectionBackground(C_SELECCION);
        listaModos.setSelectionForeground(Color.WHITE);
        listaModos.setFixedCellHeight(30);
        listaModos.setBorder(new EmptyBorder(0, 0, 0, 0));
        listaModos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(2, 10, 2, 0));
                return lbl;
            }
        });
        
        JScrollPane scrollFiltros = new JScrollPane(listaModos);
        scrollFiltros.setBorder(BorderFactory.createLineBorder(C_BORDE));
        scrollFiltros.getViewport().setBackground(C_CARD);
        scrollFiltros.setPreferredSize(new Dimension(280, 160));

        JButton btnImg = crearBoton("Cargar Imagen", C_ACENTO);
        JButton btnGuardar = crearBoton("Guardar Resultado", C_EXITO);
        btnGuardar.setEnabled(false);
        
        final BufferedImage[] imgLocal = new BufferedImage[1];
        final BufferedImage[] resultadoLocal = new BufferedImage[1];

        Runnable ejecutarFiltro = () -> {
            if (imgLocal[0] == null) return;
            int sel = listaModos.getSelectedIndex();
            float factor = 1.0f;
            if (sel == 1) factor = 0.75f;
            else if (sel == 2) factor = 0.50f;
            else if (sel == 3) factor = 0.25f;

            final float f = factor;
            listaModos.setEnabled(false);
            
            SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    return efectos.ProcesadorImagenes.bufferAcumulacion(imgLocal[0], f);
                }
                
                @Override
                protected void done() {
                    try {
                        BufferedImage res = get();
                        resultadoLocal[0] = res;
                        panelResult.setImagen(res);
                        btnGuardar.setEnabled(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error al procesar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        listaModos.setEnabled(true);
                    }
                }
            };
            worker.execute();
        };
        
        listaModos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ejecutarFiltro.run();
            }
        });

        btnImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(ultimoDirectorioCarga);
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage cargada = ImageIO.read(fc.getSelectedFile());
                    imgLocal[0] = cargada;
                    panelImg1.setImagen(cargada);
                    ultimoDirectorioCarga = fc.getSelectedFile().getParentFile();
                    if (listaModos.getSelectedIndex() == -1) listaModos.setSelectedIndex(0);
                    else ejecutarFiltro.run();
                } catch (Exception ex) {}
            }
        });
        
        btnGuardar.addActionListener(e -> {
            if (resultadoLocal[0] == null) return;
            JFileChooser chooser = new JFileChooser(ultimoDirectorioCarga);
            chooser.setDialogTitle("Guardar imagen");
            chooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".png")) {
                    archivo = new File(archivo.getParentFile(), archivo.getName() + ".png");
                }
                try {
                    ImageIO.write(resultadoLocal[0], "png", archivo);
                    JOptionPane.showMessageDialog(dialog, "Guardado exitoso");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar");
                }
            }
        });

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBackground(C_PANEL);
        panelBotones.add(filaCompleta(btnImg));
        panelBotones.add(Box.createVerticalStrut(10));

        JPanel panelAcciones = new JPanel();
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setOpaque(false);
        panelAcciones.add(panelBotones);
        panelAcciones.add(Box.createVerticalGlue());
        panelAcciones.add(filaCompleta(btnGuardar));

        leftPanel.add(scrollFiltros, BorderLayout.NORTH);
        leftPanel.add(panelAcciones, BorderLayout.CENTER);

        dialog.add(leftPanel, BorderLayout.WEST);
        
        JRootPane root = dialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
        am.put("cerrar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        im.put(KeyStroke.getKeyStroke("control O"), "cargar");
        am.put("cargar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                btnImg.doClick();
            }
        });

        im.put(KeyStroke.getKeyStroke("control S"), "guardar");
        am.put("guardar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (btnGuardar.isEnabled()) btnGuardar.doClick();
            }
        });

        dialog.setVisible(true);
    }

    private void mostrarDialogoEcualizador() {
        JDialog dialog = new JDialog(this, "Ecualizador e Histograma", true);
        dialog.setSize(1280, 780);
        dialog.setMinimumSize(new Dimension(960, 620));
        dialog.setUndecorated(true);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(C_FONDO);
        dialog.setLayout(new BorderLayout());

        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(C_PANEL);
        barra.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(C_ACENTO.darker().darker());
        filaTitulo.setBorder(new EmptyBorder(6, 12, 6, 8));

        JLabel lblTitulo = new JLabel("ImaGen Studio - Ecualizador e Histograma");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        JPanel controlesTitulo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controlesTitulo.setBackground(C_ACENTO.darker().darker());
        controlesTitulo.setOpaque(true);

        JButton btnClose = crearBotonVentanaIcono("close");
        btnClose.addActionListener(e -> dialog.dispose());
        controlesTitulo.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controlesTitulo, BorderLayout.EAST);

        final Point[] dragPoint = new Point[1];
        filaTitulo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = dialog.getLocation();
                dialog.setLocation(loc.x + e.getX() - dragPoint[0].x, loc.y + e.getY() - dragPoint[0].y);
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(10, 18, 10, 18)));

        JLabel lblH = new JLabel("🎨 ImaGen Studio");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblH.setForeground(C_ACENTO2);

        JLabel lblSub = new JLabel("  Ajuste de Brillo y Ecualización de Histograma");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_TEXTO_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(lblH);
        izq.add(lblSub);
        
        header.add(izq, BorderLayout.WEST);

        barra.add(filaTitulo, BorderLayout.NORTH);
        barra.add(header, BorderLayout.CENTER);
        dialog.add(barra, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        centerPanel.setBackground(C_FONDO);
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        ImagePanel panelImgOrig = new ImagePanel("Imagen Original");
        ImagePanel panelImgProc = new ImagePanel("Imagen Procesada");
        ImagePanel panelHist = new ImagePanel("Histograma");

        centerPanel.add(panelImgOrig);
        centerPanel.add(panelImgProc);
        centerPanel.add(panelHist);
        
        dialog.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(C_PANEL);
        bottomPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        controles.setOpaque(false);

        JButton btnImg = crearBoton("Cargar Imagen", C_ACENTO);
        JButton btnGuardar = crearBoton("Guardar Resultado", C_EXITO);
        btnGuardar.setEnabled(false);

        JSlider sliderBrillo = new JSlider(-255, 255, 0);
        sliderBrillo.setMajorTickSpacing(50);
        sliderBrillo.setPaintTicks(true);
        sliderBrillo.setPaintLabels(true);
        sliderBrillo.setEnabled(false);
        sliderBrillo.setPreferredSize(new Dimension(300, 45));
        sliderBrillo.setOpaque(false);
        sliderBrillo.setForeground(C_TEXTO);

        sliderBrillo.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && sliderBrillo.isEnabled()) {
                    sliderBrillo.setValue(0);
                }
            }
        });

        JCheckBox chkGrayscale = new JCheckBox("Escala de Grises");
        chkGrayscale.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkGrayscale.setForeground(C_TEXTO);
        chkGrayscale.setOpaque(false);
        chkGrayscale.setEnabled(false);

        JCheckBox chkCDF = new JCheckBox("Ecualizar CDF");
        chkCDF.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCDF.setForeground(C_TEXTO);
        chkCDF.setOpaque(false);
        chkCDF.setEnabled(false);
        
        controles.add(btnImg);
        controles.add(new JLabel("Brillo:"));
        controles.add(sliderBrillo);
        controles.add(chkGrayscale);
        controles.add(chkCDF);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightControls.setOpaque(false);
        rightControls.add(btnGuardar);

        bottomPanel.add(controles, BorderLayout.CENTER);
        bottomPanel.add(rightControls, BorderLayout.EAST);
        
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        final BufferedImage[] imgBase = new BufferedImage[1];
        final BufferedImage[] imgResult = new BufferedImage[1];

        Runnable actualizarImagen = () -> {
            if (imgBase[0] == null) return;
            boolean cdf = chkCDF.isSelected();
            boolean gray = chkGrayscale.isSelected();
            sliderBrillo.setEnabled(!cdf);

            if (cdf) {
                imgResult[0] = efectos.ProcesadorImagenes.ecualizarCDF(imgBase[0], gray);
            } else {
                int brillo = sliderBrillo.getValue();
                imgResult[0] = efectos.ProcesadorImagenes.ajustarBrillo(imgBase[0], brillo, gray);
            }
            panelImgProc.setImagen(imgResult[0]);
            
            BufferedImage histImg = efectos.ProcesadorImagenes.generarGraficoHistograma(imgResult[0], gray);
            panelHist.setImagen(histImg);
            
            btnGuardar.setEnabled(true);
        };

        btnImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(ultimoDirectorioCarga != null && ultimoDirectorioCarga.exists() ? ultimoDirectorioCarga : new File(System.getProperty("user.home")));
            fc.setDialogTitle("Seleccionar imagen");
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                ultimoDirectorioCarga = f.getParentFile();
                try {
                    imgBase[0] = javax.imageio.ImageIO.read(f);
                    panelImgOrig.setImagen(imgBase[0]);
                    chkGrayscale.setEnabled(true);
                    chkCDF.setEnabled(true);
                    sliderBrillo.setEnabled(!chkCDF.isSelected());
                    actualizarImagen.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        sliderBrillo.addChangeListener(e -> {
            if (!sliderBrillo.getValueIsAdjusting()) {
                actualizarImagen.run();
            }
        });
        chkGrayscale.addActionListener(e -> actualizarImagen.run());
        chkCDF.addActionListener(e -> actualizarImagen.run());

        btnGuardar.addActionListener(e -> {
            if (imgResult[0] == null) return;
            JFileChooser chooser = new JFileChooser(ultimoDirectorioCarga != null && ultimoDirectorioCarga.exists() ? ultimoDirectorioCarga : new File(System.getProperty("user.home")));
            chooser.setDialogTitle("Guardar imagen");
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".png") && !archivo.getName().toLowerCase().endsWith(".jpg")) {
                    archivo = new File(archivo.getParentFile(), archivo.getName() + ".png");
                }
                try {
                    javax.imageio.ImageIO.write(imgResult[0], "png", archivo);
                    JOptionPane.showMessageDialog(dialog, "Imagen guardada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JRootPane root = dialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar");
        am.put("cerrar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });
        im.put(KeyStroke.getKeyStroke("control O"), "cargar");
        am.put("cargar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { btnImg.doClick(); }
        });
        im.put(KeyStroke.getKeyStroke("control S"), "guardar");
        am.put("guardar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (btnGuardar.isEnabled()) btnGuardar.doClick(); }
        });

        dialog.setVisible(true);
    }

    class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(70, 80, 115);
            this.trackColor = new Color(22, 25, 38);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        private JButton createZeroButton() {
            JButton button = new JButton();
            Dimension zeroDim = new Dimension(0, 0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? thumbColor.brighter() : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    }
