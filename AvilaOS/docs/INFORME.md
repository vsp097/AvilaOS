# AvilaOS - Proyecto 2: sistema de archivos, planificacion de disco y suspension de procesos

Sistemas Operativos, trimestre 2627-1. Integrantes: Vyckhy, Andrea y Diego.

> Quien escribe cada parte: **Diego** redacta el informe completo (secciones 1 a 5 y 7). Los diagramas UML de la seccion 6 los hacen **Vyckhy** (clases) y **Andrea** (secuencia). Cada uno revisa la descripcion de las clases que programo.

---

## 1. Como esta organizado el sistema (Diego)

AvilaOS separa el **hardware simulado**, el **sistema operativo** y la **interfaz**:

- **Hardware que obedece al reloj.** `GlobalClock` es un hilo que en cada ciclo llama a `onTick` de cada componente registrado, en este orden: `Computador` y despues `Disco`. Los dos implementan la interfaz `ClockListener`, y cada uno avanza exactamente un ciclo de trabajo por tick.
- **Sistema operativo.** `SistemaOperativo` arma y conecta todo: el computador, el disco, el sistema de archivos (`SistemaArchivos`), el log y las metricas. Es quien valida las operaciones, arma las solicitudes de E/S y aplica los cambios cuando el disco termina.
- **Interfaz.** Las ventanas (`main` y los paneles) piden copias del estado (`copiarColas`, `copiarBloques`, `copiarArbol`) con un `Timer` y las dibujan. Lo unico que le piden al sistema es crear procesos, cambiar politicas y el modo de usuario.

**Semaforos (`java.util.concurrent.Semaphore`).** Hay uno en `Computador` (las colas), uno en `Disco` (la cola de E/S, el mapa de bloques y el cabezal) y uno en `SistemaArchivos` (el arbol). Cuando hay que tomar mas de uno se toman siempre en el orden *computador, archivos, disco*, y el disco avisa que termino una solicitud ya con su semaforo suelto. Asi no puede haber interbloqueo.

**Estructuras propias.** No se usa ninguna coleccion de Java: las colas de procesos son `Cola<T>`, la cola de E/S y los hijos de un directorio son `Listas<T>` (lista enlazada) y el mapa de bloques es un arreglo de `Bloque`.

## 2. Clases y metodos mas importantes (Diego)

### Hardware y reloj
| Clase | Que hace | Metodos clave |
|---|---|---|
| `GlobalClock` | Hilo del reloj global; se puede pausar, reanudar y cambiar la duracion del ciclo. | `run`, `agregar`, `pausar`, `reanudar` |
| `Computador` | CPU y RAM, las siete colas de procesos, el planificador de CPU y el planificador a mediano plazo. | `onTick`, `admitirInterno`, `ejecutar`, `despertar`, `hacerEspacio`, `revisarReactivacion`, `suspensionLista`, `reactivacionLista` |
| `Disco` | Disco simulado: mapa de bloques (archivos y area de intercambio), cabezal con su direccion, cola unica de E/S. Atiende una solicitud a la vez. | `onTick`, `trabajar`, `mover`, `encolar`, `retirar`, `asignar`, `asignarSwap`, `liberar`, `cadena` |
| `Bloque` | Un bloque: si esta ocupado, quien lo ocupa, color y el numero del **siguiente** bloque (asignacion encadenada; el ultimo guarda -1). | `ocupar`, `liberar`, `getSiguiente` |
| `SolicitudES` | Una solicitud en la cola del disco: proceso, operacion, ruta, recorrido de bloques y ciclo de llegada. | `getBloque`, `sumarHecho`, `termino` |

### Sistema operativo
| Clase | Que hace | Metodos clave |
|---|---|---|
| `SistemaOperativo` | Conecta todo. Implementa `ServicioES` (el computador le pide la E/S), `OyenteDisco` (el disco le avisa que termino) y `ServicioIntercambio` (el mediano plazo le pide suspender y reactivar). | `crearProceso`, `revisarOperacion`, `solicitar`, `solicitudAtendida`, `aplicar`, `suspender`, `reactivar`, `guardarArchivos` |
| `SistemaArchivos` | Arbol de directorios y archivos con CRUD y permisos. | `buscar`, `crear`, `quitar`, `renombrar`, `revisarPermiso` |
| `NodoArchivo` | Un directorio o archivo: nombre, dueño, permiso, primer bloque, cantidad de bloques, hijos. | `buscarHijo`, `agregarHijo`, `getRuta` |
| `DiscoFIFO`, `DiscoSSTF`, `DiscoSCAN`, `DiscoCSCAN` | Las cuatro politicas de disco. Reciben la cola, la posicion y direccion del cabezal y devuelven una `DecisionDisco` (que solicitud atender, o ir a un extremo). | `decidir` |
| `MedianoPlazoAntiguedad` | Criterio del planificador a mediano plazo (ver seccion 4). | `elegirParaSuspender`, `elegirParaReactivar` |
| `ArchivosJSON`, `ConfigArchivos`, `ConfigProcesos` | Carga y validacion de los dos JSON y guardado del sistema de archivos. | `cargarArchivos`, `cargarProcesos`, `guardarArchivos`, `validar` |
| `Metricas` | Metricas del Proyecto 1 para un solo computador, mas suspensiones, reactivaciones y el historial del desplazamiento del cabezal para el grafico. | `registrarCiclo`, `registrarSuspension`, `registrarDisco` |
| `Validaciones` | Validacion de tipo y rango de todos los campos de entrada. | `leerTexto`, `leerEntero`, `leerDecimal` |

