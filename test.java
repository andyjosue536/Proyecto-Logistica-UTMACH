/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.logistica;

import java.util.Scanner;

/**
 * Algoritmo: Sistema Inteligente de Logistica y Distribucion UTMACH
 * Integrantes: Grupo 4 - Paralelo B (Montesdeoca, Feijoo, Andrade, Jimenez, Sari)
 * Version mejorada: incorpora Funciones, SubProcesos (metodos), plano cartesiano,
 * calculo de distancia/tiempo/combustible, peso y capacidad de carga,
 * prioridad de pedidos, validaciones adicionales y estadisticas.
 *
 * Traduccion directa del pseudocodigo original (.psc) a Java para NetBeans.
 */
public class test {

    // Clase auxiliar para simular el "Por Referencia" de dos salidas
    // (peso_asignado y vehiculos_usados) del subproceso AsignarCargaMultiple.
    static class ResultadoCarga {
        double pesoAsignado;
        String vehiculosUsados;
    }

    public static void main(String[] args) {
        // ===================================================================
        // 1. CONSTANTES Y DIMENSIONAMIENTO
        // ===================================================================
        try (Scanner sc = new Scanner(System.in)) {
            // ===================================================================
            // 1. CONSTANTES Y DIMENSIONAMIENTO
            // ===================================================================
            final int NUM_PROD = 5;
            final int NUM_VEH = 3;
            final int NUM_PED = 5;
            final int NUM_SECTORES = 5;
            
            // (arreglos dimensionados en n+1 para poder indexar desde 1, igual que el pseudocodigo)
            int[] prodCodigo = new int[NUM_PROD + 1];
            String[] prodNombre = new String[NUM_PROD + 1];
            int[] prodStock = new int[NUM_PROD + 1];
            int[] prodMinimo = new int[NUM_PROD + 1];
            double[] prodPeso = new double[NUM_PROD + 1];
            int[] prodEntregado = new int[NUM_PROD + 1];
            
            String[] vehCodigo = new String[NUM_VEH + 1];
            double[] vehCapacidad = new double[NUM_VEH + 1];
            double[] vehCargaActual = new double[NUM_VEH + 1];
            String[] vehEstado = new String[NUM_VEH + 1];
            int[] vehSector = new int[NUM_VEH + 1];
            int[] vehEntregas = new int[NUM_VEH + 1];
            
            int[] pedCodigo = new int[NUM_PED + 1];
            int[] pedSector = new int[NUM_PED + 1];
            int[] pedProdCod = new int[NUM_PED + 1];
            int[] pedCantidad = new int[NUM_PED + 1];
            String[] pedEstado = new String[NUM_PED + 1];
            int[] pedPrioridad = new int[NUM_PED + 1];
            double[] pedDistancia = new double[NUM_PED + 1];
            double[] pedTiempo = new double[NUM_PED + 1];
            
            String[] sectorNombre = new String[NUM_SECTORES + 1];
            double[] sectorX = new double[NUM_SECTORES + 1];
            double[] sectorY = new double[NUM_SECTORES + 1];
            double[][] matrizDistancias = new double[NUM_SECTORES + 1][NUM_SECTORES + 1];
            
            // ===================================================================
            // 2. INICIALIZACION DE DATOS BASE
            // ===================================================================
            prodCodigo[1] = 101; prodNombre[1] = "Caja de Banano";  prodStock[1] = 120; prodMinimo[1] = 20; prodPeso[1] = 18;
            prodCodigo[2] = 102; prodNombre[2] = "Saco de Cafe";    prodStock[2] = 80;  prodMinimo[2] = 15; prodPeso[2] = 60;
            prodCodigo[3] = 103; prodNombre[3] = "Caja de Camaron"; prodStock[3] = 200; prodMinimo[3] = 30; prodPeso[3] = 22;
            prodCodigo[4] = 104; prodNombre[4] = "Saco de Arroz";   prodStock[4] = 15;  prodMinimo[4] = 25; prodPeso[4] = 50;
            prodCodigo[5] = 105; prodNombre[5] = "Caja de Cacao";   prodStock[5] = 50;  prodMinimo[5] = 10; prodPeso[5] = 25;
            for (int i = 1; i <= NUM_PROD; i++) {
                prodEntregado[i] = 0; // acumulado historico entregado (para estadisticas)
            }
            
            vehCodigo[1] = "V-01"; vehCapacidad[1] = 500.0; vehEstado[1] = "Disponible";   vehSector[1] = 1;
            vehCodigo[2] = "V-02"; vehCapacidad[2] = 800.0; vehEstado[2] = "Disponible";   vehSector[2] = 1;
            vehCodigo[3] = "V-03"; vehCapacidad[3] = 350.0; vehEstado[3] = "Mantenimiento"; vehSector[3] = 1;
            for (int i = 1; i <= NUM_VEH; i++) {
                vehCargaActual[i] = 0;
                vehEntregas[i] = 0;
            }
            
            for (int i = 1; i <= NUM_PED; i++) {
                pedCodigo[i] = 0;
                pedSector[i] = 1;
                pedCantidad[i] = 0;
                pedProdCod[i] = 0;
                pedEstado[i] = "Vacio";
                pedPrioridad[i] = 0;
                pedDistancia[i] = 0;
                pedTiempo[i] = 0;
            }
            
            // 2.1 Plano cartesiano: coordenadas (km) del almacen (sector 1) y sectores
            sectorNombre[1] = "Centro (Almacen)"; sectorX[1] = 0; sectorY[1] = 0;
            sectorNombre[2] = "Norte";            sectorX[2] = 2; sectorY[2] = 5;
            sectorNombre[3] = "Sur";              sectorX[3] = 3; sectorY[3] = -4;
            sectorNombre[4] = "Este";             sectorX[4] = 5; sectorY[4] = 2;
            sectorNombre[5] = "Puerto Bolivar";   sectorX[5] = 7; sectorY[5] = -3;
            
            // 2.2 Matriz de distancias: se calcula con calcularDistancia()
            //     a partir de las coordenadas cartesianas (no se digita a mano).
            for (int i = 1; i <= NUM_SECTORES; i++) {
                for (int j = 1; j <= NUM_SECTORES; j++) {
                    matrizDistancias[i][j] = calcularDistancia(sectorX[i], sectorY[i], sectorX[j], sectorY[j]);
                }
            }
            
            // ===================================================================
            // 3. MENU PRINCIPAL ITERATIVO
            // ===================================================================
            boolean continuar = true;
            while (continuar) {
                borrarPantalla();
                System.out.println("=========================================================");
                System.out.println("    SISTEMA INTELIGENTE DE LOGISTICA Y DISTRIBUCION      ");
                System.out.println("          EMPRESAS DE MACHALA - UTMACH TI                ");
                System.out.println("=========================================================");
                System.out.println("1. Consultar Inventario y Alertas de Reposicion");
                System.out.println("2. Registrar Nuevo Pedido de Cliente");
                System.out.println("3. Procesar y Planificar Rutas Automaticas (Logica/Rutas)");
                System.out.println("4. Simular Despacho y Entrega de Pedidos");
                System.out.println("5. Generar Reporte de Gestion Logistica y Estadistica");
                System.out.println("6. Salir del Sistema");
                System.out.println("=========================================================");
                System.out.print("Seleccione una opcion (1-6): ");
                int opcion = leerEntero(sc);
                
                switch (opcion) {
                    case 1 -> {
                        mostrarInventario(prodCodigo, prodNombre, prodStock, prodMinimo, NUM_PROD);
                        esperarEnter(sc, "Presione Enter para continuar...");
                    }
                        
                    case 2 -> {
                        registrarPedido(pedCodigo, pedSector, pedProdCod, pedCantidad, pedEstado, pedPrioridad, NUM_PED, NUM_SECTORES,
                                prodCodigo, prodNombre, prodStock, prodMinimo, NUM_PROD, sc);
                        esperarEnter(sc, "Presione Enter para continuar...");
                    }
                        
                    case 3 -> {
                        planificarRutas(pedCodigo, pedSector, pedProdCod, pedCantidad, pedEstado, pedPrioridad,
                                pedDistancia, pedTiempo, prodCodigo, prodStock, prodPeso, vehCodigo, vehCapacidad,
                                vehCargaActual, vehEstado, vehSector, matrizDistancias, sectorNombre, NUM_PED, NUM_PROD, NUM_VEH);
                        esperarEnter(sc, "Presione Enter para continuar...");
                    }
                        
                    case 4 -> {
                        realizarEntregas(pedCodigo, pedProdCod, pedCantidad, pedEstado, prodCodigo, prodStock,
                                prodEntregado, vehCodigo, vehEstado, vehCargaActual, vehEntregas, NUM_PED, NUM_PROD, NUM_VEH);
                        esperarEnter(sc, "Presione Enter para continuar...");
                    }
                        
                    case 5 -> {
                        generarReporte(pedCodigo, pedEstado, pedDistancia, pedTiempo, prodCodigo, prodNombre,
                                prodEntregado, vehCodigo, vehEstado, vehEntregas, NUM_PED, NUM_PROD, NUM_VEH);
                        esperarEnter(sc, "Presione Enter para regresar al menu...");
                    }
                        
                    case 6 -> {
                        System.out.println("Finalizando el software logistico inteligente.");
                        continuar = false;
                    }
                        
                    default -> {
                        System.out.println("Opcion invalida.");
                        esperarEnter(sc, "Presione Enter para reintentar...");
                    }
                }
            }
        }
    }

