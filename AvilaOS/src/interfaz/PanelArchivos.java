/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package interfaz;

//--- bloque a: imports ---
import avilaos.InfoNodo;
import avilaos.TipoOperacionES;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
//--- fin del bloque a: imports ---

/**
 *
 * @author vyckh
 */
//el arbol de directorios y archivos con la informacion del elemento seleccionado
public class PanelArchivos extends javax.swing.JPanel {

    //--- bloque a: campos ---
    //los botones no hacen la operacion: le piden a la ventana que arme el proceso de E/S
    public interface AccionListener {
        void pedirOperacion(TipoOperacionES operacion, InfoNodo sobre, boolean directorio);
    }

    private DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("/");
    private DefaultTreeModel modelo = new DefaultTreeModel(raiz);
    private InfoNodo[] nodos = new InfoNodo[0];
    //la version del sistema de archivos que esta dibujada, para no rearmar el arbol si no cambio
    private int versionMostrada = -1;
    private AccionListener listener;
    //--- fin del bloque a: campos ---

    /**
     * Creates new form PanelArchivos
     */
    public PanelArchivos() {
        initComponents();
        //--- bloque b ---
        arbol.setModel(modelo);
        arbol.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        arbol.setCellRenderer(new Dibujo());
        arbol.addTreeSelectionListener(e -> mostrarInfo());
        mostrarInfo();
        //--- fin del bloque b ---
    }

    //--- bloque c ---
    public PanelArchivos(AccionListener l) {
        this();
        listener = l;
    }

    public void setAccionListener(AccionListener l) {
        listener = l;
    }

    private void pedir(TipoOperacionES op, boolean directorio) {
        if (listener != null) {
            listener.pedirOperacion(op, getSeleccionado(), directorio);
        }
    }

    private void btnCrearArchivoActionPerformed(java.awt.event.ActionEvent evt) {
        pedir(TipoOperacionES.CREAR, false);
    }

    private void btnCrearDirectorioActionPerformed(java.awt.event.ActionEvent evt) {
        pedir(TipoOperacionES.CREAR, true);
    }

    private void btnLeerActionPerformed(java.awt.event.ActionEvent evt) {
        pedir(TipoOperacionES.LEER, false);
    }

