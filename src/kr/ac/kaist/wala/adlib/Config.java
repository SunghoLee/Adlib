package kr.ac.kaist.wala.adlib;

public class Config{
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

    public static boolean COMPUTE_ALIAS = true;
    public static CgOption CALLGRAPH_OPTION = CgOption.OBJ_SENSITIVITY;
    public static int SENSITIVITY_DEPTH = 3;

    public static String describe(){
        String res = "================================\n" +
                     " Call graph option: " + CALLGRAPH_OPTION.getName() + "\n" +
                     " Sensitivity Depth: " + SENSITIVITY_DEPTH + "\n" +
                     " Alias Option: " + COMPUTE_ALIAS + "\n" +
                     "================================";
        return res;
    }
}