    // ===========================================================================
    // FUNCIONES (devuelven un valor calculado, usadas dentro de expresiones)
    // ===========================================================================

    // Distancia entre dos puntos del plano cartesiano: d = raiz((x2-x1)^2 + (y2-y1)^2)
    static double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // Tiempo estimado de entrega en horas: Tiempo = Distancia / Velocidad
    static double calcularTiempoEntrega(double distancia, double velocidad) {
        return distancia / velocidad;
    }

    // Consumo estimado de combustible en galones: Combustible = Distancia / Rendimiento
    static double calcularCombustible(double distancia, double rendimiento) {
        return distancia / rendimiento;
    }

    // Selecciona el vehiculo mas adecuado: Disponible + capacidad suficiente
    // + menor distancia al sector del pedido. Devuelve 0 si ninguno puede tomar el pedido.
    static int asignarVehiculo(String[] vehEstado, double[] vehCapacidad, double[] vehCargaActual,
            int[] vehSector, double[][] matrizDistancias, double pesoPedido, int sectorPedido, int n) {
        int idx = 0;
        double mejorDistancia = -1;

        for (int k = 1; k <= n; k++) {
            if (vehEstado[k].equals("Disponible")) {
                double capacidadLibre = vehCapacidad[k] - vehCargaActual[k];
                if (capacidadLibre >= pesoPedido) {
                    if (idx == 0 || matrizDistancias[vehSector[k]][sectorPedido] < mejorDistancia) {
                        idx = k;
                        mejorDistancia = matrizDistancias[vehSector[k]][sectorPedido];
                    }
                }
            }
        }
        return idx;
    }

