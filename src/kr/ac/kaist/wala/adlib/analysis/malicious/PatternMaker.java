package kr.ac.kaist.wala.adlib.analysis.malicious;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by leesh on 07/04/2018.
 */
public class PatternMaker {
    public MaliciousPatternChecker.MaliciousPattern[] make(String name, MaliciousPatternChecker.MaliciousPoint[][] points){
        List<Set<MaliciousPatternChecker.MaliciousPoint>> l = new ArrayList<>();

        for(int i=0; i<points.length; i++){
            l.add(Arrays.stream(points[i]).collect(Collectors.toSet()));
        }

        try {
            //TODO: why should we use reflection to invoke the cartesianProduct method of Sets? Why does gradle show error when invoking the method directly?
            Class o = Class.forName("com.google.common.collect.Sets");
            for(Method m : o.getMethods()){
                if(m.toString().contains("public static java.util.Set com.google.common.collect.Sets.cartesianProduct(java.util.List)")){
                    m.invoke(null, l);
                    Set<List<MaliciousPatternChecker.MaliciousPoint>> res = (Set<List<MaliciousPatternChecker.MaliciousPoint>>) m.invoke(null, l);//Sets.cartesianProduct(l);

                    int index = 0;
                    MaliciousPatternChecker.MaliciousPattern[] patterns = new MaliciousPatternChecker.MaliciousPattern[res.size()];

                    for(List<MaliciousPatternChecker.MaliciousPoint> pl : res){
                        patterns[index] = new MaliciousPatternChecker.MaliciousPattern(name + (++index), pl.toArray(new MaliciousPatternChecker.MaliciousPoint[0]));
                    }

                    return patterns;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
//        System.exit(-1);
        return null;
    }
}
