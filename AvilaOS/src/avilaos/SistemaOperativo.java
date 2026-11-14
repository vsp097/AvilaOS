/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Computador;
import Data_Structures.Listas;
import Data_Structures.PCB;
import Data_Structures.TipoProceso;
import java.io.IOException;

/**
 *
 * @author vyckh
 */
//aqui vive la logica de AvilaOS: arma el computador, el disco y el sistema de archivos y los conecta
//la ventana solo llama a esta clase y mira, no decide nada
public class SistemaOperativo implements ServicioES, OyenteDisco {

    private Computador computador;
    private Disco disco;
    private SistemaArchivos archivos;
    private LogEventos log;
    private Metricas metricas;
    private GlobalClock reloj;

    private ModoUsuario modo = ModoUsuario.ADMINISTRADOR;
    private String usuario = "admin";
    private int siguienteId = 1;
    //rutas que algun proceso ya pidio crear y todavia estan en la cola del disco
    private Listas<String> reservadas = new Listas<>();
    private boolean iniciado = false;

    //se arma con los dos archivos ya cargados; si algo no cuadra lanza el error con el motivo
    public SistemaOperativo(ConfigArchivos ca, ConfigProcesos cp) {
        ca.validar();
        cp.validar();
        log = new LogEventos();
        metricas = new Metricas();
        reloj = new GlobalClock();
        reloj.setcduration(cp.globalConfig.cycleDurationMs);

        disco = new Disco(ca.diskConfig.totalBlocks, cp.globalConfig.swapSizeBlocks, ca.diskConfig.initialHead);
        if (ca.diskConfig.headSpeed != null) {
            disco.setVelocidad(ca.diskConfig.headSpeed);
        }
        disco.setLog(log);
        disco.setOyente(this);
        disco.setPlanificador(SelectorPoliticaDisco.crear(PoliticaDisco.desdeTexto(cp.globalConfig.initialDiskPolicy)));

        archivos = new SistemaArchivos();
        cargarArchivos(ca);

        computador = new Computador();
        computador.setCPUt("CPU");
        computador.setRAMt(cp.computer.ramCapacity);
        computador.settype("MB");
        computador.setNombre("Computador");
        computador.setLog(log);
        computador.setMetricas(metricas, 0);
        computador.setServicioES(this);
        computador.setPlanificador(SelectorPolitica.crear(ConfigProcesos.leerPolitica(cp.computer.policy),
                Math.max(1, cp.computer.quantum)));

        //primero el computador y despues el disco: lo que un proceso pide en un ciclo el disco ya lo ve en ese ciclo
        reloj.agregar(computador);
        reloj.agregar(disco);

        cargarProcesos(cp);
    }

    //----- carga inicial -----

    private void cargarArchivos(ConfigArchivos ca) {
        ConfigArchivos.Entrada[] entradas = ca.fileSystem;
        boolean[] hecho = new boolean[entradas.length];
        //los directorios pueden venir en cualquier orden, asi que se dan vueltas hasta que no entre ninguno mas
        boolean avanzo = true;
        while (avanzo) {
            avanzo = false;
            for (int i = 0; i < entradas.length; i++) {
                ConfigArchivos.Entrada e = entradas[i];
                if (hecho[i] || TipoNodo.desdeTexto(e.type) != TipoNodo.DIRECTORIO) {
                    continue;
                }
                NodoArchivo padre = archivos.buscar(SistemaArchivos.rutaPadre(e.path));
                if (padre != null && padre.esDirectorio()) {
                    archivos.crear(e.path, TipoNodo.DIRECTORIO, e.owner, Permiso.desdeTexto(e.permissions), "sistema");
                    hecho[i] = true;
                    avanzo = true;
                }
            }
        }
        for (int i = 0; i < entradas.length; i++) {
            ConfigArchivos.Entrada e = entradas[i];
            if (hecho[i]) {
                continue;
            }
            if (TipoNodo.desdeTexto(e.type) == TipoNodo.DIRECTORIO) {
                throw new IllegalArgumentException("No existe el directorio donde va " + e.path);
            }
            String creador = e.createdBy == null || e.createdBy.trim().isEmpty() ? "sistema" : e.createdBy.trim();
            NodoArchivo nodo = archivos.crear(e.path, TipoNodo.ARCHIVO, e.owner, Permiso.desdeTexto(e.permissions), creador);
            int color = archivos.nuevoColor();
            int[] cadena;
            if (e.chain != null && e.chain.length > 0) {
                cadena = e.chain;
                disco.asignarCadena(cadena, e.path, color);
            } else {
                cadena = disco.asignarDesde(e.startBlock, e.blocks, e.path, color);
            }
            nodo.setBloques(cadena[0], cadena.length);
            nodo.setColor(color);
        }
    }