    // Obtiene la capacidad maxima individual de toda la flota
    // (sirve como referencia de "el vehiculo mas grande que existe").
    static double obtenerCapacidadMaxima(double[] vehCapacidad, int n) {
        double capMax = vehCapacidad[1];
        for (int k = 2; k <= n; k++) {
            if (vehCapacidad[k] > capMax) {
                capMax = vehCapacidad[k];
            }
        }
        return capMax;
    }

    // Calcula cuantos vehiculos (del tamano maximo disponible en la flota) se
    // necesitarian como minimo para poder transportar un peso dado.
    static int calcularVehiculosNecesarios(double pesoPedido, double capacidadMaxima) {
        int nec = 0;
        double acumulado = 0;

        if (capacidadMaxima > 0) {
            while (acumulado < pesoPedido) {
                nec++;
                acumulado += capacidadMaxima;
            }
        }
        return nec;
    }

    // ===========================================================================
    // SUBPROCESOS (encapsulan procesos completos del sistema)
    // ===========================================================================

    // Cuando NINGUN vehiculo puede llevar el pedido completo por si solo, este
    // subproceso reparte (fracciona) la carga entre todos los vehiculos disponibles
    // que tengan espacio libre, hasta cubrir el peso total o hasta agotar los
    // vehiculos existentes.
    static ResultadoCarga asignarCargaMultiple(String[] vehEstado, double[] vehCapacidad,
            double[] vehCargaActual, String[] vehCodigo, int n, double pesoPedido) {
        double restante = pesoPedido;
        double pesoAsignado = 0;
        StringBuilder vehiculosUsados = new StringBuilder();

        for (int k = 1; k <= n; k++) {
            if (restante > 0 && vehEstado[k].equals("Disponible")) {
                double capacidadLibre = vehCapacidad[k] - vehCargaActual[k];
                if (capacidadLibre > 0) {
                    if (capacidadLibre >= restante) {
                        vehCargaActual[k] += restante;
                        pesoAsignado += restante;
                        vehiculosUsados.append(vehCodigo[k]).append(" ");
                        vehEstado[k] = "En Ruta";
                        restante = 0;
                    } else {
                        vehCargaActual[k] += capacidadLibre;
                        pesoAsignado += capacidadLibre;
                        vehiculosUsados.append(vehCodigo[k]).append(" ");
                        vehEstado[k] = "En Ruta";
                        restante -= capacidadLibre;
                    }
                }
            }
        }

        ResultadoCarga r = new ResultadoCarga();
        r.pesoAsignado = pesoAsignado;
        r.vehiculosUsados = vehiculosUsados.toString();
        return r;
    }

