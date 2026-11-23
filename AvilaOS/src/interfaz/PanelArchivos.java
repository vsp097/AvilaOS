/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import avilaos.InfoNodo;
import avilaos.TipoOperacionES;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author vyckh
 */
//el arbol de directorios y archivos con la informacion del elemento seleccionado
public class PanelArchivos extends JPanel {

    //los botones no hacen la operacion: le piden a la ventana que arme el proceso de E/S
    public interface AccionListener {
        void pedirOperacion(TipoOperacionES operacion, InfoNodo sobre, boolean directorio);
    }

    private DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("/");
    private DefaultTreeModel modelo = new DefaultTreeModel(raiz);
    private JTree arbol = new JTree(modelo);
    private JLabel[] datos = new JLabel[6];
    private InfoNodo[] nodos = new InfoNodo[0];
    //la version del sistema de archivos que esta dibujada, para no rearmar el arbol si no cambio
    private int versionMostrada = -1;
    private AccionListener listener;

    public PanelArchivos(AccionListener l) {
        listener = l;
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createTitledBorder("Sistema de archivos"));
        setPreferredSize(new Dimension(290, 300));

        arbol.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        arbol.setCellRenderer(new Dibujo());
        arbol.addTreeSelectionListener(e -> mostrarInfo());
        add(new JScrollPane(arbol), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 4));
        JPanel info = new JPanel(new GridLayout(datos.length, 1));
        info.setBorder(BorderFactory.createTitledBorder("Seleccionado"));
        for (int i = 0; i < datos.length; i++) {
            datos[i] = new JLabel(" ");
            info.add(datos[i]);
        }
        abajo.add(info, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(3, 2, 4, 4));
        botones.add(boton("Crear archivo", TipoOperacionES.CREAR, false));
        botones.add(boton("Crear directorio", TipoOperacionES.CREAR, true));
        botones.add(boton("Leer", TipoOperacionES.LEER, false));
        botones.add(boton("Renombrar", TipoOperacionES.ACTUALIZAR, false));
        botones.add(boton("Eliminar", TipoOperacionES.ELIMINAR, false));
        abajo.add(botones, BorderLayout.SOUTH);
        add(abajo, BorderLayout.SOUTH);
        mostrarInfo();
    }

    private JButton boton(String texto, final TipoOperacionES op, final boolean directorio) {
        JButton b = new JButton(texto);
        b.setMargin(new java.awt.Insets(2, 2, 2, 2));
        b.addActionListener(e -> {
            if (listener != null) {
                listener.pedirOperacion(op, getSeleccionado(), directorio);
            }
        });
        return b;
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
            datos[0].setText("Selecciona un elemento del arbol");
            for (int i = 1; i < datos.length; i++) {
                datos[i].setText(" ");
            }
            return;
        }
        datos[0].setText("Nombre: " + n.nombre + (n.esDirectorio() ? "  (directorio)" : ""));
        datos[1].setText("Ruta: " + n.ruta);
        datos[2].setText("Tamano: " + (n.esDirectorio() ? "-" : n.bloques + " bloques"));
        datos[3].setText("Dueno: " + n.dueno);
        datos[4].setText("Permisos: " + n.permiso);
        datos[5].setText("Primer bloque: " + (n.esDirectorio() ? "-" : String.valueOf(n.primerBloque)));
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
}
