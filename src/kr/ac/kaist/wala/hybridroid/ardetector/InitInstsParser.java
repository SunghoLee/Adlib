package kr.ac.kaist.wala.hybridroid.ardetector;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leesh on 06/04/2017.
 */
public class InitInstsParser {

    public static InitInst[] parse(String s) throws ParseException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(s));
        String content = "";
        String buffer = "";

        while((buffer = br.readLine()) != null){
            content += buffer + "\n";
        }

        JSONParser parser = new JSONParser();
        JSONArray arr = (JSONArray)parser.parse(content);
        List<InitInst> insts = new ArrayList<>();
        for(int i=0; i<arr.size(); i++){
            JSONObject o = (JSONObject) arr.get(i);
            String klass = (String) o.get("class");
            String method = (String) o.get("method");

            klass = (klass.startsWith("L"))? klass : "L"+klass;
            klass = klass.replace(".","/");
            insts.add(new InitInst(TypeReference.findOrCreate(ClassLoaderReference.Application, klass), Selector.make(method)));
        }

        return insts.toArray(new InitInst[0]);
    }

    public static class InitInst {
        private TypeReference klass;
        private Selector method;

        private InitInst(TypeReference klass, Selector method){
            this.klass = klass;
            this.method = method;
        }

        public TypeReference getReceiverType(){
            return this.klass;
        }

        public Selector getMethodSelector(){
            return this.method;
        }
    }
}