    static void mostrarInventario(int[] prodCodigo, String[] prodNombre, int[] prodStock, int[] prodMinimo, int n) {
        borrarPantalla();
        System.out.println("--- INVENTARIO ACTUAL DE MERCANCIAS ---");
        System.out.println("CODIGO | PRODUCTO | STOCK | MINIMO | ESTADO");
        for (int i = 1; i <= n; i++) {
            System.out.print(prodCodigo[i] + " | " + prodNombre[i] + " | " + prodStock[i] + " | " + prodMinimo[i] + " | ");
            if (prodStock[i] <= prodMinimo[i]) {
                System.out.println("[ALERTA: REPOSICION CRITICA]");
            } else {
                System.out.println("[STOCK OPTIMO]");
            }
        }
    }

    static void registrarPedido(int[] pedCodigo, int[] pedSector, int[] pedProdCod, int[] pedCantidad,
            String[] pedEstado, int[] pedPrioridad, int n, int numSectores,
            int[] prodCodigo, String[] prodNombre, int[] prodStock, int[] prodMinimo, int nProd, Scanner sc) {
        borrarPantalla();
        System.out.println("--- REGISTRO DE NUEVA ENCOMIENDA/PEDIDO ---");

        int pos = 0;
        for (int i = 1; i <= n; i++) {
            if (pedCodigo[i] == 0 && pos == 0) {
                pos = i;
            }
        }

        if (pos == 0) {
            System.out.println("Error: Capacidad maxima de pedidos pendientes alcanzada.");
            return;
        }

        System.out.print("Ingrese codigo numerico para el pedido (Ej: 5001): ");
        int codigoNuevo = leerEntero(sc);

        // Validacion: codigo de pedido no repetido
        boolean repetido = false;
        for (int i = 1; i <= n; i++) {
            if (pedCodigo[i] == codigoNuevo) {
                repetido = true;
            }
        }

        if (repetido) {
            System.out.println("Error: ya existe un pedido registrado con ese codigo.");
            return;
        }

        pedCodigo[pos] = codigoNuevo;

        System.out.print("Seleccione Sector de Entrega (1:Centro, 2:Norte, 3:Sur, 4:Este, 5:Puerto Bolivar): ");
        pedSector[pos] = leerEntero(sc);
        while (pedSector[pos] < 1 || pedSector[pos] > numSectores) {
            System.out.print("Sector invalido. Ingrese un sector del 1 al " + numSectores + ": ");
            pedSector[pos] = leerEntero(sc);
        }

        // Se muestra el inventario completo (misma funcion que "Ver Inventario")
        // para que el usuario pueda consultar los codigos disponibles sin salir del registro.
        mostrarInventario(prodCodigo, prodNombre, prodStock, prodMinimo, nProd);

        System.out.print("Ingrese Codigo del Producto solicitado (101 a 105): ");
        pedProdCod[pos] = leerEntero(sc);
        System.out.print("Ingrese la cantidad requerida del producto: ");
        pedCantidad[pos] = leerEntero(sc);

        System.out.print("Prioridad del pedido (1: Urgente, 2: Normal): ");
        int prioridadIngresada = leerEntero(sc);
        while (prioridadIngresada != 1 && prioridadIngresada != 2) {
            System.out.print("Opcion invalida. Ingrese 1 (Urgente) o 2 (Normal): ");
            prioridadIngresada = leerEntero(sc);
        }
        pedPrioridad[pos] = prioridadIngresada;

        if (pedCantidad[pos] > 0) {
            pedEstado[pos] = "Pendiente";
            System.out.println("¡Pedido registrado exitosamente con estado PENDIENTE!");
        } else {
            System.out.println("Error de validacion: la cantidad debe ser mayor a cero.");
            pedCodigo[pos] = 0;
        }
    }