    private void btnRenombrarActionPerformed(java.awt.event.ActionEvent evt) {
        pedir(TipoOperacionES.ACTUALIZAR, false);
    }

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        pedir(TipoOperacionES.ELIMINAR, false);
    }

    public InfoNodo getSeleccionado() {
        TreePath camino = arbol.getSelectionPath();
        if (camino == null) {
            return null;
        }
        Object dato = ((DefaultMutableTreeNode) camino.getLastPathComponent()).getUserObject();
        return dato instanceof InfoNodo ? (InfoNodo) dato : null;
    }

    //recibe la copia del arbol en el orden en que se dibuja y lo rearma solo si la version cambio
    public void mostrar(InfoNodo[] copia, int version) {
        if (version == versionMostrada) {
            return;
        }
        versionMostrada = version;
        nodos = copia;
        InfoNodo antes = getSeleccionado();
        String rutaAntes = antes == null ? null : antes.ruta;

        //ultimo[n] es el ultimo directorio visto en el nivel n, ahi se cuelgan los del nivel n+1
        DefaultMutableTreeNode[] ultimo = new DefaultMutableTreeNode[copia.length + 1];
        DefaultMutableTreeNode elegido = null;
        for (int i = 0; i < copia.length; i++) {
            InfoNodo n = copia[i];
            DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(n);
            if (n.nivel == 0) {
                raiz = nodo;
            } else {
                ultimo[n.nivel - 1].add(nodo);
            }
            ultimo[n.nivel] = nodo;
            if (n.ruta.equals(rutaAntes)) {
                elegido = nodo;
            }
        }
        modelo.setRoot(raiz);
        for (int i = 0; i < arbol.getRowCount(); i++) {
            arbol.expandRow(i);
        }
        if (elegido != null) {
            arbol.setSelectionPath(new TreePath(elegido.getPath()));
        }
        mostrarInfo();
    }

    private void mostrarInfo() {
        InfoNodo n = getSeleccionado();
        if (n == null) {
            lblNombre.setText("Selecciona un elemento del arbol");
            lblRuta.setText(" ");
            lblTamano.setText(" ");
            lblDueno.setText(" ");
            lblPermisos.setText(" ");
            lblPrimerBloque.setText(" ");
            return;
        }
        lblNombre.setText("Nombre: " + n.nombre + (n.esDirectorio() ? "  (directorio)" : ""));
        lblRuta.setText("Ruta: " + n.ruta);
        lblTamano.setText("Tamano: " + (n.esDirectorio() ? "-" : n.bloques + " bloques"));
        lblDueno.setText("Dueno: " + n.dueno);
        lblPermisos.setText("Permisos: " + n.permiso);
        lblPrimerBloque.setText("Primer bloque: " + (n.esDirectorio() ? "-" : String.valueOf(n.primerBloque)));
    }

    //un directorio vacio se dibuja como carpeta y no como archivo
    private static class Dibujo extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object dato = ((DefaultMutableTreeNode) value).getUserObject();
            if (dato instanceof InfoNodo) {
                InfoNodo n = (InfoNodo) dato;
                if (n.esDirectorio()) {
                    setIcon(expanded ? getOpenIcon() : getClosedIcon());
                } else {
                    setIcon(getLeafIcon());
                }
                setText(n.nivel == 0 ? "/" : n.nombre);
            }
            return this;
        }
    }
    //--- fin del bloque c ---

    //--- diseno: esto lo genera el disenador de NetBeans ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        scrArbol = new javax.swing.JScrollPane();
        arbol = new javax.swing.JTree();
        pnlAbajo = new javax.swing.JPanel();
        pnlInfo = new javax.swing.JPanel();
        lblNombre = new javax.swing.JLabel();
        lblRuta = new javax.swing.JLabel();
        lblTamano = new javax.swing.JLabel();
        lblDueno = new javax.swing.JLabel();
        lblPermisos = new javax.swing.JLabel();
        lblPrimerBloque = new javax.swing.JLabel();
        pnlBotones = new javax.swing.JPanel();
        btnCrearArchivo = new javax.swing.JButton();
        btnCrearDirectorio = new javax.swing.JButton();
        btnLeer = new javax.swing.JButton();
        btnRenombrar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Sistema de archivos"));
        setPreferredSize(new java.awt.Dimension(290, 300));
        setLayout(new java.awt.BorderLayout(0, 4));

        scrArbol.setViewportView(arbol);

        add(scrArbol, java.awt.BorderLayout.CENTER);

        pnlAbajo.setLayout(new java.awt.BorderLayout(0, 4));

        pnlInfo.setBorder(javax.swing.BorderFactory.createTitledBorder("Seleccionado"));
        pnlInfo.setLayout(new java.awt.GridLayout(6, 1));

        lblNombre.setText(" ");
        pnlInfo.add(lblNombre);

        lblRuta.setText(" ");
        pnlInfo.add(lblRuta);

        lblTamano.setText(" ");
        pnlInfo.add(lblTamano);

        lblDueno.setText(" ");
        pnlInfo.add(lblDueno);

        lblPermisos.setText(" ");
        pnlInfo.add(lblPermisos);

        lblPrimerBloque.setText(" ");
        pnlInfo.add(lblPrimerBloque);

        pnlAbajo.add(pnlInfo, java.awt.BorderLayout.CENTER);

        pnlBotones.setLayout(new java.awt.GridLayout(3, 2, 4, 4));

        btnCrearArchivo.setText("Crear archivo");
        btnCrearArchivo.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnCrearArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearArchivoActionPerformed(evt);
            }
        });
        pnlBotones.add(btnCrearArchivo);

        btnCrearDirectorio.setText("Crear directorio");
        btnCrearDirectorio.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnCrearDirectorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearDirectorioActionPerformed(evt);
            }
        });
        pnlBotones.add(btnCrearDirectorio);

        btnLeer.setText("Leer");
        btnLeer.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLeer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLeerActionPerformed(evt);
            }
        });
        pnlBotones.add(btnLeer);

        btnRenombrar.setText("Renombrar");
        btnRenombrar.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnRenombrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenombrarActionPerformed(evt);
            }
        });
        pnlBotones.add(btnRenombrar);

        btnEliminar.setText("Eliminar");
        btnEliminar.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        pnlBotones.add(btnEliminar);

        pnlAbajo.add(pnlBotones, java.awt.BorderLayout.PAGE_END);

        add(pnlAbajo, java.awt.BorderLayout.PAGE_END);
    }

    // Variables declaration - do not modify
    private javax.swing.JTree arbol;
    private javax.swing.JButton btnCrearArchivo;
    private javax.swing.JButton btnCrearDirectorio;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLeer;
    private javax.swing.JButton btnRenombrar;
    private javax.swing.JLabel lblDueno;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPermisos;
    private javax.swing.JLabel lblPrimerBloque;
    private javax.swing.JLabel lblRuta;
    private javax.swing.JLabel lblTamano;
    private javax.swing.JPanel pnlAbajo;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JScrollPane scrArbol;
    // End of variables declaration
    //--- fin del diseno ---
}