    private void cargarProcesos(ConfigProcesos cp) {
        int deES = 0;
        for (int i = 0; i < cp.processes.length; i++) {
            ConfigProcesos.Proceso c = cp.processes[i];
            TipoOperacionES op = TipoOperacionES.desdeTexto(c.operation);
            PCB p = new PCB(siguienteId++, c.name, 0, op == null ? TipoProceso.CPU_BOUND : TipoProceso.ES, c.memory);
            p.settime(c.instructions);
            p.setdeadline(c.deadline);
            p.setPrioridad(c.priority == null ? 1 : c.priority);
            if (op != null) {
                p.setOperacion(op);
                p.setRuta(c.targetPath);
                p.setTamanoArchivo(c.fileSizeBlocks);
                p.setDuenoArchivo("admin");
                p.setDeCargaInicial(true);
                deES++;
            }
            computador.agregarProceso(p);
        }
    }

    //----- el reloj -----

    public void iniciar() {
        if (!iniciado) {
            iniciado = true;
            reloj.start();
        }
    }

    public void pausar() {
        reloj.pausar();
    }

    public void reanudar() {
        reloj.reanudar();
    }

    public boolean estaPausado() {
        return reloj.isPaused();
    }

    public void detener() {
        reloj.detener();
    }

    public long getCiclo() {
        return reloj.getCiclo();
    }

    public long getDuracionCiclo() {
        return reloj.getcduration();
    }

    public void setDuracionCiclo(long ms) {
        reloj.setcduration(ms);
    }

    //----- modo de usuario -----

    public void setModoUsuario(ModoUsuario m, String nombre) {
        modo = m;
        usuario = m == ModoUsuario.ADMINISTRADOR ? "admin" : nombre;
        log.registrar((int) reloj.getCiclo(), "Modo " + modo + " (" + usuario + ")");
    }

    public ModoUsuario getModoUsuario() {
        return modo;
    }

    public String getUsuario() {
        return usuario;
    }

    //----- crear procesos (lo unico que la ventana puede pedir sobre los archivos) -----

    //crea el proceso y lo mete a la cola de nuevos
    //si la operacion no esta permitida lanza el error con la explicacion y la simulacion sigue igual
    public PCB crearProceso(DatosProceso d) {
        if (d.memoria > computador.getRamt()) {
            throw new IllegalArgumentException("El proceso pide " + d.memoria + " MB y la RAM total es de "
                    + computador.getRamt() + " MB");
        }
        if (d.operacion != null) {
            d.ruta = SistemaArchivos.limpiarRuta(d.ruta);
            String motivo;
            archivos.tomar();
            try {
                motivo = revisarOperacion(d.operacion, d.ruta, d.esDirectorio, d.tamano, d.nuevoNombre, modo, usuario);
            } finally {
                archivos.soltar();
            }
            if (motivo != null) {
                log.registrar((int) reloj.getCiclo(), "Se rechaza " + d.operacion + " " + d.ruta + " pedido por "
                        + usuario + ": " + motivo);
                throw new IllegalArgumentException("Operacion rechazada: " + motivo);
            }
        }
        PCB p = new PCB(siguienteId++, d.nombre, 0, d.operacion == null ? TipoProceso.CPU_BOUND : TipoProceso.ES, d.memoria);
        p.settime(d.instrucciones);
        p.setdeadline(d.deadline);
        p.setPrioridad(d.prioridad);
        p.setUsuario(usuario);
        p.setModoUsuario(modo);
        if (d.operacion != null) {
            p.setOperacion(d.operacion);
            p.setRuta(d.ruta);
            p.setEsDirectorio(d.esDirectorio);
            p.setTamanoArchivo(d.tamano);
            p.setNuevoNombre(d.nuevoNombre == null ? "" : d.nuevoNombre.trim());
            p.setPermisoArchivo(d.permiso);
            //solo el administrador puede crear cosas a nombre de otro
            boolean eligeDueno = modo == ModoUsuario.ADMINISTRADOR && d.dueno != null && !d.dueno.trim().isEmpty();
            p.setDuenoArchivo(eligeDueno ? d.dueno.trim() : usuario);
        }
        computador.agregarProceso(p);
        return p;
    }

