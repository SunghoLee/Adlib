package kr.ac.kaist.wala.hybridroid.util.debug;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.strings.Atom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by leesh on 22/08/2017.
 */
public class PointerAnalysisCommandlineDebugger {
    private PointerAnalysis<InstanceKey> pa;
    private CallGraph cg;

    public PointerAnalysisCommandlineDebugger(CallGraph cg, PointerAnalysis<InstanceKey> pa){
        this.pa = pa;
        this.cg = cg;
    }

    public void debug(){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            CommandLineParser parser = new CommandLineParser();

            L1:
            while(true){
                System.out.print("Type your command: ");
                String s = br.readLine();

                Command command = parser.parse(s);

                try {
                    switch (command.getCommandType()) {
                        case Command.END:
                            break L1;
                        case Command.ABNORMAL:
                            System.out.println("Wrong command: " + s);
                            break;
                        case Command.NORMAL: {
                            CGNode target = null;

                            for (CGNode n : cg) {
                                if (n.toString().contains(command.getNodeName())) {
                                    target = n;
                                    break;
                                }
                            }

                            if (target == null) {
                                System.out.println("Cannot find the node: " + command.getNodeName());
                                break;
                            }

                            PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(target, command.getValueNumber());

                            System.out.println("====");
                            System.out.println("Node: " + target);
                            System.out.println("V: " + command.getValueNumber());
                            System.out.println("\tPK: " + pk);

                            if (command.getFieldName() == null) {
                                for (InstanceKey ik : pa.getPointsToSet(pk))
                                    System.out.println("\t\tIK: " + ik);
                            } else {
                                for (InstanceKey ik : pa.getPointsToSet(pk)) {
                                    System.out.println("\t\tIK: " + ik);
                                    IField f = ik.getConcreteType().getField(Atom.findOrCreateAsciiAtom(command.getFieldName()));
                                    System.out.println("\t\t\t#F: " + f);
                                    PointerKey fPK = pa.getHeapModel().getPointerKeyForInstanceField(ik, f);
                                    for (InstanceKey fik : pa.getPointsToSet(fPK)) {
                                        System.out.println("\t\t\t\t#FIK: " + fik);
                                    }
                                }
                            }
                            System.out.println("====");
                        }
                        break;
                    }
                }catch (Exception e){
                    System.out.println("Wrong command: " + s);
                }
            }


//            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class CommandLineParser {

        public Command parse(String s){
            if(isEndCommand(s))
                return Command.makeEndCommand();

            Triple t = parseCommand(s);

            if(t == null)
                return Command.makeAbnormalCommand();

            String nodeName = (String)t.x;
            int valueNumber = (int)t.y;
            String fieldName = (String)t.z;

            System.out.println("N: " + nodeName);
            System.out.println("V: " + valueNumber);

            if(nodeName == null || nodeName.equals("") || valueNumber < 1)
                return Command.makeAbnormalCommand();

            if(fieldName == null || fieldName.equals(""))
                return new Command(nodeName, valueNumber);

            return new Command(nodeName, valueNumber, fieldName);
        }

        private Triple parseCommand(String s){
            char[] commands = s.toCharArray();
            int index = 0;
            int state = 0;

            s += " ";

            String buff = "";
            String nodeName = "";
            String valueNumuberS = "";
            String fieldName = "";

            int valueNumuber = -1;

            try {
                while (index != commands.length) {
                    switch (state) {
                        case 0:
                            index = removeWhiteSpace(commands, index);
                            state = 1;
                            break;
                        case 1:
                            if (commands[index] == 'N' && commands[index + 1] == '{') {
                                state = 2;
                            }else if (commands[index] == 'V' && commands[index + 1] == '{') {
                                state = 3;
                            }else if (commands[index] == 'F' && commands[index + 1] == '{') {
                                state = 4;
                            }else {
                                throw new Exception("parsing error: '" + commands[index] + "' at " + index);
                            }

                            index += 2;
                            break;
                        case 2:
                            if (commands[index] != '}') {
                                nodeName += commands[index];
                            }else{
                                state = 5;
                            }
                            index ++;
                            break;
                        case 3:
                            if (commands[index] != '}') {
                                valueNumuberS += commands[index];
                            }else{
                                state = 5;
                            }
                            index ++;
                            break;
                        case 4:
                            if (commands[index] != '}') {
                                fieldName += commands[index];
                            }else{
                                state = 5;
                            }
                            index ++;
                            break;
                        case 5:
                            index = removeWhiteSpace(commands, index);
                            state = 1;
                            break;
                    }
                }

                valueNumuber = Integer.parseInt(valueNumuberS);

            } catch (Exception e){
                return null;
            }

            return new Triple(nodeName, valueNumuber, fieldName);
        }

        class Triple {
            Object x;
            Object y;
            Object z;

            public Triple(Object x, Object y, Object z){
                this.x = x;
                this.y = y;
                this.z = z;
            }
        }

        private int removeWhiteSpace(char[] commands, int startIndex){
            int i=startIndex;

            for(; (commands[i] == ' ' || commands[i] == '\t') && i < commands.length; i++);

            return i;
        }

        private boolean isEndCommand(String s){
            return s.equals("end");
        }
    }
}

class Command {
    public static final int END = -1;
    public static final int NORMAL = 1;
    public static final int ABNORMAL = 9;

    private final String nodeName;
    private final int valueNumber;
    private final String fieldName;
    private final int type;

    public static Command makeEndCommand(){
        return new Command(null, -1, null, Command.END);
    }

    public static Command makeAbnormalCommand(){
        return new Command(null, -1, null, Command.ABNORMAL);
    }

    public Command(String nodeName, int valueNumber){
        this(nodeName, valueNumber, null, Command.NORMAL);
    }

    public Command(String nodeName, int valueNumber, String fieldName){
        this(nodeName, valueNumber, fieldName, Command.NORMAL);
    }

    private Command(String nodeName, int valueNumber, String fieldName, int type){
        this.nodeName = nodeName;
        this.valueNumber = valueNumber;
        this.fieldName = fieldName;
        this.type = type;
    }

    public String getNodeName(){
        return this.nodeName;
    }

    public int getValueNumber(){
        return this.valueNumber;
    }

    public String getFieldName(){
        return this.fieldName;
    }

    public int getCommandType(){
        return this.type;
    }
}