    static void planificarRutas(int[] pedCodigo, int[] pedSector, int[] pedProdCod, int[] pedCantidad,
            String[] pedEstado, int[] pedPrioridad, double[] pedDistancia, double[] pedTiempo,
            int[] prodCodigo, int[] prodStock, double[] prodPeso, String[] vehCodigo, double[] vehCapacidad,
            double[] vehCargaActual, String[] vehEstado, int[] vehSector, double[][] matrizDistancias,
            String[] sectorNombre, int nPed, int nProd, int nVeh) {

        borrarPantalla();
        System.out.println("--- ENRUTAMIENTO AUTOMATICO E INTELIGENTE ---");
        boolean encontrado = false;

        // Recomendacion de agrupacion: pedidos pendientes por sector
        for (int i = 1; i <= nPed; i++) {
            int sectoresPendientes = 0;
            for (int j = 1; j <= nPed; j++) {
                if (pedEstado[j].equals("Pendiente") && pedSector[j] == pedSector[i] && pedCodigo[i] != 0) {
                    sectoresPendientes++;
                }
            }
            if (pedCodigo[i] != 0 && pedEstado[i].equals("Pendiente") && sectoresPendientes > 1) {
                System.out.println("Sugerencia: agrupar los pedidos del sector " + sectorNombre[pedSector[i]] + " en un mismo vehiculo.");
            }
        }

        // Primero se procesan los pedidos Urgentes (prioridad 1) y luego los Normales (2)
        for (int k = 1; k <= 2; k++) {
            if (k == 1) {
                System.out.println("===== PROCESANDO COLA DE PRIORIDAD: URGENTE =====");
            } else {
                System.out.println("===== PROCESANDO COLA DE PRIORIDAD: NORMAL =====");
            }

            for (int i = 1; i <= nPed; i++) {
                if (pedEstado[i].equals("Pendiente") && pedPrioridad[i] == k) {
                    encontrado = true;
                    String etiquetaPrioridad = (pedPrioridad[i] == 1) ? "URGENTE" : "NORMAL";
                    System.out.println("Procesando pedido Nro: " + pedCodigo[i] + " (Prioridad: " + etiquetaPrioridad + ")");

                    // Verificar stock disponible del producto solicitado
                    boolean stockOk = false;
                    double pesoProdActual = 0;
                    for (int j = 1; j <= nProd; j++) {
                        if (prodCodigo[j] == pedProdCod[i]) {
                            pesoProdActual = prodPeso[j];
                            if (prodStock[j] >= pedCantidad[i]) {
                                stockOk = true;
                            }
                        }
                    }

                    double pesoPedido = pesoProdActual * pedCantidad[i];

                    if (stockOk) {
                        int vehElegido = asignarVehiculo(vehEstado, vehCapacidad, vehCargaActual, vehSector, matrizDistancias, pesoPedido, pedSector[i], nVeh);

                        if (vehElegido != 0) {
                            pedEstado[i] = "En Preparacion";
                            vehEstado[vehElegido] = "En Ruta";
                            vehCargaActual[vehElegido] += pesoPedido;

                            double distanciaCalc = matrizDistancias[vehSector[vehElegido]][pedSector[i]];
                            double tiempoCalc = calcularTiempoEntrega(distanciaCalc, 40);
                            double combustibleCalc = calcularCombustible(distanciaCalc, 8);

                            pedDistancia[i] = distanciaCalc;
                            pedTiempo[i] = tiempoCalc;

                            System.out.println(">> DECISION: Pedido " + pedCodigo[i] + " asignado al vehiculo " + vehCodigo[vehElegido]);
                            System.out.println("   Motivo: capacidad disponible (" + pesoPedido + " kg de " + vehCapacidad[vehElegido]
                                    + " kg), sector " + sectorNombre[pedSector[i]] + " y menor distancia (" + distanciaCalc + " km).");
                            System.out.println("   Tiempo estimado: " + (tiempoCalc * 60) + " minutos | Combustible estimado: " + combustibleCalc + " galones.");
                        } else {
                            // ---- Ya no se queda "flotando" en espera sin explicacion ----
                            double capacidadMaxima = obtenerCapacidadMaxima(vehCapacidad, nVeh);
                            int vehiculosNecesarios = calcularVehiculosNecesarios(pesoPedido, capacidadMaxima);

                            if (pedPrioridad[i] == 1) {
                                // URGENTE: se intenta de inmediato fraccionar el pedido entre varios vehiculos
                                ResultadoCarga r = asignarCargaMultiple(vehEstado, vehCapacidad, vehCargaActual, vehCodigo, nVeh, pesoPedido);
                                double pesoAsignado = r.pesoAsignado;
                                String vehiculosUsados = r.vehiculosUsados;

                                if (pesoAsignado >= pesoPedido) {
                                    pedEstado[i] = "En Preparacion";
                                    double distanciaCalc = matrizDistancias[1][pedSector[i]];
                                    double tiempoCalc = calcularTiempoEntrega(distanciaCalc, 40);
                                    double combustibleCalc = calcularCombustible(distanciaCalc, 8);
                                    pedDistancia[i] = distanciaCalc;
                                    pedTiempo[i] = tiempoCalc;

                                    System.out.println(">> DECISION (URGENTE-FRACCIONADO): el pedido (" + pesoPedido
                                            + " kg) supera la capacidad del vehiculo mas grande (" + capacidadMaxima
                                            + " kg, se necesitaban ~" + vehiculosNecesarios + " vehiculos) y fue repartido entre: " + vehiculosUsados);
                                } else {
                                    if (pesoAsignado > 0) {
                                        System.out.println(">> ALERTA (URGENTE): solo se logro cubrir " + pesoAsignado + " kg de " + pesoPedido + " kg usando: " + vehiculosUsados);
                                    } else {
                                        System.out.println(">> ALERTA (URGENTE): no hay vehiculos disponibles en este momento.");
                                    }
                                    System.out.println("   Se necesitan al menos " + vehiculosNecesarios + " vehiculos de " + capacidadMaxima
                                            + " kg c/u para este pedido. Por ser URGENTE, quedara primero en la fila y se reintentara automaticamente en la proxima planificacion.");
                                }
                            } else {
                                // NORMAL: no se fracciona de forma automatica (eso se reserva a lo Urgente);
                                // se informa con precision y el pedido queda en cola ordenada, sin bloquear el sistema.
                                if (pesoPedido > capacidadMaxima) {
                                    System.out.println(">> ALERTA (NORMAL): este pedido (" + pesoPedido + " kg) supera la capacidad maxima individual de la flota ("
                                            + capacidadMaxima + " kg). Se necesitarian al menos " + vehiculosNecesarios
                                            + " vehiculos; considere dividirlo en pedidos mas pequenos o registrarlo como URGENTE para fraccionamiento automatico.");
                                } else {
                                    System.out.println(">> ALERTA (NORMAL): no hay vehiculos disponibles en este momento (todos ocupados o en mantenimiento). "
                                            + "El pedido queda en cola y se procesara despues de los pedidos Urgentes pendientes.");
                                }
                            }
                        }
                    } else {
                        System.out.println(">> ALERTA: salida denegada por falta de stock suficiente.");
                    }
                    System.out.println("--------------------------------------------");
                }
            }
        }

        if (!encontrado) {
            System.out.println("No existen pedidos en estado Pendiente.");
        }
    }

