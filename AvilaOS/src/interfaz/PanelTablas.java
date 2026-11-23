/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package interfaz;

//--- bloque a: imports ---
import avilaos.Disco;
import avilaos.InfoNodo;
import avilaos.SolicitudES;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
//--- fin del bloque a: imports ---

/**
 *
 * @author andrea
 */
//las dos tablas: la cola de E/S del disco y la tabla de asignacion de archivos
public class PanelTablas extends javax.swing.JPanel {

    //--- bloque a: campos ---
    //los modelos no son componentes, por eso se crean aqui y no en el disenador
    private ModeloCola modeloCola = new ModeloCola();
    private ModeloAsignacion modeloAsignacion = new ModeloAsignacion();
    //lo ultimo que se mostro, para no refrescar las tablas si no cambio nada
    private String firmaCola = "";
    private String firmaAsignacion = "";
    //--- fin del bloque a: campos ---

    /**
     * Creates new form PanelTablas
     */
    public PanelTablas() {
        initComponents();
        //--- bloque b ---
        tablaCola.setModel(modeloCola);
        tablaAsignacion.setModel(modeloAsignacion);
        tablaCola.setFillsViewportHeight(true);
        tablaAsignacion.setFillsViewportHeight(true);
        //la primera columna se pinta con el color del archivo
        tablaAsignacion.getColumnModel().getColumn(0).setCellRenderer(new CeldaColor());
        tablaAsignacion.getColumnModel().getColumn(0).setMaxWidth(44);
        tablaAsignacion.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaAsignacion.getColumnModel().getColumn(2).setPreferredWidth(50);
        tablaAsignacion.getColumnModel().getColumn(3).setPreferredWidth(80);
        tablaCola.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaCola.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaCola.getColumnModel().getColumn(4).setPreferredWidth(50);
        tablaCola.getColumnModel().getColumn(5).setPreferredWidth(50);
        //--- fin del bloque b ---
    }

    //--- bloque c ---
    public void mostrarCola(Disco disco, long ciclo) {
        SolicitudES actual = disco.getActual();
        SolicitudES[] cola = disco.copiarCola();
        int extra = actual == null ? 0 : 1;
        Object[][] filas = new Object[cola.length + extra][];
        if (actual != null) {
            filas[0] = fila("atendiendo", actual, ciclo);
        }
        for (int i = 0; i < cola.length; i++) {
            filas[i + extra] = fila(String.valueOf(i + 1), cola[i], ciclo);
        }
        String firma = firmar(filas);
        if (!firma.equals(firmaCola)) {
            firmaCola = firma;
            modeloCola.filas = filas;
            modeloCola.fireTableDataChanged();
        }
    }

    private Object[] fila(String posicion, SolicitudES s, long ciclo) {
        return new Object[]{posicion, s.getProceso().getname(), s.getOperacion(), s.getRuta(), s.getBloque(),
            Math.max(0, ciclo - s.getCicloLlegada())};
    }

    //solo los archivos tienen fila, los directorios no ocupan bloques
    public void mostrarAsignacion(InfoNodo[] nodos) {
        int cuantos = 0;
        for (int i = 0; i < nodos.length; i++) {
            if (!nodos[i].esDirectorio()) {
                cuantos++;
            }
        }
        Object[][] filas = new Object[cuantos][];
        int f = 0;
        for (int i = 0; i < nodos.length; i++) {
            InfoNodo n = nodos[i];
            if (!n.esDirectorio()) {
                filas[f++] = new Object[]{n.color, n.ruta, n.bloques, n.primerBloque, n.dueno, n.permiso, n.creador};
            }
        }
        String firma = firmar(filas);
        if (!firma.equals(firmaAsignacion)) {
            firmaAsignacion = firma;
            modeloAsignacion.filas = filas;
            modeloAsignacion.fireTableDataChanged();
        }
    }

    private String firmar(Object[][] filas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filas.length; i++) {
            for (int j = 0; j < filas[i].length; j++) {
                sb.append(filas[i][j]).append('|');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static class ModeloCola extends AbstractTableModel {

        private final String[] columnas = {"Turno", "Proceso", "Operacion", "Ruta", "Bloque", "Espera"};
        Object[][] filas = new Object[0][];

        @Override
        public int getRowCount() {
            return filas.length;
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int c) {
            return columnas[c];
        }

        @Override
        public Object getValueAt(int f, int c) {
            return filas[f][c];
        }
    }

    private static class ModeloAsignacion extends AbstractTableModel {

        private final String[] columnas = {"Color", "Archivo", "Bloques", "1er bloque", "Dueno", "Permiso", "Creado por"};
        Object[][] filas = new Object[0][];

        @Override
        public int getRowCount() {
            return filas.length;
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int c) {
            return columnas[c];
        }

        @Override
        public Object getValueAt(int f, int c) {
            return filas[f][c];
        }
    }

    //la primera columna se pinta con el mismo color que tiene el archivo en el disco
    private static class CeldaColor extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean sel, boolean foco, int f, int c) {
            super.getTableCellRendererComponent(tabla, "", sel, foco, f, c);
            setBackground(Paleta.de((Integer) valor));
            return this;
        }
    }
    //--- fin del bloque c ---

    //--- diseno: esto lo genera el disenador de NetBeans ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        scrCola = new javax.swing.JScrollPane();
        tablaCola = new javax.swing.JTable();
        scrAsignacion = new javax.swing.JScrollPane();
        tablaAsignacion = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(440, 300));
        setLayout(new java.awt.GridLayout(2, 1, 0, 4));

        scrCola.setBorder(javax.swing.BorderFactory.createTitledBorder("Cola de E/S del disco"));

        tablaCola.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        scrCola.setViewportView(tablaCola);

        add(scrCola);

        scrAsignacion.setBorder(javax.swing.BorderFactory.createTitledBorder("Tabla de asignacion de archivos"));

        tablaAsignacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        scrAsignacion.setViewportView(tablaAsignacion);

        add(scrAsignacion);
    }

    // Variables declaration - do not modify
    private javax.swing.JScrollPane scrAsignacion;
    private javax.swing.JScrollPane scrCola;
    private javax.swing.JTable tablaAsignacion;
    private javax.swing.JTable tablaCola;
    // End of variables declaration
    //--- fin del diseno ---
}
