package analizadorSintactico;

import analizadorLexico.AnalizadorLexico;
import errores.*;
import java.io.*;
import tablaSimbolos.TablaSimbolos;
import token.Token;


public class AnalizadorSintactico {

    private String parse;
    private AnalizadorLexico analizador;
    private TablaSimbolos tS;
    private Token tokenDevuelto;
    private BufferedWriter tablasWriter;
    private BufferedWriter parseWriter;
    private BufferedWriter errorWriter;

    //Atributos semantico
    private String tipo;
    private int ancho;
    private int tabla;
    private boolean declaracion;
    private Token tokenLlamador;
    private static Token nombreFuncion;
    public static boolean flagDeclaracionLocal = false;
    public static boolean flagDeclaracion = false;
    private static boolean flagReturn = true;
    public static File miDir = new File(".");

    public AnalizadorSintactico() throws DeclaracionIncompatibleException, IOException {
        
        //Inicializando atributos de clase
        this.analizador = new AnalizadorLexico();
        this.tS = new TablaSimbolos();
        this.tokenDevuelto = new Token(null, null);
        this.tokenLlamador = new Token(null, null);
        this.nombreFuncion = new Token(null, null);

        //Inicializando los atributos basicos
        this.parse = "";
        this.tabla = 0;

        //Nuevos archivos
        File archivoTablas = new File(miDir + "//impreso//tablas.txt");
        File archivoParse = new File(miDir + "//impreso//parse.txt");
        File archivoError = new File(miDir + "//impreso//error.txt");

        try {
          this.tablasWriter = new BufferedWriter(new FileWriter(archivoTablas));
          this.parseWriter = new BufferedWriter(new FileWriter(archivoParse));
          this.errorWriter = new BufferedWriter(new FileWriter(archivoError));
        } catch (IOException ex) {
          System.out.println("Ha habido un problema inicializando el fichero de tablas, probablemente no se cree correctamente.");
        }

    }

    public void empareja(Token valor) throws EmparejaException, ComentarioException, CadenaException, OpLogicoException, OtroSimboloException, FueraDeRangoException, IdException, IOException, DeclaracionIncompatibleException {
        if (valor != null && valor.equals(tokenDevuelto)) {
            tokenDevuelto = analizador.al(tS);
        } else {
            throw new EmparejaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token " + valor.toString() + " y se ha encontrado el token " + this.getTokenDevuelto().toString());
        }
    }
    