    static void realizarEntregas(int[] pedCodigo, int[] pedProdCod, int[] pedCantidad, String[] pedEstado,
            int[] prodCodigo, int[] prodStock, int[] prodEntregado, String[] vehCodigo, String[] vehEstado,
            double[] vehCargaActual, int[] vehEntregas, int nPed, int nProd, int nVeh) {

        borrarPantalla();
        System.out.println("--- SIMULADOR DE DESPACHO Y ENTREGA OPERATIVA ---");
        boolean encontrado = false;

        for (int i = 1; i <= nPed; i++) {
            if (pedEstado[i].equals("En Preparacion")) {
                pedEstado[i] = "Entregado";
                encontrado = true;

                actualizarInventario(prodCodigo, prodStock, pedProdCod[i], pedCantidad[i], nProd);

                for (int j = 1; j <= nProd; j++) {
                    if (prodCodigo[j] == pedProdCod[i]) {
                        prodEntregado[j] += pedCantidad[i];
                    }
                }

                System.out.println(">> [ENTREGA EXITOSA] El pedido Nro: " + pedCodigo[i] + " ha sido entregado.");
            }
        }

        // Liberar vehiculos que estaban en ruta y contabilizar su entrega
        for (int j = 1; j <= nVeh; j++) {
            if (vehEstado[j].equals("En Ruta")) {
                vehEstado[j] = "Disponible";
                vehCargaActual[j] = 0;
                vehEntregas[j] = vehEntregas[j] + 1;
            }
        }

        if (!encontrado) {
            System.out.println("No hay despachos listos en preparacion.");
        }
    }