    public int getSiguienteId() {
        return siguienteId;
    }

    //revisa que la operacion tenga sentido y que el usuario tenga permiso
    //devuelve null si se puede hacer, o el motivo si no. Hay que tener tomado el sistema de archivos
    private String revisarOperacion(TipoOperacionES op, String ruta, boolean esDirectorio, int tamano,
            String nuevoNombre, ModoUsuario m, String u) {
        NodoArchivo nodo = archivos.buscar(ruta);
        switch (op) {
            case CREAR:
                if (nodo != null || reservadas.contiene(ruta)) {
                    return "ya existe " + ruta;
                }
                NodoArchivo padre = archivos.buscar(SistemaArchivos.rutaPadre(ruta));
                if (padre == null || !padre.esDirectorio()) {
                    return "no existe el directorio " + SistemaArchivos.rutaPadre(ruta);
                }
                String permiso = SistemaArchivos.revisarPermiso(op, padre, esDirectorio, m, u);
                if (permiso != null) {
                    return permiso;
                }
                if (!esDirectorio) {
                    if (tamano < 1) {
                        return "el archivo debe tener por lo menos 1 bloque";
                    }
                    int libres = disco.getLibres();
                    if (tamano > libres) {
                        return "no hay bloques libres suficientes: pide " + tamano + " y quedan " + libres;
                    }
                }
                return null;
            case LEER:
                if (nodo == null) {
                    return "no existe " + ruta;
                }
                return SistemaArchivos.revisarPermiso(op, nodo, false, m, u);
            case ACTUALIZAR:
            case ELIMINAR:
                if (nodo == null) {
                    return "no existe " + ruta;
                }
                if (nodo == archivos.getRaiz()) {
                    return "la raiz no se puede modificar";
                }
                String p = SistemaArchivos.revisarPermiso(op, nodo, false, m, u);
                if (p != null) {
                    return p;
                }
                if (op == TipoOperacionES.ACTUALIZAR && nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                    String nombre = nuevoNombre.trim();
                    try {
                        SistemaArchivos.revisarNombre(nombre);
                    } catch (IllegalArgumentException e) {
                        return e.getMessage();
                    }
                    String nueva = SistemaArchivos.unir(SistemaArchivos.rutaPadre(ruta), nombre);
                    if (!nombre.equals(nodo.getNombre()) && (nodo.getPadre().buscarHijo(nombre) != null
                            || reservadas.contiene(nueva))) {
                        return "ya existe " + nueva;
                    }
                }
                return null;
            default:
                return "esa operacion es solo del sistema operativo";
        }
    }

    //----- lo que pide el computador cuando un proceso ejecuta su E/S -----