    public void procedP() throws FirstNoCoincideException, EmparejaException, CadenaException, OpLogicoException, ComentarioException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //P -> B ; P = { var if id prompt write }
        if (("PR".equals(this.getTokenDevuelto().getId()) && ("write".equals(this.getTokenDevuelto().getValor())
                || "prompt".equals(this.getTokenDevuelto().getValor())
                || "if".equals(this.getTokenDevuelto().getValor())
                || "var".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "1 ");
            
            procedB();
            empareja(new Token("PUNTCOM", null));
            procedP();
        }
        //P -> Fq P = { function }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "function".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "2 ");
            
            procedFq();
            procedP();
        }
        //P -> eof = { eof }
        else if ("EOF".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "3 ");
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < EOF , _ >, < PR , function >, < ID , id >, < PR , if >, < PR , prompt >,  < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
    }

    public void procedB() throws FirstNoCoincideException, EmparejaException, CadenaException, OpLogicoException, OtroSimboloException, ComentarioException, FueraDeRangoException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        
        //B -> var F2 id = { var }
        if ("PR".equals(this.getTokenDevuelto().getId()) && "var".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "4 ");
            flagDeclaracion = true;
            empareja(new Token("PR", "var"));
            flagDeclaracion = false;

            //En este caso asignamos el tipo segun lo que recibamos: entero, chars y bool CORREGIR
            this.tokenLlamador = this.tokenDevuelto;
      
            procedF2();    
            if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            }
            else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 1;
            }
            else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            tS.addTipo(this.tokenDevuelto, this.tipo);
            tS.addDireccion(tokenDevuelto, ancho);

            empareja(new Token("ID", null));
        } 
        //B -> if ( E ) C = { if }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "if".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "5 ");
            
            empareja(new Token("PR", "if"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            if ("CADENA".equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La condicion de la sentencia 'if' no puede ser una cadena.");
            } else if ("VOID".equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La condicion de la sentencia 'if' no puede llamar a una funcion que puede ser 'VOID'.");
            }
            empareja(new Token("PARENTCERRADO", null));
            procedC();
        } 
        //B -> S = { write prompt id }
        else if (("PR".equals(this.getTokenDevuelto().getId()) && ("write".equals(this.getTokenDevuelto().getValor())
                || "prompt".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "7 ");
            procedS();
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , for >, < ID , id >, < PR , if >, < PR , prompt >, < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
    }

    public void procedS() throws FirstNoCoincideException, EmparejaException, ComentarioException, FueraDeRangoException, OtroSimboloException, CadenaException, IdException, IOException, OpLogicoException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        
        //S -> id S1 = { id }
        if ("ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "8 ");
            //Semantico
            tokenLlamador = tokenDevuelto;  
            //Semantico
            empareja(new Token("ID", null));
            procedS1();
        } 
        //S -> prompt ( id ) = { prompt }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "prompt".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "9 ");
            empareja(new Token("PR", "prompt"));
            empareja(new Token("PARENTABIERTO", null));
            if (tS.getTipo(tokenDevuelto) == null) {
                //caso en que no este declarada la variable
                tipo = "ENTERA";
                ancho = 2;
                tS.addTipo(tokenDevuelto, tipo);
                tS.addDireccion(tokenDevuelto, ancho);
            } else if ("FUNC".equals(tS.getTipo(tokenDevuelto))) {
                throw new DeclaracionIncompatibleException("Error en linea " + AnalizadorLexico.linea + ". La variable o funcion '" + tokenDevuelto.getValor() + "' ha sido declarada previamente.");
            }
            empareja(new Token("ID", null));
            empareja(new Token("PARENTCERRADO", null));
        } 
        //S -> write ( E ) = { write }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "write".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "10 ");
            empareja(new Token("PR", "write"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            empareja(new Token("PARENTCERRADO", null));
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , prompt >) pero se ha detectado: " + this.getTokenDevuelto().toString());

        }
    }

    public void procedS1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, IdException, OtroSimboloException, CadenaException, FueraDeRangoException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        
        //S1 -> = E = { = }
        if ("ASIG".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "11 ");
            empareja(new Token("ASIG", null));
            procedE();
            if ("VOID".equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + AnalizadorLexico.linea + ". Esta intentando asignar una funcion que puede ser 'VOID' a una variable.");
            }
            //Comprobacion de tipos en la asignacion
            if (!tS.getTipo(tokenLlamador).equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + AnalizadorLexico.linea + ". Error de tipos en la asignacion.");
            }
            tS.addTipo(tokenLlamador, tipo);
            tS.addDireccion(tokenLlamador, ancho);
        } 
        //S1 -> ( L ) = { ( }
        else if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "12 ");
            //Semantico
            int contParam = 0;
            empareja(new Token("PARENTABIERTO", null));
            contParam = procedL();
            if (nombreFuncion != null && nombreFuncion.equals(tokenLlamador) && tS.getNParametrosGlobal(tokenLlamador) == contParam) {
                tS.buscaTSGlobal(tokenLlamador.getValor());
            } else if (tS.buscaTS(tokenLlamador.getValor())[0] == null || !"FUNC".equals(tS.getTipo(tokenLlamador))) {//tipo != FUNC porque si es variable o no declarada tiene que dar error.
                throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(AnalizadorLexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' no ha sido declarada.");
            } else if (tS.getNParametros(tokenLlamador) != contParam) {
                throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(AnalizadorLexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' debe ser llamada con " + tS.getNParametros(tokenLlamador) + " parametros y se ha llamado con " + contParam + ".");
            }
            empareja(new Token("PARENTCERRADO", null));
        }
        //S1 -> /= E = { / }
        else if("ASIGDIV".equals(this.getTokenDevuelto().getId())){
            this.setParse(this.getParse() + "13 ");
            empareja(new Token("ASIGDIV", null));
            procedE();
            if ("VOID".equals(tipo)) {
                    throw new TipoIncorrectoException("Error en linea: " + AnalizadorLexico.linea + ". Esta intentando asignar una funcion que puede ser 'VOID' a una variable.");
            }
            //Comprobacion de tipos enteros
            if (!tS.getTipo(tokenLlamador).equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + AnalizadorLexico.linea + ". Error de tipos en la asignacion con division.");
            }
            else if (!tipo.equals("ENTERA")) {
                                throw new TipoIncorrectoException("Error en linea: " + AnalizadorLexico.linea + ". Error de tipos en la asignacion con division. Deben de ser enteros.");
            }
            tS.addTipo(tokenLlamador, tipo);
            tS.addDireccion(tokenLlamador, ancho);
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < PARENTABIERTO , _ >, < ASIG , _ > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
    }

    public void procedFq() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IOException, IdException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        int contParam = 0;
        
        //Fq -> function F3 id ( A ) { Cfun } = { function }
        if ("PR".equals(this.getTokenDevuelto().getId()) && "function".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "14 ");
            flagDeclaracion = true;
            empareja(new Token("PR", "function"));
            flagDeclaracion = false;
 
            //AQUI CREAMOS LA TABLA DE SIMBOLOS LOCAL Y EL DESPLAZAMIENTO LOCAL
            procedF3(); // En este paso tenemos que almacenar el tipo de la funcion CORREGIR
            this.tipo = "FUNC";
            this.ancho = 4;
            this.tS.addTipo(tokenDevuelto, tipo);
            this.tS.addDireccion(tokenDevuelto, 4);
            tS.crearTSL();
            TablaSimbolos tLocal = (TablaSimbolos) tS.getTablaSimbolos().get(tS.getContadorRegistros() - 1)[0];
            if (tLocal != null) {
                //quitamos las palabras reservadas a la tabla de simbolos
                tLocal.vaciarTabla();
            }
            nombreFuncion = this.getTokenDevuelto();
            empareja(new Token("ID", null));
            flagDeclaracionLocal = true;
            empareja(new Token("PARENTABIERTO", null));
            contParam = procedA();
            this.tS.addParametros(contParam);
            flagDeclaracionLocal = false;
            empareja(new Token("PARENTCERRADO", null));
            
            empareja(new Token("LLAVEABIERTA", null));
            
            procedCfun();
            //Borramos la local.
            this.tS.addEtiqueta();
            //Justo antes de borrar volcamos la tabla de simbolos local correspondiente
            tablasWriter.write("TABLA DE LA FUNCION " + nombreFuncion.getValor() + " #"+tS.indice++);
            this.tS.volcarTabla(tablasWriter);
            this.tS.borraTS();
            empareja(new Token("LLAVECERRADA", null));
            if (flagReturn) {
                tipo = "VOID";
            }
            tS.addDevuelve(tipo);
            nombreFuncion = null;

        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token < PR , function > pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
    }

    public int procedA() throws EmparejaException, CadenaException, OpLogicoException, ComentarioException, FueraDeRangoException, OtroSimboloException, IdException, IOException, DeclaracionIncompatibleException, FirstNoCoincideException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        int contParam = 0;
        
        //A -> F2 id D = { int chars bool }
        if (("PR".equals(this.getTokenDevuelto().getId()) && ("int".equals(this.getTokenDevuelto().getValor())
                || "chars".equals(this.getTokenDevuelto().getValor()))
                || "bool".equals(this.getTokenDevuelto().getValor()))) {
          this.setParse(this.getParse() + "48 ");
          procedF2();
          if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            }
            else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 1;
            }
            else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            tS.addTipo(this.tokenDevuelto, this.tipo);
            tS.addDireccion(tokenDevuelto, ancho);
          empareja(new Token("ID", null));
          procedD();
        }
        //A -> lambda
        else {
            this.setParse(this.getParse() + "19 ");
        }
        return contParam;
    }

    public int procedD() throws EmparejaException, CadenaException, OtroSimboloException, OpLogicoException, ComentarioException, FueraDeRangoException, IdException, IOException, DeclaracionIncompatibleException, FirstNoCoincideException, FuncionNoDeclaradaException, VariableNoDeclaradaException, ConcatenacionNoImplementadaException, TiposDiferentesException, TipoIncorrectoException {
        int contParam = 0;
        
        //D -> , F2 id D = { , }
        if ("COMA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "20 ");
            empareja(new Token("COMA", null));
            this.tokenLlamador = this.tokenDevuelto;
      
            //Almacenamos el tipo del parametro
            procedF2();    
            if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            }
            else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 4;
            }
            else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            tS.addTipo(this.tokenDevuelto, this.tipo);
            tS.addDireccion(tokenDevuelto, ancho);

            empareja(new Token("ID", null));
            contParam = procedD();
            contParam++;
        }
        //D -> lambda
        else {
            this.setParse(this.getParse() + "21 ");
        }
        return contParam;
    }

    public void procedC() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

      //C -> { S } C1 = { { }
    if ("LLAVEABIERTA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "25 ");
      
      empareja(new Token("LLAVEABIERTA", null));
      
      procedS();
      empareja(new Token("LLAVECERRADA", null));
      
      procedC1();
    }
    //C -> S = { write prompt id }
    else if (("PR".equals(this.getTokenDevuelto().getId()) && ("write".equals(this.getTokenDevuelto().getValor())
                || "prompt".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "26 ");

      procedS();
    }
    else {
       throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < PR , write >, < ID , id >, < LLAVEABIERTA , - >, < PR , prompt > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  public void procedC1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

      //C1 -> else { S } = { else }
    if ("PR".equals(this.getTokenDevuelto().getId()) && "else".equals(this.getTokenDevuelto().getValor())) {
      this.setParse(this.getParse() + "27 ");

      empareja(new Token("PR", "else"));
      empareja(new Token("LLAVEABIERTA", null));
      procedS();
      empareja(new Token("LLAVECERRADA", null));
    }
    //C1 -> lambda
    else {
      this.setParse(this.getParse() + "28 ");
    }

  }

  public void procedC2() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException, DevuelveCadenaException {

    //C2 -> { Sfun } C3 = { { } 
    if ("LLAVEABIERTA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "29 ");

      empareja(new Token("LLAVEABIERTA", null));
      procedSfun();
      empareja(new Token("LLAVECERRADA", null));
      procedC3();
    }
    //C2 -> Sfun = { write prompt id } 
    else if (("PR".equals(this.getTokenDevuelto().getId()) && ("write".equals(this.getTokenDevuelto().getValor())
                || "prompt".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "30 ");

      procedSfun();
    }
    else {
      throw new FirstNoCoincideException("ProcedG2:Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token ( < PR , write >, < ID , id >, < LLAVEABIERTA , - >, < PR , prompt > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  public void procedC3() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException, DevuelveCadenaException {

    //G3 -> else { Sfun }
    if ("PR".equals(this.getTokenDevuelto().getId()) && "else".equals(this.getTokenDevuelto().getValor())) {
      this.setParse(this.getParse() + "31 ");

      empareja(new Token("PR", "else"));
      empareja(new Token("LLAVEABIERTA", null));
      procedSfun();
      empareja(new Token("LLAVECERRADA", null));
    }
    //G3 -> lambda
    else {
       this.setParse(this.getParse() + "32 ");
    }

  }

    private int procedL() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, IdException, CadenaException, FueraDeRangoException, OtroSimboloException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        int contParam = 0;
        
        //L -> E Q = { id cadena bool num }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "BOOL".equals(this.getTokenDevuelto().getId())
                || "NUM".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "25 ");
            contParam++;
            procedE();
            contParam += procedQ();
        }
        //L -> lambda
        else {
            this.setParse(this.getParse() + "26 ");
        }
        return contParam;
    }

    public int procedQ() throws FirstNoCoincideException, EmparejaException, OpLogicoException, CadenaException, FueraDeRangoException, IdException, OtroSimboloException, ComentarioException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        int contParam = 0;

        //Q -> , E Q = { , }
        if ("COMA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "27 ");
            contParam++;
            empareja(new Token("COMA", null));
            procedE();
            contParam += procedQ();
        } 
        //Q -> lambda
        else {
            this.setParse(this.getParse() + "28 ");
        }
        return contParam;
    }

    public void procedX() throws FirstNoCoincideException, EmparejaException, IOException, CadenaException, ComentarioException, OpLogicoException, FueraDeRangoException, IdException, OtroSimboloException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //X -> E = { id cadena ent bool }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "BOOL".equals(this.getTokenDevuelto().getId())
                || "NUM".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "29 ");
            procedE();
            if ("CADENA".equals(tipo)) {
                throw new DevuelveCadenaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Se ha intentado devolver una cadena y solo se permite devolver un entero o vacio.");
            }
        } 
        //X -> lambda
        else {
            this.setParse(this.getParse() + "30 ");
            tipo = "VOID";
        }

    }

    private void procedE() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, IdException, OtroSimboloException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //E -> T R1 = { id cadena ent bool }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "BOOL".equals(this.getTokenDevuelto().getId())
                || "NUM".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "31 ");
            procedT();
            procedR1();
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
    }

    public void procedR1() throws FirstNoCoincideException, EmparejaException, CadenaException, ComentarioException, OpLogicoException, IdException, OtroSimboloException, FueraDeRangoException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        String tipoR1 = "";
        
        //R1 -> && T R1 = { && }
        if ("CONJUNCION".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "32 ");
            if (tipo.equals("CADENA")) {
                tipoR1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoR1 = "VOID";
            } else {
                tipoR1 = "ENTERA";
            }
            empareja(new Token("CONJUNCION", null));
            procedT();
            if ("VOID".equals(tipo) || "VOID".equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Los tipos de los operandos no coinciden.");
            } else if (tipo.equals(tipoR1) && tipo.equals("CADENA")) {
                throw new ConcatenacionNoImplementadaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La conjuncion de cadenas no se puede realizar.");
            }
            procedR1();
        } 
        //R1-> lambda
        else {
            this.setParse(this.getParse() + "34 ");
        }
    }

    public void procedT() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, OtroSimboloException, FueraDeRangoException, CadenaException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        String tipoT = "";
        
        //T -> H T1 = { id cadena ent bool }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "BOOL".equals(this.getTokenDevuelto().getId())
                || "NUM".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "35 ");
            procedH();
            procedT1();
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
    }

    public void procedT1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, FueraDeRangoException, OtroSimboloException, CadenaException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        String tipoT1 = "";
        
        //T1 -> < H T1 = { < }
        if ("MENORQUE".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "36 ");
            if (tipo.equals("CADENA")) {
                tipoT1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoT1 = "VOID";
            } else {
                tipoT1 = "ENTERA";
            }
            empareja(new Token("MENORQUE", null));
            procedH();
            if ("VOID".equals(tipo) || "VOID".equals(tipoT1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoT1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Los tipos de los operandos no coinciden.");
            } else if (tipo.equals(tipoT1) && "CADENA".equals(tipo)) {
                tipo = "ENTERA";
                ancho = 2;
            }
            procedT1();
        } 
        //T1 -> lambda
        else {
            this.setParse(this.getParse() + "38 ");
        }
    }

    public void procedH() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, OtroSimboloException, FueraDeRangoException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //H -> F H1 = { id cadena ent bool } 
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "BOOL".equals(this.getTokenDevuelto().getId())
                || "NUM".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "39 ");
            procedF();
            procedH1();
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
    }

    public void procedH1() throws FirstNoCoincideException, EmparejaException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, ComentarioException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        String tipoH1 = "";
        
        //H1 -> + F H1 = { + }
        if ("SUMA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "40 ");
            if (tipo.equals("CADENA")) {
                tipoH1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoH1 = "VOID";
            } else {
                tipoH1 = "ENTERA";
            }
            empareja(new Token("SUMA", null));
            procedF();
            if ("VOID".equals(tipo) || "VOID".equals(tipoH1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoH1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Los tipos de los sumandos no coinciden.");
            } else if (tipo.equals(tipoH1) && tipo.equals("CADENA")) {
                throw new ConcatenacionNoImplementadaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". No esta implementada la concatenacion de cadenas.");
            }
            procedH1();
        } 
        //H1 -> lambda
        else {
            this.setParse(this.getParse() + "42 ");
        }
    }

    public void procedF() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        Token tokenAuxiliar = tokenLlamador;
        
        //F -> id F1 = { id }
        if ("ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "43 ");
            //Semantico
            tokenLlamador = tokenDevuelto;
            if (tS.getTipo(tokenDevuelto) == null) {
                //Si la variable no esta declarada: chars "", int a cero y bool a false
                tipo = "ENTERA";
                ancho = 2;
                tS.addTipo(tokenDevuelto, tipo);
                tS.addDireccion(tokenDevuelto, ancho);
            } else if ("CADENA".equals(tS.getTipo(tokenDevuelto))) {
                tipo = "CADENA";
                ancho = tokenDevuelto.getValor().length();
            } else if ("ENTERA".equals(tS.getTipo(tokenDevuelto))) {
                tipo = "ENTERA";
                ancho = 2;
            }
            empareja(new Token("ID", null));
            procedF1();
        } 
        //F -> cadena = { cadena } 
        else if ("CADENA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "44 ");
            tipo = "CADENA";
            ancho = this.tokenDevuelto.getValor().length();
            empareja(new Token("CADENA", null));
        }
        //F -> ent = { ent }
        else if ("NUM".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "45 ");
            ancho = 2;
            tipo = "ENTERA";
            empareja(new Token("NUM", null));
        } 
        //F -> bool = { bool }
        else if ("BOOL".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "46 ");
            ancho = 1;
            tipo = "BOOL";
            empareja(new Token("BOOL", null));
        } 
        //F -> ( E ) = { ( }
        else if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "47 ");
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            if ("CADENA".equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La condicion de la sentencia condicional ternaria no puede ser una cadena.");
            } else if ("VOID".equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La condicion de la sentencia condicional ternaria no puede ser una funcion que puede ser 'VOID'.");
            }
            empareja(new Token("PARENTCERRADO", null));
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
        tokenLlamador = tokenAuxiliar;
    }

    private void procedF1() throws FirstNoCoincideException, EmparejaException, ComentarioException, CadenaException, OpLogicoException, OtroSimboloException, FueraDeRangoException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //F1 -> ( L ) = { ( }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
            if (tS.getTipo(tokenLlamador) == null) {
                throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(AnalizadorLexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' no ha sido declarada.");
            }
            this.setParse(this.getParse() + "48 ");
            //Semantico
            int contParam = 0;
            if (tS.buscaTS(tokenLlamador.getValor())[0] == null && "FUNC".equals(tS.getTipo(tokenLlamador))) {
                throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(AnalizadorLexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' no ha sido declarada.");
            }
            empareja(new Token("PARENTABIERTO", null));
            contParam = procedL();
            if (tS.getNParametros(tokenLlamador) != contParam) {
                throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(AnalizadorLexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' debe ser llamada con " + tS.getNParametros(tokenLlamador) + " parametros y se ha llamado con " + contParam + ".");
            }
            empareja(new Token("PARENTCERRADO", null));
            if (tS.getDevuelve(tokenLlamador) != null) {
                tipo = tS.getDevuelve(tokenLlamador);
            }
        } 
        //F1 -> lambda
        else {
            if ("FUNC".equals(tS.getTipo(tokenLlamador))) {
                throw new DeclaracionIncompatibleException("Error en linea " + AnalizadorLexico.linea + ". La variable o funcion '" + tokenLlamador.getValor() + "' ha sido declarada previamente.");
            }
            this.setParse(this.getParse() + "49 ");
        }
    }
    
    public void procedF2() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //F2 -> int = { int }
        if ("PR".equals(this.getTokenDevuelto().getId()) && "int".equals(this.getTokenDevuelto().getValor())) {
          this.setParse(this.getParse() + "48 ");

          empareja(new Token("PR", "int"));
        }
        //F2 -> chars = { chars }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "chars".equals(this.getTokenDevuelto().getValor())) {
          this.setParse(this.getParse() + "49 ");

          empareja(new Token("PR", "chars"));
        }
        //F2 -> bool = { bool }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "bool".equals(this.getTokenDevuelto().getValor())) {
          this.setParse(this.getParse() + "50 ");

          empareja(new Token("PR", "bool"));
        }
        else {
          throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < PR , int >, < PR , chars >, < PR , bool > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }

  }

    public void procedF3() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //F3 -> int = { int }
        if ("PR".equals(this.getTokenDevuelto().getId()) && "int".equals(this.getTokenDevuelto().getValor())) {
          this.setParse(this.getParse() + "48 ");

          empareja(new Token("PR", "int"));
        }
        //F3 -> chars = { chars }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "chars".equals(this.getTokenDevuelto().getValor())) {
          this.setParse(this.getParse() + "49 ");

         empareja(new Token("PR", "chars"));        
        }
        //F3 -> bool = { bool } 
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "bool".equals(this.getTokenDevuelto().getValor())) {
          this.setParse(this.getParse() + "50 ");

          empareja(new Token("PR", "bool"));
        }
        //F3 -> void = { void }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "void".equals(this.getTokenDevuelto().getValor())) {
          this.setParse(this.getParse() + "50 ");

          empareja(new Token("PR", "void"));
        }
        else {
          throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < PR , int >, < PR , chars >, < PR , bool >, < PR, void > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }

      }
    private boolean procedBfun() throws ComentarioException, OpLogicoException, OtroSimboloException, FueraDeRangoException, CadenaException, IdException, EmparejaException, IOException, FirstNoCoincideException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //Bfun - > var F2 id = { var }
        if ("PR".equals(this.getTokenDevuelto().getId()) && "var".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "50 ");
            flagDeclaracion = true;
            empareja(new Token("PR", "var"));
            flagDeclaracion = false;

            //En este caso asignamos el tipo segun lo que recibamos: entero, chars y bool CORREGIR
            this.tokenLlamador = this.tokenDevuelto;
      
            procedF2();  
            if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            }
            else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 1;
            }
            else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            tS.addTipo(this.tokenDevuelto, this.tipo);
            tS.addDireccion(tokenDevuelto, ancho);

            empareja(new Token("ID", null));
        } 
        //Bfun -> if ( E ) C2 = { if }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "if".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "51 ");
            empareja(new Token("PR", "if"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            if ("CADENA".equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La condicion de la sentencia 'if' no puede ser una cadena.");
            } else if ("VOID".equals(tipo)) {
                throw new TipoIncorrectoException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La condicion de la sentencia 'if' no puede llamar a una funcion que puede ser 'VOID'.");
            }
            empareja(new Token("PARENTCERRADO", null));
            procedC2();
            flagReturn = true;
        } 
        //Bfun -> Sfun = { write prompt id return }
        else if (("PR".equals(this.getTokenDevuelto().getId()) && ("write".equals(this.getTokenDevuelto().getValor())
                || "prompt".equals(this.getTokenDevuelto().getValor()) || "return".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "53 ");
            flagReturn = procedSfun();
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , for >, < PR , return >, < ID , id >, < PR , if >, < PR , prompt >,  < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
        return flagReturn;
    }

    private boolean procedSfun() throws EmparejaException, OpLogicoException, IdException, OtroSimboloException, ComentarioException, FueraDeRangoException, FirstNoCoincideException, CadenaException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //Sfun -> id S1 = { id }
        if ("ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "54 ");
            //Semantico
            tokenLlamador = tokenDevuelto;
            //Semantico
            empareja(new Token("ID", null));
            procedS1();
        } 
        //Sfun -> prompt ( id ) = { prompt }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "prompt".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "55 ");
            empareja(new Token("PR", "prompt"));
            empareja(new Token("PARENTABIERTO", null));
            if (tS.getTipo(tokenDevuelto) == null) {
                //No esta declarada la variable
                tipo = "ENTERA";
                ancho = 2;
                tS.addTipo(tokenDevuelto, tipo);
                tS.addDireccion(tokenDevuelto, ancho);
            } else if ("FUNC".equals(tS.getTipo(tokenDevuelto))) {
                throw new DeclaracionIncompatibleException("Error en linea " + AnalizadorLexico.linea + ". La variable o funcion '" + tokenDevuelto.getValor() + "' ha sido declarada previamente.");
            }
            empareja(new Token("ID", null));
            empareja(new Token("PARENTCERRADO", null));
        } 
        //Sfun -> write ( E ) = { write } 
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "write".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "56 ");
            empareja(new Token("PR", "write"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            empareja(new Token("PARENTCERRADO", null));
        } 
        //Sfun -> return X = { return }
        else if ("PR".equals(this.getTokenDevuelto().getId()) && "return".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "57 ");
            empareja(new Token("PR", "return"));
            flagReturn = false;
            procedX();
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , prompt>,  < PR , return >) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
        return flagReturn;
    }

    private boolean procedCfun() throws CadenaException, FirstNoCoincideException, ComentarioException, OpLogicoException, OtroSimboloException, EmparejaException, IdException, IOException, FueraDeRangoException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //Cfun -> Bfun ; C1fun = { id prompt write if var return }
        if (("PR".equals(this.getTokenDevuelto().getId()) && ("write".equals(this.getTokenDevuelto().getValor())
                || "prompt".equals(this.getTokenDevuelto().getValor()) || "return".equals(this.getTokenDevuelto().getValor())
                || "if".equals(this.getTokenDevuelto().getValor())
                || "var".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "58 ");
            flagReturn = procedBfun();
            empareja(new Token("PUNTCOM", null));
            procedC1fun(flagReturn);
        } else {
            throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , for >, < ID , id >, < PR , if >, < PR , prompt>, < PR , return >,  <PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
        }
        return flagReturn;
    }

    private void procedC1fun(boolean condReturn) throws FirstNoCoincideException, CadenaException, OpLogicoException, OtroSimboloException, EmparejaException, IdException, ComentarioException, FueraDeRangoException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

        //C1fun -> Cfun = { id prompt write return id var }
        if (("PR".equals(this.getTokenDevuelto().getId()) && ("write".equals(this.getTokenDevuelto().getValor())
                || "prompt".equals(this.getTokenDevuelto().getValor()) || "return".equals(this.getTokenDevuelto().getValor())
                || "if".equals(this.getTokenDevuelto().getValor())
                || "var".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "59 ");
            if (!condReturn) {
                throw new CodigoMuertoException("Error en la linea: " + Integer.toString(AnalizadorLexico.linea) + ". Existe codigo muerto.");
            }
            procedCfun();

        } 
        //C1fun -> lambda
        else {
            this.setParse(this.getParse() + "60 ");
        }
    }

    public AnalizadorLexico getAnalizador() {
        return analizador;
    }

    public void setAnalizador(AnalizadorLexico analizador) {
        this.analizador = analizador;
    }

    public TablaSimbolos gettS() {
        return tS;
    }

    public void settS(TablaSimbolos tS) {
        this.tS = tS;
    }

    public Token getTokenDevuelto() {
        return tokenDevuelto;
    }

    public void setTokenDevuelto(Token tokenDevuelto) {
        this.tokenDevuelto = tokenDevuelto;
    }

    public String getParse() {
        return parse;
    }

    public void setParse(String parse) {
        this.parse = parse;
    }

    public BufferedWriter getTablasWriter() {
        return tablasWriter;
    }

    public void setTablasWriter(BufferedWriter tablasWriter) {
        this.tablasWriter = tablasWriter;
    }

    public BufferedWriter getParseWriter() {
        return parseWriter;
    }

    public void setParseWriter(BufferedWriter parseWriter) {
        this.parseWriter = parseWriter;
    }

    public BufferedWriter getErrorWriter() {
        return errorWriter;
    }

    public void setErrorWriter(BufferedWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    public static void main(String[] args) {
        AnalizadorSintactico as = null;
        
        try {
            File ficheroAAnalizar=null;
            if (args != null) {
                ficheroAAnalizar = new File(miDir.getCanonicalPath() + "//pruebas//" + args[0]);
            }
            as = new AnalizadorSintactico();

            if (args.length != 1) {
                throw new FileNotFoundException("Se han pasado " + args.length + " ficheros para analizar y solo debe pasarse un fichero.");
            } else if (!ficheroAAnalizar.exists()) {
                throw new FileNotFoundException("El fichero a analizar " + args[0].toString() + " no existe.");
            }
            as.getAnalizador().leerFichero(ficheroAAnalizar);
            as.setTokenDevuelto(as.getAnalizador().al(as.gettS()));
            while (!"EOF".equals(as.getTokenDevuelto().getId())) {
                as.procedP();
            }

            as.gettS().volcarTabla(as.getTablasWriter());
            as.getParseWriter().write("DescendenteParser " + as.getParse());
            as.getAnalizador().getBw().close();
            as.getTablasWriter().close();
            as.getParseWriter().close();

            if (as.getAnalizador().getFr() != null) {
                as.getAnalizador().getFr().close();
            }

        } catch (FirstNoCoincideException | FileNotFoundException | EmparejaException | ComentarioException | CadenaException | OpLogicoException | OtroSimboloException | FueraDeRangoException | IdException | FuncionNoDeclaradaException | VariableNoDeclaradaException | DevuelveCadenaException | TiposDiferentesException | CodigoMuertoException | DeclaracionIncompatibleException | ConcatenacionNoImplementadaException | TipoIncorrectoException ex) {
            System.out.println(ex.getMessage());
            try {
                if (as != null) {
                    as.getErrorWriter().write(ex.getMessage());
                }
                as.getErrorWriter().close();
            } catch (IOException exc) {
                System.out.println("Error escribiendo en el fichero de error.");
            }

        }
        //Error en la escritura/tratamiento de los ficheros generados
        catch (IOException ex) {
            System.out.println("Error con la escritura o tratamiento de alguno de los ficheros generados.");
            try {
                if (as != null) {
                    as.getErrorWriter().write("Error con la escritura o tratamiento de alguno de los ficheros generados.");
                }
                as.getErrorWriter().close();
            } catch (IOException exc) {
                System.out.println("Error escribiendo en el fichero de error.");
            }
        }
        //Excepcion no controlada
        catch (Exception ex) {
            System.out.println("Excepcion no controlada.");
            try {
                if (as != null) {
                    as.getErrorWriter().write("Se ha producido una excepcion no controlada.");
                }
                as.getErrorWriter().close();
            } catch (IOException exc) {
                System.out.println("Error escribiendo en el fichero de error.");
            }
        }
    }
}