    static void actualizarInventario(int[] prodCodigo, int[] prodStock, int codigoProducto, int cantidad, int nProd) {
        for (int j = 1; j <= nProd; j++) {
            if (prodCodigo[j] == codigoProducto) {
                prodStock[j] -= cantidad;
            }
        }
    }

    static void generarReporte(int[] pedCodigo, String[] pedEstado, double[] pedDistancia, double[] pedTiempo,
            int[] prodCodigo, String[] prodNombre, int[] prodEntregado, String[] vehCodigo, String[] vehEstado,
            int[] vehEntregas, int nPed, int nProd, int nVeh) {

        borrarPantalla();
        System.out.println("=========================================================");
        System.out.println("      REPORTE INTEGRAL DE DESEMPENO LOGISTICO            ");
        System.out.println("=========================================================");

        int totalPedidos = 0, entregados = 0, pendientes = 0;
        for (int i = 1; i <= nPed; i++) {
            if (pedCodigo[i] != 0) {
                totalPedidos++;
                if (pedEstado[i].equals("Entregado")) {
                    entregados++;
                } else {
                    pendientes++;
                }
            }
        }

        if (totalPedidos > 0) {
            System.out.println("· Cantidad total de ordenes registradas: " + totalPedidos);
            System.out.println("· Ordenes completadas y despachadas: " + entregados);
            System.out.println("· Ordenes retenidas o pendientes: " + pendientes);
            System.out.println("· Tasa de efectividad operativa: " + ((double) entregados / totalPedidos * 100) + "%");
        } else {
            System.out.println("· No existen pedidos registrados todavia.");
        }

        System.out.println("=========================================================");
        System.out.println("ESTADO DE LA FLOTA VEHICULAR:");
        for (int j = 1; j <= nVeh; j++) {
            System.out.println("- Vehiculo: " + vehCodigo[j] + " | Estado: " + vehEstado[j] + " | Entregas realizadas: " + vehEntregas[j]);
        }

        mostrarEstadisticas(pedCodigo, pedEstado, pedDistancia, pedTiempo, prodCodigo, prodNombre, prodEntregado, vehCodigo, vehEntregas, nPed, nProd, nVeh);

        System.out.println("=========================================================");
    }