    @Override
    public String solicitar(PCB p, long ciclo) {
        archivos.tomar();
        try {
            TipoOperacionES op = p.getOperacion();
            String ruta = p.getRuta();
            String motivo = revisarOperacion(op, ruta, p.getEsDirectorio(), p.getTamanoArchivo(), p.getNuevoNombre(),
                    p.getModoUsuario(), p.getUsuario());
            if (motivo != null) {
                return motivo;
            }
            NodoArchivo nodo = archivos.buscar(ruta);
            //las operaciones sobre directorios no tienen bloques propios, se atienden donde este el cabezal
            int[] aqui = new int[]{Math.min(disco.getCabezal(), disco.getBloquesArchivos() - 1)};
            int[] recorrido;
            int color = -1;
            switch (op) {
                case CREAR:
                    if (p.getEsDirectorio()) {
                        recorrido = aqui;
                    } else {
                        //los bloques se reservan de una vez, asi el planificador sabe a donde tiene que ir el cabezal
                        color = archivos.nuevoColor();
                        recorrido = disco.asignar(p.getTamanoArchivo(), ruta, color);
                        if (recorrido == null) {
                            return "no hay bloques libres suficientes";
                        }
                    }
                    reservadas.agregar(ruta);
                    break;
                case ELIMINAR:
                    //para borrar basta llegar al primer bloque, los demas se sueltan siguiendo la cadena
                    recorrido = nodo.esDirectorio() ? aqui : new int[]{nodo.getPrimerBloque()};
                    break;
                default:
                    //leer y actualizar recorren todos los bloques del archivo en el orden de la cadena
                    recorrido = nodo.esDirectorio() ? aqui : disco.cadena(nodo.getPrimerBloque());
                    break;
            }
            SolicitudES s = new SolicitudES(p, op, ruta, recorrido, ciclo);
            s.setColor(color);
            disco.encolar(s);
            return null;
        } finally {
            archivos.soltar();
        }
    }

    @Override
    public void cpuOciosa() {
    }

    //----- lo que avisa el disco cuando termina una solicitud -----

    @Override
    public void solicitudAtendida(SolicitudES s) {
        PCB p = s.getProceso();
        String error;
        archivos.tomar();
        try {
            error = aplicar(s);
        } finally {
            archivos.soltar();
        }
        int ciclo = (int) reloj.getCiclo();
        if (error == null) {
            log.registrar(ciclo, "El disco termina " + s.getOperacion() + " " + s.getRuta() + " de " + p.getname());
            computador.despertar(p);
        } else {
            log.registrar(ciclo, "El disco no pudo completar " + s.getOperacion() + " " + s.getRuta() + " de "
                    + p.getname() + ": " + error);
            computador.fallar(p, error);
        }
    }

    //el cambio en el sistema de archivos se hace cuando el disco ya termino, no antes
    private String aplicar(SolicitudES s) {
        PCB p = s.getProceso();
        String ruta = s.getRuta();
        NodoArchivo nodo = archivos.buscar(ruta);
        switch (s.getOperacion()) {
            case CREAR:
                reservadas.eliminar(ruta);
                try {
                    NodoArchivo nuevo = archivos.crear(ruta, p.getEsDirectorio() ? TipoNodo.DIRECTORIO : TipoNodo.ARCHIVO,
                            p.getDuenoArchivo(), p.getPermisoArchivo(), p.getname());
                    if (!p.getEsDirectorio()) {
                        nuevo.setBloques(s.getRecorrido()[0], s.getRecorrido().length);
                        nuevo.setColor(s.getColor());
                    }
                } catch (IllegalArgumentException e) {
                    //mientras esperaba en la cola le borraron el directorio: se devuelven los bloques
                    if (!p.getEsDirectorio()) {
                        disco.liberar(s.getRecorrido()[0]);
                    }
                    return e.getMessage();
                }
                return null;
            case LEER:
                return nodo == null ? ruta + " ya no existe" : null;
            case ACTUALIZAR:
                if (nodo == null) {
                    return ruta + " ya no existe";
                }
                String nombre = p.getNuevoNombre();
                if (nombre.isEmpty() || nombre.equals(nodo.getNombre())) {
                    return null;
                }
                if (nodo.getPadre().buscarHijo(nombre) != null) {
                    return "ya existe otro elemento llamado " + nombre;
                }
                archivos.renombrar(nodo, nombre);
                //los bloques guardan la ruta de su archivo, hay que ponerles la nueva
                Listas<NodoArchivo> movidos = archivos.archivosDe(nodo);
                for (int i = 0; i < movidos.len(); i++) {
                    NodoArchivo a = movidos.retornar(i);
                    disco.cambiarOcupante(a.getPrimerBloque(), a.getRuta());
                }
                return null;
            case ELIMINAR:
                if (nodo == null) {
                    return ruta + " ya no existe";
                }
                //si es un directorio se va con todo lo que tiene adentro
                Listas<NodoArchivo> borrados = archivos.archivosDe(nodo);
                int liberados = 0;
                for (int i = 0; i < borrados.len(); i++) {
                    NodoArchivo a = borrados.retornar(i);
                    disco.liberar(a.getPrimerBloque());
                    liberados += a.getBloques();
                }
                archivos.quitar(nodo);
                log.registrar((int) reloj.getCiclo(), "Se elimina " + ruta + " y se liberan " + liberados + " bloques");
                return null;
            default:
                return null;
        }
    }