### Interfaz
`main` (ventana principal), `inicio` (exige cargar los dos JSON antes de arrancar), `PanelColas` (las 7 colas), `panelPCB`, `PanelArchivos` (JTree e informacion del nodo), `PanelDisco` (mapa de bloques con el cabezal), `PanelTablas` (cola de E/S y tabla de asignacion con colores), `FormularioES` (crear procesos de E/S), `PanelMetricas` (metricas y graficos con JFreeChart), `Panellog`.

### Flujo de una operacion de E/S
1. El usuario llena `FormularioES`. `Validaciones` revisa tipos y rangos; `SistemaOperativo.crearProceso` revisa que la ruta exista y los permisos. Si algo falla se explica en la ventana y la simulacion sigue.
2. El proceso entra a **Nuevo**; cuando hay RAM pasa a **Listo**.
3. Cuando obtiene la CPU, su primera instruccion se ejecuta en modo SO: `Computador.ejecutar` llama a `ServicioES.solicitar`, que arma la `SolicitudES` con el recorrido de bloques y la encola en el disco. El proceso pasa a **Bloqueado** (motivo: E/S de disco).
4. En cada ciclo el disco le pregunta a la politica activa que sigue, mueve el cabezal (N posiciones por ciclo) o lee/escribe un bloque (1 ciclo por bloque).
5. Al terminar, `OyenteDisco.solicitudAtendida` aplica el cambio en el sistema de archivos y despierta al proceso, que pasa a **Listo**.

## 3. Enums e interfaces (Diego)

**Enums**

| Enum | Valores | Para que |
|---|---|---|
| `EstadoProceso` | Nuevo, Listo, Ejecucion, Bloqueado, ListoSuspendido, BloqueadoSuspendido, Terminado | Modelo de siete estados. |
| `TipoOperacionES` | CREAR, LEER, ACTUALIZAR, ELIMINAR, SUSPENDER, REACTIVAR | Operacion de cada solicitud de E/S. |
| `PoliticaDisco` | FIFO, SSTF, SCAN, C_SCAN | Politica de planificacion de disco. |
| `ModoUsuario` | ADMINISTRADOR, USUARIO | Modo de uso. |
| `MotivoBloqueo` | NINGUNO, ES_DISCO, SUSPENSION | Por que esta bloqueado un proceso (se muestra en las colas y el PCB). |
| `DireccionCabezal` | ARRIBA, ABAJO | Direccion del cabezal. |
| `TipoNodo` | ARCHIVO, DIRECTORIO | Tipo de nodo del arbol. |
| `Permiso` | PUBLICO, PRIVADO | Permiso de un archivo. |
| `TipoProceso` | CPU_BOUND, ES | Tipo de proceso. |
| `PoliticaPlanificacion`, `ModoCPU` | (del Proyecto 1) | Politica de CPU y modo de la CPU. |

**Interfaces**

| Interfaz | La implementan | Para que |
|---|---|---|
| `ClockListener` | `Computador`, `Disco` | Todo lo que avanza con el reloj. |
| `PlanificadorDisco` | `DiscoFIFO`, `DiscoSSTF`, `DiscoSCAN`, `DiscoCSCAN` | Agregar una politica nueva (LOOK, por ejemplo) es crear una clase y agregarla al enum y a `SelectorPoliticaDisco`; `Disco` no cambia. |
| `PlanificadorMedianoPlazo` | `MedianoPlazoAntiguedad` | Criterio para escoger a quien suspender y a quien reactivar. |
| `ServicioES` | `SistemaOperativo` | El computador le pide la E/S al sistema operativo sin conocerlo. |
| `OyenteDisco` | `SistemaOperativo` | El disco avisa que termino sin conocer al sistema operativo. |
| `ServicioIntercambio` | `SistemaOperativo` | El mediano plazo pide suspender y reactivar. |
| `Planificador` | `FCFS`, `Roundrobin`, `EDF`, `Prioridades` | Politicas de CPU (Proyecto 1). |

