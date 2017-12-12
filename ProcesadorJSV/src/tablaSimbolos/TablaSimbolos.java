package tablaSimbolos;

import analizadorLexico.AnalizadorLexico;
import errores.DeclaracionIncompatibleException;
import java.io.*;
import java.util.*;
import token.Token;


public class TablaSimbolos {

   private int contadorRegistros = 0;
   private ArrayList<Object[]> tablaSimbolos;
   private int desplazamiento = 0;
   public static int indice = 1;

   private void addPalabrasReservadas() throws DeclaracionIncompatibleException {
       
       this.addTs(new Token("PR", "function"));
       this.addDireccion(new Token("PR", "function"), "function".length());
       this.addTipo(new Token("PR", "function"), "PR");

       this.addTs(new Token("PR", "if"));
       this.addDireccion(new Token("PR", "if"), "if".length());
       this.addTipo(new Token("PR", "if"), "PR");
       
       this.addTs(new Token("PR", "else"));
       this.addDireccion(new Token("PR", "else"), "else".length());
       this.addTipo(new Token("PR", "else"), "PR");

       this.addTs(new Token("PR", "write"));
       this.addDireccion(new Token("PR", "write"), "write".length());
       this.addTipo(new Token("PR", "write"), "PR");

       this.addTs(new Token("PR", "prompt"));
       this.addDireccion(new Token("PR", "prompt"), "prompt".length());
       this.addTipo(new Token("PR", "prompt"), "PR");

       this.addTs(new Token("PR", "return"));
       this.addDireccion(new Token("PR", "return"), "return".length());
       this.addTipo(new Token("PR", "return"), "PR");

       this.addTs(new Token("PR", "var"));
       this.addDireccion(new Token("PR", "var"), "var".length());
       this.addTipo(new Token("PR", "var"), "PR");

       this.addTs(new Token("PR", "void"));
       this.addDireccion(new Token("PR", "void"), "void".length());
       this.addTipo(new Token("PR", "void"), "PR");
       
       this.addTs(new Token("PR", "int"));
       this.addDireccion(new Token("PR", "int"), "int".length());
       this.addTipo(new Token("PR", "int"), "PR");
       
       this.addTs(new Token("PR", "chars"));
       this.addDireccion(new Token("PR", "chars"), "chars".length());
       this.addTipo(new Token("PR", "chars"), "PR");
       
       this.addTs(new Token("PR", "bool"));
       this.addDireccion(new Token("PR", "bool"), "bool".length());
       this.addTipo(new Token("PR", "bool"), "PR");
   }

   public TablaSimbolos() throws DeclaracionIncompatibleException {
       this.tablaSimbolos = new ArrayList<Object[]>();
       this.addPalabrasReservadas();
   }

   private void escribirCabecera(BufferedWriter tablaWriter) throws IOException {

       tablaWriter.write("LEX");
       tablaWriter.write("\t\t\t\t");

       tablaWriter.write("TIPO");
       tablaWriter.write("\t\t\t\t");
       tablaWriter.write("DIR");
       tablaWriter.write("\t\t\t\t");
       tablaWriter.write("NUMPARAM");
       tablaWriter.write("\t\t\t\t");
       tablaWriter.write("TIPODEV");
       tablaWriter.write("\t\t\t\t");
       tablaWriter.write("ETIQ");
       tablaWriter.newLine();
   }

   public void volcarTabla(BufferedWriter tablaWriter) throws IOException {
       TablaSimbolos tablaAVolcar;
       if (this.tablaSimbolos.get(contadorRegistros - 1)[0] instanceof TablaSimbolos) {
            //tabla local
           tablaAVolcar = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           tablaWriter.newLine();
           tablaWriter.newLine();
           escribirCabecera(tablaWriter);

       } else {
           //tabla global
           tablaAVolcar = this;
           tablaWriter.write("TABLA PRINCIPAL #"+indice++);
           tablaWriter.newLine();
           tablaWriter.newLine();
           escribirCabecera(tablaWriter);

       }
       for (int i = 0; i < tablaAVolcar.tablaSimbolos.size(); i++) {
           Object[] fila = tablaAVolcar.tablaSimbolos.get(i);
           for (int j = 0; j < fila.length; j++) {
               String aImprimir = "";
               if (fila[j] != null) {
                   aImprimir = fila[j].toString();
                   tablaWriter.write(aImprimir);
               }
               if (aImprimir.length() >= 16) {
                   tablaWriter.write("\t\t");
               } else if (aImprimir.length() >= 12) {
                   tablaWriter.write("\t\t\t");
               } else if (aImprimir.length() >= 8) {
                   tablaWriter.write("\t\t\t\t");
               } else if (aImprimir.length() >= 4) {
                   tablaWriter.write("\t\t\t\t\t");
               } else {
                   tablaWriter.write("\t\t\t\t\t\t");
               }

           }
           tablaWriter.newLine();
       }
       tablaWriter.newLine();
   }

