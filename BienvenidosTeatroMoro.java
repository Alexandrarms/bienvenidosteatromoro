package com.mycompany.bienvenidosteatromoro;

import java.util.*;

/**
 *
 * @author alexa
 */
public class BienvenidosTeatroMoro {
   
    //Variables estáticas para estadísticas globales
    static double totalIngresos = 0;
    static int totalEntradasVendidas = 0;
    static int totalDescuentosAplicados = 0;
    static int totalDescuentosMultiples = 0;
    static int totalDescuentosIndividuales = 0;
    
    // Mapa de zona y estado de asientos
    static Map<String, char[]> mapaAsientos = new HashMap<>();
    static List<Entrada> entradasVendidas = new ArrayList<>();
    static List<Entrada> ultimasEntradasVendidas = new ArrayList<>();
   
    
    public static void main(String[] args) {
        inicializarMapaAsientos();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("BIENVENIDOS A TEATRO MORO");
        
        while (true) { 
            System.out.println("\n::::MENÚ PRINCIPAL::::\n");
            System.out.println("1.- Venta de entradas");
            System.out.println("2.- Visualización resumen de ventas");
            System.out.println("3.- Generar boleta");
            System.out.println("4.- Mostrar Estado de Asientos");
            System.out.println("0.- Salir");
            System.out.print("Seleccione una opción: ");
            
            int opcion = leerEntero(scanner);
                
            switch (opcion) {
                case 1 -> totalIngresos += venderEntradas(scanner);
                case 2 -> mostrarResumenVentas();
                case 3 -> generarBoleta();
                case 4 -> mostrarMapaAsientos();
                case 0 -> {
                        System.out.println(" Gracias por su compra.");
                        scanner.close();
                        return;
                }
                default -> System.out.println(" Opción inválida.");
            }           
        }
    }
    private static void inicializarMapaAsientos(){
        mapaAsientos.put("VIP", new char[20]);
        mapaAsientos.put("PLATEA", new char[20]);
        mapaAsientos.put("BALCÓN", new char[20]);
        for (char[] zona : mapaAsientos.values()) {
            Arrays.fill(zona, '0'); 
        }
    }
   
    
    private static double venderEntradas(Scanner scanner) {
        ultimasEntradasVendidas.clear();
        boolean algunDescuentoIndividual = false;
        List<CompraTemporal> comprasTemp = new ArrayList<>();
        
        //Compra de asientos
        while (true) {
            mostrarMapaAsientos();
            String zona = seleccionarZona(scanner);
            int asiento = seleccionarAsiento(scanner, zona);
            double precio = obtenerPrecioPorZona(zona);
       
            String tipoDesc = seleccionarDescuento(scanner);
            double descuento = switch (tipoDesc) {
                case "ESTUDIANTE" -> {
                    algunDescuentoIndividual = true;
                    totalDescuentosIndividuales++;
                    totalDescuentosAplicados++;
                    yield 0.10;
                }
                case "TERCERA" -> {
                    algunDescuentoIndividual = true;
                    totalDescuentosIndividuales++;
                    totalDescuentosAplicados++;
                    yield 0.15;
                }
                default -> 0;
            };
       
            comprasTemp.add(new CompraTemporal(zona, asiento, precio, descuento, tipoDesc));
            mapaAsientos.get(zona)[asiento -1] = 'E'; // 'E' de escogido
       
            System.out.print("¿Desea comprar otra entrada? (SI/NO): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("SI")) break;
        }
       
    
        double descuentoMultiple = 0;
        if (!algunDescuentoIndividual && comprasTemp.size() > 1){
            int cant = comprasTemp.size();
            if (cant == 2) descuentoMultiple = 0.05;
            else if (cant == 3) descuentoMultiple = 0.10;
            else if (cant >= 4) descuentoMultiple = 0.15;
        }
        
        double totalCompra = 0;
        List<Entrada> entradasFinales = new ArrayList<>();
    
        
        for (CompraTemporal c : comprasTemp){
            double precioConDesc = c.precioBase * (1 - c.descuentoIndividual);
            if (descuentoMultiple > 0){
                precioConDesc *= (1 - descuentoMultiple);
            }
          
            entradasFinales.add(new Entrada(c.zona, c.asiento, c.precioBase, precioConDesc, c.getTipoDescuento()));
            totalEntradasVendidas++;
            totalCompra += precioConDesc;
            
        }
        
        System.out.println("\n::::RESUMEN TOTAL DE LA COMPRA::::");
        for (Entrada e : entradasFinales){
            mapaAsientos.get(e.getZona())[e.getAsiento() - 1] = 'X'; // Confirmar que está vendido
             System.out.printf("Zona: %s, Asiento: %d, Precio Final: $%,.0f CLP%n",
                     e.getZona(), e.getAsiento(), e.getPrecioFinal());
        }
      
        if (descuentoMultiple > 0){
            System.out.printf("\nDescuento por compra múltiple aplicado (%.0f%%).%n", descuentoMultiple * 100);
            totalDescuentosMultiples++;
            totalDescuentosAplicados++;
        } else if (comprasTemp.size() > 1 && algunDescuentoIndividual){
            System.out.println("\nNo se aplicó descuento por compra múltiple porque una o más entradas ya tenían descuento individual.");
        }

        entradasVendidas.addAll(entradasFinales);
        ultimasEntradasVendidas.addAll(entradasFinales); 
        return finalizarCompra(scanner, totalCompra);
        
    }  

    private static void mostrarResumenVentas() {
        System.out.println("\n::::RESUMEN DE VENTAS::::\n");
        System.out.printf("Total ingresos: $%,.0f CLP%n", totalIngresos);
        System.out.printf("Total entradas vendidas: %d%n", totalEntradasVendidas);
        System.out.printf("Total descuentos aplicados: %d%n", totalDescuentosAplicados);
        System.out.printf("Total descuentos múltiples: %d%n", totalDescuentosMultiples);
        System.out.printf("Total descuentos Individuales: %d%n", totalDescuentosIndividuales);
    }

    
    private static void generarBoleta(){
        if (ultimasEntradasVendidas.isEmpty()) {
            System.out.println("No hay compras recientes para generar boleta.");
            return;
        }
        System.out.println("\n----------------------------------------");
        System.out.println(":::::::::::: BOLETA DE COMPRA ::::::::::");
        System.out.println("                TEATRO MORO               ");
        System.out.println("----------------------------------------\n");
        double total = 0;
        for (Entrada e : ultimasEntradasVendidas){
            double porcentajeDescuento = (1 - (e.getPrecioFinal() / e.getPrecioBase())) * 100;
            System.out.printf("Zona: %s\nAsiento: %d\nPrecioBase: $%,.0f CLP\nDescuento Aplicado: %.0f%%\nPrecio Final: $%,.0f CLP\n\n",
                    e.getZona(), e.getAsiento(), e.getPrecioBase(), porcentajeDescuento, e.getPrecioFinal());
            total += e.getPrecioFinal();
        }
        System.out.printf("\n*Total: $%,.0f CLP%n", total);
        System.out.println("------------------------------------------");
        System.out.println("¡GRACIAS POR SU VISITA AL TEATRO MORO!");
    }

    private static void mostrarMapaAsientos(){
        System.out.println("\n::::ESTADO DE LOS ASIENTOS::::");
        for (String zona : List.of("VIP", "PLATEA", "BALCÓN")){
            System.out.print("Zona " + zona + ": ");
            for (char estado : mapaAsientos.get(zona)){
                System.out.print("[" + estado + "]");
            }
            System.out.println();
        }
        System.out.println("Leyenda: [0]=Libre [E]=Escogido [X]=Vendido");
    }  
    
    private static String seleccionarZona(Scanner scanner){
        while (true){
            System.out.print("\nSELECCIONE ZONA A COMPRAR (VIP, PLATEA, BALCÓN): ");
            String zona = scanner.nextLine().toUpperCase();
            if (zona.equals("VIP") || zona.equals("PLATEA") || zona.equals("BALCÓN")) return zona;
            System.out.println(" Zona inválida. Intente nuevamente.");     
        }
    }       
       
    private static int seleccionarAsiento(Scanner scanner, String zona){
        char[] asientos = mapaAsientos.get(zona);
        while (true) {
            System.out.print(" Ingrese el número de asiento (1-20): ");
            int num = leerEntero(scanner);
            if (num >= 1 && num <= 20) {
                if (asientos[num - 1] == '0') {
                    return num;
                } else { 
                    System.out.println("Asiento ocupado. Elija otro.");
                }
            } else { 
                System.out.println("Número inválido. Intente entre 1 y 20.");
            } 
        }
    }    
                
    private static double obtenerPrecioPorZona(String zona){
        return switch (zona) {
            case "VIP" -> 50000;
            case "PLATEA" -> 40000;
            case "BALCÓN" -> 30000;
            default -> 0;
        };
    }
    private static String seleccionarDescuento(Scanner scanner){
        while (true) {
            System.out.print(" Descuento: (¿ES USTED ESTUDIANTE/TERCERA/NINGUNO?): ");
            String tipo = scanner.nextLine().trim().toUpperCase();
            if (tipo.equals("ESTUDIANTE") || tipo.equals("TERCERA") || tipo.equals("NINGUNO")) {
                return tipo;
            }
            System.out.println("Opción inválida.");
        }
    }
    
   
    private static int leerEntero(Scanner scanner){
        while (!scanner.hasNextInt()) {
            System.out.println("Debe ingresar un número");
            scanner.next();
        }
        int num = scanner.nextInt();
        scanner.nextLine();
        return num;
    }
    
    
    private static double finalizarCompra(Scanner scanner, double total){
        System.out.printf("\nTotal a pagar: $%,.0f CLP%n", total);
        String metodo = obtenerMetodoPago(scanner);
        System.out.printf("Pago realizado con %s. ¡GRACIAS POR SU COMPRA!%n", metodo);
        return total;
    }
        
        
    private static String obtenerMetodoPago(Scanner scanner){
        while (true){
            System.out.print("Seleccione el método de pago (EFECTIVO/TARJETA): ");
            String metodoPago = scanner.nextLine().trim().toUpperCase();
            if (metodoPago.equals("EFECTIVO") || metodoPago.equals("TARJETA")){
                return metodoPago;
            }
            System.out.println("Método inválido. Intente nuevamente.");
        }
    }
    

    // Clase interna para representar una entrada
    static class Entrada {
        private final String zona;
        private final int asiento;
        private final double precioBase;
        private final double precioFinal;
        private final String tipoDescuento; // estudiante, tercera, ninguno
        
        public Entrada(String zona, int asiento, double precioBase, double precioFinal, String tipoDescuento){
            this.zona = zona;
            this.asiento = asiento;
            this.precioBase = precioBase;
            this.precioFinal = precioFinal;
            this.tipoDescuento = tipoDescuento;
        }
        
        public String getZona(){ return zona; }
        public int getAsiento(){ return asiento; }
        public double getPrecioBase() { return precioBase; }
        public double getPrecioFinal(){ return precioFinal; }
        public String getTipoDescuento() { return tipoDescuento; }
    }
    
    
    static class CompraTemporal {
        String zona;
        int asiento;
        double precioBase;
        double descuentoIndividual;
        String tipoDescuento;
        
        public CompraTemporal(String zona, int asiento, double precioBase, double descuentoIndividual, String tipoDescuento){
            this.zona = zona;
            this.asiento = asiento;
            this.precioBase = precioBase;
            this.descuentoIndividual = descuentoIndividual;
            this.tipoDescuento = tipoDescuento;
        }
        
        public String getTipoDescuento(){
            return tipoDescuento;
        }    
    }
}