    //----- el disco -----

    public void cambiarPoliticaDisco(PoliticaDisco politica) {
        disco.setPlanificador(SelectorPoliticaDisco.crear(politica));
    }

    public void setVelocidadCabezal(int v) {
        disco.setVelocidad(v);
    }

    //----- guardar el estado del sistema de archivos -----

    public ConfigArchivos exportarArchivos() {
        archivos.tomar();
        try {
            ConfigArchivos c = new ConfigArchivos();
            c.diskConfig = new ConfigArchivos.DiskConfig();
            c.diskConfig.totalBlocks = disco.getBloquesArchivos();
            c.diskConfig.initialHead = Math.min(disco.getCabezal(), disco.getBloquesArchivos() - 1);
            c.diskConfig.headSpeed = disco.getVelocidad();
            Listas<NodoArchivo> nodos = archivos.todos();
            c.fileSystem = new ConfigArchivos.Entrada[nodos.len()];
            for (int i = 0; i < nodos.len(); i++) {
                NodoArchivo n = nodos.retornar(i);
                ConfigArchivos.Entrada e = new ConfigArchivos.Entrada();
                e.path = n.getRuta();
                e.type = n.getTipo().enIngles();
                e.owner = n.getDueno();
                e.permissions = n.getPermiso().enIngles();
                if (!n.esDirectorio()) {
                    e.startBlock = n.getPrimerBloque();
                    e.blocks = n.getBloques();
                    e.chain = disco.cadena(n.getPrimerBloque());
                    e.createdBy = n.getCreador();
                }
                c.fileSystem[i] = e;
            }
            return c;
        } finally {
            archivos.soltar();
        }
    }

    public void guardarArchivos(String ruta) throws IOException {
        ArchivosJSON.guardarArchivos(exportarArchivos(), ruta);
        log.registrar((int) reloj.getCiclo(), "Se guarda el sistema de archivos en " + ruta);
    }

    //----- copias para la ventana -----

    //el arbol completo en el orden en que se dibuja: cada directorio y enseguida lo que tiene adentro
    public InfoNodo[] copiarArbol() {
        archivos.tomar();
        try {
            Listas<InfoNodo> lista = new Listas<>();
            copiarNodo(archivos.getRaiz(), 0, lista);
            InfoNodo[] copia = new InfoNodo[lista.len()];
            for (int i = 0; i < copia.length; i++) {
                copia[i] = lista.retornar(i);
            }
            return copia;
        } finally {
            archivos.soltar();
        }
    }

    private void copiarNodo(NodoArchivo nodo, int nivel, Listas<InfoNodo> lista) {
        lista.agregar(new InfoNodo(nodo, nivel));
        for (int i = 0; i < nodo.getHijos().len(); i++) {
            copiarNodo(nodo.getHijos().retornar(i), nivel + 1, lista);
        }
    }

    public int getVersionArchivos() {
        return archivos.getVersion();
    }

    //los duenos que aparecen en el sistema de archivos, para escoger con cual usuario entrar
    public String[] getUsuarios() {
        InfoNodo[] nodos = copiarArbol();
        Listas<String> nombres = new Listas<>();
        for (int i = 0; i < nodos.length; i++) {
            if (!nodos[i].dueno.equals("admin") && !nombres.contiene(nodos[i].dueno)) {
                nombres.agregar(nodos[i].dueno);
            }
        }
        String[] arreglo = new String[nombres.len()];
        for (int i = 0; i < arreglo.length; i++) {
            arreglo[i] = nombres.retornar(i);
        }
        return arreglo;
    }

    public Computador getComputador() {
        return computador;
    }

    public Disco getDisco() {
        return disco;
    }

    public LogEventos getLog() {
        return log;
    }

    public Metricas getMetricas() {
        return metricas;
    }
}