   public void addTs(Token token) {
       /*
        Posicion 0 = lexema
        Posicion 1 = tipo
        Posicion 2 = direccion
        Posicion 3 = numero parametros (solo para funciones)
        Posicion 4 = tipo devuelto (solo para funciones)
        Posicion 5 = etiqueta (solo para funciones)
        */
       boolean anadidoLocal = false;
       if (contadorRegistros > 0 && this.tablaSimbolos.get(contadorRegistros - 1)[0] instanceof TablaSimbolos) {
           //tabla local
           TablaSimbolos tLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           tLocal.addTs(token);
           anadidoLocal = true;
       }
       if (!anadidoLocal) {
           this.tablaSimbolos.add(new Object[6]);
           this.tablaSimbolos.get(contadorRegistros)[0] = (String) token.getValor();
           contadorRegistros++;
       }
   }

   public void addTipo(Token token, String tipo) throws DeclaracionIncompatibleException {
       if (this.buscaTS(token.getValor())[0] != null) {
           if (this.getTipo(token) != null && !this.getTipo(token).equals(tipo) && ("FUNC".equals(tipo) || "FUNC".equals(this.getTipo(token)))) {
               throw new DeclaracionIncompatibleException("Error en linea " + AnalizadorLexico.linea + ". La variable o funcion '" + token.getValor() + "' ha sido declarada previamente.");
           }
           Integer[] contTemporal = buscaTS(token.getValor());
           if (contTemporal[1] == 1) {
               TablaSimbolos tLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
               tLocal.getTablaSimbolos().get(contTemporal[0])[1] = tipo;
           } else {
               this.tablaSimbolos.get(contTemporal[0])[1] = tipo;
           }
       }
   }

   public void addParametros(int nParam) {
       this.tablaSimbolos.get(contadorRegistros - 2)[3] = nParam;
   }

   public void addEtiqueta() {
       String etiqueta = "FUNC.";
       etiqueta += "" + this.tablaSimbolos.get(contadorRegistros - 2)[0];
       this.tablaSimbolos.get(contadorRegistros - 2)[5] = etiqueta;
   }

   public void addDevuelve(String tipo) {
       this.tablaSimbolos.get(contadorRegistros - 1)[4] = tipo;
   }

   public void addDireccion(Token token, int ancho) {
       if (this.buscaTS(token.getValor())[0] != null) {
           Integer[] contTemporal = buscaTS(token.getValor());
           if (contTemporal[1] == 1) {
               TablaSimbolos tLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
               tLocal.tablaSimbolos.get(contTemporal[0])[2] = tLocal.desplazamiento;
               tLocal.desplazamiento += ancho;
           } else {
               this.tablaSimbolos.get(contTemporal[0])[2] = desplazamiento;
               desplazamiento += ancho;
           }
       }
   }

   public Integer[] buscaTS(String palabra) {
       Integer[] estaEnLocal = new Integer[2];
       if (contadorRegistros > 0 && this.tablaSimbolos.get(contadorRegistros - 1)[0] instanceof TablaSimbolos) {
           //tabla local
           TablaSimbolos tLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           estaEnLocal = tLocal.buscaTS(palabra);
           estaEnLocal[1] = 1;
       }
       if (estaEnLocal[0] == null) {
           Integer[] contador = new Integer[2];
           //pos tabla local
           contador[0] = 0;
           //pos tabla global
           contador[1] = 0;
           boolean encontrado = false;
           while (contador[0] < this.tablaSimbolos.size() && !encontrado) {
               if (this.tablaSimbolos.get(contador[0])[0].equals(palabra)) {
                   encontrado = true;
               } else {
                   contador[0]++;
               }
           }
           if (!encontrado) {
               contador[0] = null;
           }
           return contador;
       }
       return estaEnLocal;
   }