    static void mostrarEstadisticas(int[] pedCodigo, String[] pedEstado, double[] pedDistancia, double[] pedTiempo,
            int[] prodCodigo, String[] prodNombre, int[] prodEntregado, String[] vehCodigo, int[] vehEntregas,
            int nPed, int nProd, int nVeh) {

        System.out.println("=========================================================");
        System.out.println("ESTADISTICAS DEL SISTEMA:");

        // Distancia y tiempo promedio de los pedidos ya entregados
        double sumaDistancia = 0, sumaTiempo = 0;
        int cantidadEntregados = 0;
        for (int i = 1; i <= nPed; i++) {
            if (pedCodigo[i] != 0 && pedEstado[i].equals("Entregado")) {
                sumaDistancia += pedDistancia[i];
                sumaTiempo += pedTiempo[i];
                cantidadEntregados++;
            }
        }

        if (cantidadEntregados > 0) {
            double distanciaPromedio = sumaDistancia / cantidadEntregados;
            double tiempoPromedio = (sumaTiempo / cantidadEntregados) * 60; // horas -> minutos
            System.out.println("· Distancia promedio recorrida por entrega: " + distanciaPromedio + " km");
            System.out.println("· Tiempo promedio de entrega: " + tiempoPromedio + " minutos");
        } else {
            System.out.println("· Aun no hay entregas completadas para calcular promedios de ruta.");
        }

        // Vehiculo mas utilizado (mayor numero de entregas)
        int idxMaxVeh = 1;
        for (int j = 2; j <= nVeh; j++) {
            if (vehEntregas[j] > vehEntregas[idxMaxVeh]) {
                idxMaxVeh = j;
            }
        }
        if (vehEntregas[idxMaxVeh] > 0) {
            System.out.println("· Vehiculo mas utilizado: " + vehCodigo[idxMaxVeh] + " (" + vehEntregas[idxMaxVeh] + " entregas)");
        } else {
            System.out.println("· Aun no se registran entregas por vehiculo.");
        }

        // Producto mas distribuido (mayor cantidad entregada acumulada)
        int idxMaxProd = 1;
        for (int j = 2; j <= nProd; j++) {
            if (prodEntregado[j] > prodEntregado[idxMaxProd]) {
                idxMaxProd = j;
            }
        }
        if (prodEntregado[idxMaxProd] > 0) {
            System.out.println("· Producto mas distribuido: " + prodNombre[idxMaxProd] + " (" + prodEntregado[idxMaxProd] + " unidades)");
        } else {
            System.out.println("· Aun no se registran productos distribuidos.");
        }
    }

    // ===========================================================================
    // UTILIDADES DE ENTRADA / SALIDA
    // ===========================================================================

    // Lee un entero de forma segura, reintentando si el usuario ingresa texto no numerico.
    static int leerEntero(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.print("Ingrese un valor numerico valido: ");
            sc.next();
        }
        int valor = sc.nextInt();
        sc.nextLine(); // limpia el salto de linea pendiente
        return valor;
    }

    // Simula "Borrar Pantalla" del pseudocodigo (portable en cualquier consola).
    static void borrarPantalla() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    // Simula "Escribir ...; Leer pausa" para continuar tras presionar Enter.
    static void esperarEnter(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        sc.nextLine();
    }
}
