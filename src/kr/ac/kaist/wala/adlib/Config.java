package kr.ac.kaist.wala.adlib;

import org.apache.commons.cli.*;

public class Config{
  // Configuration Options for Adlib
  private static boolean COMPUTE_ALIAS;
  private static CgOption CALLGRAPH_OPTION;
  private static int SENSITIVITY_DEPTH;
  private static String WALA_PROP;
  private static String SDK;
  private static String INIT_INST;
  
  // Arguments Prefixes
  private static String PROP_PREFIX = "p";
  private static String CS_PREFIX = "s";
  private static String ALIAS_PREFIX = "alias";
  private static String SDK_PREFIX = "sdk";
  private static String INIT_INST_PREFIX = "init";

  // Commandline Options
  private static Options options = new Options();

  static{ 
    Option propOpt = Option.builder(PROP_PREFIX)
      .hasArg(true)
      .desc("path of the wala.properties file")
      .build();
    Option csOpt = Option.builder(CS_PREFIX)
      .hasArg(true)
      .desc("context-sensitivity (ps, ci, cs1 - 3, os1 - 3)")
      .build();
    Option sdkOpt = Option.builder(SDK_PREFIX)
      .hasArg(true)
      .desc("path of the AdSDK")
      .build();
    Option iinstOpt = Option.builder(INIT_INST_PREFIX)
      .hasArg(true)
      .desc("path of the init inst")
      .build();
    Option aliasOpt = Option.builder(ALIAS_PREFIX)
      .hasArg(false)
      .desc("enable alias handling in IFDS")
      .build();

    options.addOption(propOpt);
    options.addOption(csOpt);
    options.addOption(sdkOpt);
    options.addOption(iinstOpt);
    options.addOption(aliasOpt);
  }

  public enum CgOption{
    PATH_SEPERATION("Path Seperation"),
    INSENSITIVITY("Context Insensitivity"),
    CALLSITE_SENSITIVITY("Callsite Sensitivity"),
    OBJ_SENSITIVITY("Object Sensitivity");

    private String name;

    private CgOption(String n){
      this.name = n;
    }

    public String getName(){
      return name;
    }
  };

  public static boolean aliasMode(){ return COMPUTE_ALIAS; }
  public static CgOption sensitivity(){ return CALLGRAPH_OPTION; }
  public static int sensitivityDepth(){ return SENSITIVITY_DEPTH; }
  public static String getProperty(){ return WALA_PROP; }
  public static String getSDK(){ return SDK; }
  public static String getInitInst(){ return INIT_INST; }

  public static void parse(String[] args){
    DefaultParser parser = new DefaultParser();

    try{
      CommandLine cl = parser.parse(options, args, true);

      if(!cl.hasOption(PROP_PREFIX) || !cl.hasOption(CS_PREFIX) || !cl.hasOption(SDK_PREFIX) || !cl.hasOption(INIT_INST_PREFIX))
        throw new ParseException("Missing argument");

      WALA_PROP = cl.getOptionValue(PROP_PREFIX);
      SDK = cl.getOptionValue(SDK_PREFIX);
      INIT_INST = cl.getOptionValue(INIT_INST_PREFIX);
      COMPUTE_ALIAS = cl.hasOption(ALIAS_PREFIX);

      switch(cl.getOptionValue(CS_PREFIX)){
        case "ps":
          CALLGRAPH_OPTION = CgOption.PATH_SEPERATION;
          SENSITIVITY_DEPTH = 0;
          break;
        case "ci":
          CALLGRAPH_OPTION = CgOption.INSENSITIVITY;
          SENSITIVITY_DEPTH = 0;
          break;
        case "cs1":
          CALLGRAPH_OPTION = CgOption.CALLSITE_SENSITIVITY;
          SENSITIVITY_DEPTH = 1;
          break;
        case "cs2":
          CALLGRAPH_OPTION = CgOption.CALLSITE_SENSITIVITY;
          SENSITIVITY_DEPTH = 2;
          break;
        case "cs3":
          CALLGRAPH_OPTION = CgOption.CALLSITE_SENSITIVITY;
          SENSITIVITY_DEPTH = 3;
          break;
        case "os1":
          CALLGRAPH_OPTION = CgOption.OBJ_SENSITIVITY;
          SENSITIVITY_DEPTH = 1;
          break;
        case "os2":
          CALLGRAPH_OPTION = CgOption.OBJ_SENSITIVITY;
          SENSITIVITY_DEPTH = 2;
          break;
        case "os3":
          CALLGRAPH_OPTION = CgOption.OBJ_SENSITIVITY;
          SENSITIVITY_DEPTH = 3;
          break;
        default:
          throw new ParseException("Invalid sensitivity");
      }
    }catch(ParseException pe){
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Adlib", options);
      System.exit(-1);
    }
  }

  public static String describe(){
    String res = "================================\n" +
                 " Call graph option: " + CALLGRAPH_OPTION.getName() + "\n" +
                 " Sensitivity Depth: " + SENSITIVITY_DEPTH + "\n" +
                 " Alias Option: " + COMPUTE_ALIAS + "\n" +
                 "================================";
    return res;
  }
}