   public Integer[] buscaTSGlobal(String palabra) {
       Integer[] estaEnLocal = new Integer[2];
       Integer[] contador = new Integer[2];
       //pos tabla local
       contador[0] = 0;
       //pos tabla global
       contador[1] = 0;
       
       boolean encontrado = false;
       while (contador[0] < this.tablaSimbolos.size() && !encontrado) {
           if (this.tablaSimbolos.get(contador[0])[0].equals(palabra)) {
               encontrado = true;
           } else {
               contador[0]++;
           }
       }
       if (!encontrado) {
           contador[0] = null;
       }
       return contador;
   }

   public void crearTSL() throws DeclaracionIncompatibleException {
       this.tablaSimbolos.add(new Object[6]);
       this.tablaSimbolos.get(contadorRegistros)[0] = new TablaSimbolos();
       contadorRegistros++;
   }

   public void borraTS() {
       this.tablaSimbolos.remove(contadorRegistros - 1);
       contadorRegistros--;
   }

   public TablaSimbolos accederTSL() {
       if ("TablaSimbolos".equals(this.tablaSimbolos.get(contadorRegistros - 1)[0].getClass().getCanonicalName())) {
           return (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
       } else {
           return null;
       }
   }

   public int getNParametros(Token token) {
       int toReturn = -1;
       Integer[] posYTab = this.buscaTS(token.getValor());
       if (posYTab[1] == 1) {
           System.out.println("ERROR en getNParametros");
       } else {//Estamos en la global
           Integer nParam = (Integer) this.tablaSimbolos.get(posYTab[0].intValue())[3];
           toReturn = nParam;
       }
       return toReturn;
   }

   public int getNParametrosGlobal(Token token) {
       Integer[] posYTab = this.buscaTSGlobal(token.getValor());
       Integer nParam=null;
       if (posYTab[0] != null) {
           nParam = (Integer) this.tablaSimbolos.get(posYTab[0].intValue())[3];
       }
       if(nParam!=null){
           return nParam;
       }else{
           return -1;
       }
   }

   public String getTipo(Token token) {
       Integer[] posYTab = this.buscaTS(token.getValor());
       String tipo = null;
       if (posYTab[1] == 0 && posYTab[0] != null) {
           //tabla global
           tipo = (String) this.tablaSimbolos.get(posYTab[0])[1];
       } else if (posYTab[1] == 1 && posYTab[0] != null) {
           //tabla local
           TablaSimbolos tLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           tipo = (String) tLocal.tablaSimbolos.get(posYTab[0])[1];
       }
       return tipo;
   }

   public String getDevuelve(Token token) {
       Integer[] posYTab = this.buscaTS(token.getValor());
       String devuelve = null;
       if (posYTab[1] == 0 && posYTab[0] != null) {
           //tabla global
           devuelve = (String) this.tablaSimbolos.get(posYTab[0])[4];
       } else if (posYTab[1] == 1 && posYTab[0] != null) {
           //tabla local
           TablaSimbolos tLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           devuelve = (String) tLocal.tablaSimbolos.get(posYTab[0])[4];
       }
       return devuelve;
   }

   public void vaciarTabla() {
       while (!this.tablaSimbolos.isEmpty()) {
           this.tablaSimbolos.remove(0);
       }
       this.contadorRegistros = 0;
       this.desplazamiento = 0;
   }

   public boolean isPR(String cadena) {
       return "var".equals(cadena) || "write".equals(cadena) || "prompt".equals(cadena) || "if".equals(cadena) || "function".equals(cadena) || "else".equals(cadena) || "return".equals(cadena) || "int".equals(cadena) || "chars".equals(cadena) || "bool".equals(cadena) || "void".equals(cadena);
   }

   public int getContadorRegistros() {
       return contadorRegistros;
   }

   public void setContadorRegistros(int contadorRegistros) {
       this.contadorRegistros = contadorRegistros;
   }

   public ArrayList<Object[]> getTablaSimbolos() {
       return tablaSimbolos;
   }

   public void setTablaSimbolos(ArrayList<Object[]> tablaSimbolos) {
       this.tablaSimbolos = tablaSimbolos;
   }

}