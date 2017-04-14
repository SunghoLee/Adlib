package kr.ac.kaist.wala.hybridroid.ardetector.analyzer;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by leesh on 14/04/2017.
 */
public class MaliciousPatternChecker {
    private final ReachableAPIFlowGraph graph;
    private final IClassHierarchy cha;
    private final Set<MaliciousPattern> mps = new HashSet<>();
    private final Set<MaliciousPatternWarning> warns = new HashSet<>();

    public MaliciousPatternChecker(ReachableAPIFlowGraph graph, IClassHierarchy cha){
        this.graph = graph;
        this.cha = cha;
    }

    public void addMaliciousPattern(MaliciousPattern mp){
        mp.setClassHierrachy(cha);
        mps.add(mp);
    }


    public void addMaliciousPatterns(MaliciousPattern... mps){
        for(MaliciousPattern mp : mps) {
            mp.setClassHierrachy(cha);
            this.mps.add(mp);
        }
    }


    public Set<MaliciousPatternWarning> checkPatterns(){

        for(APICallNode entry : graph.getEntries()){
            Set<MaliciousPattern> mps = clonePatterns();

            for(MaliciousPattern mp : mps){
                APICallNode curNode = entry;
                MaliciousPoint nextPoint = mp.getNextWithoutRemove();
                while(nextPoint != null){
                    curNode = reachableFromMPtoCALLNODE(curNode, nextPoint);
                    if(curNode == null)
                        break;

                    mp.removeNext();
                    nextPoint = mp.getNextWithoutRemove();
                }

                if(mp.isDone()){
                    warns.add(new MaliciousPatternWarning(entry, mp.toString()));
                }
            }
        }

        return warns;
    }

    public APICallNode reachableFromMPtoCALLNODE(APICallNode start, MaliciousPoint mp){
        Queue<APICallNode> queue = new LinkedBlockingQueue<>();
        Set<APICallNode> visited = new HashSet<>();

        queue.add(start);

        while(!queue.isEmpty()){
            APICallNode n = queue.poll();
            visited.add(n);

            Iterator<APICallNode> iSucc = graph.getSuccNodes(n);

            while(iSucc.hasNext()){
                APICallNode succ = iSucc.next();

                if(mp.isSame(succ, cha))
                    return succ;

                if(!visited.contains(succ)){
                    queue.add(succ);
                }
            }
        }

        return null;
    }

    private Set<MaliciousPattern> clonePatterns(){
        Set<MaliciousPattern> clonedPatterns = new HashSet<>();

        for(MaliciousPattern mp : mps){
            clonedPatterns.add(mp.clone());
        }

        return clonedPatterns;
    }

    public static class MaliciousPoint {
        private final TypeName tn;
        private final Selector s;

        public MaliciousPoint(TypeName tn, Selector s){
            this.tn = tn;
            this.s = s;
        }

        public TypeName getTypeName(){
            return this.tn;
        }

        public Selector getSelector(){
            return this.s;
        }

        @Override
        public String toString(){
            return tn.toString() + " . " + s.toString();
        }

        public boolean isSame(APICallNode n, IClassHierarchy cha){
            TypeReference tr1 = TypeReference.find(ClassLoaderReference.Primordial, tn);
            if(tr1 == null)
                tr1 = TypeReference.find(ClassLoaderReference.Application, tn);

            TypeReference tr2 = TypeReference.find(ClassLoaderReference.Primordial, n.getReceiverType());
            if(tr2 == null)
                tr2 = TypeReference.find(ClassLoaderReference.Application, n.getReceiverType());

            if(tr1 == null || tr2 == null)
                return tn.equals(n.getReceiverType()) && s.equals(n.getSelector());

            IClass c1 = cha.lookupClass(tr1);
            IClass c2 = cha.lookupClass(tr2);

            if(c1 == null){
                if(tr1.getClassLoader().equals(ClassLoaderReference.Primordial))
                    c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, tn));
                else
                    c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, tn));
            }

            if(c2 == null){
                if(tr2.getClassLoader().equals(ClassLoaderReference.Primordial))
                    c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, n.getReceiverType()));
                else
                    c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, n.getReceiverType()));
            }

            if(tn.equals(n.getReceiverType()) || (c1 != null && c2 != null && cha.isSubclassOf(c2, c1))){
                if(s.equals(n.getSelector()))
                    return true;
            }

            return false;
        }
    }

    public static class MaliciousPattern {
        private final Queue<MaliciousPoint> pattern = new LinkedBlockingQueue<>();
        private IClassHierarchy cha;
        private final String patternName;

        public MaliciousPattern(String name, MaliciousPoint... points){
            this.patternName = name;

            for(int i=0; i<points.length; i++){
                pattern.add(points[i]);
            }
        }

        private MaliciousPattern(String name, Queue<MaliciousPoint> queue){
            this.patternName = name;
            this.pattern.addAll(queue);
        }

        public boolean isDone(){
            return pattern.isEmpty();
        }

        public boolean isMatched(APICallNode n){
            return pattern.peek().isSame(n, cha);
        }

        private void setClassHierrachy(IClassHierarchy cha){
            this.cha = cha;
        }

        public boolean match(APICallNode n){
            if(isMatched(n)){
                pattern.poll();
                return true;
            }
            return false;
        }

        @Override
        public String toString(){
            return "[MaliciousPattern] " + this.patternName;
        }

        @Override
        public int hashCode(){
            return patternName.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof MaliciousPattern){
                MaliciousPattern mp = (MaliciousPattern) o;
                if(mp.patternName.equals(this.patternName))
                    return true;
            }
            return false;
        }

        public MaliciousPattern clone(){
            return new MaliciousPattern(patternName, pattern);
        }

        public Set<MaliciousPoint> getPoints(){
            Iterator<MaliciousPoint> iPoint = pattern.iterator();
            Set<MaliciousPoint> mpSet = new HashSet<>();

            while(iPoint.hasNext()){
                MaliciousPoint mp = iPoint.next();
                mpSet.add(mp);
            }

            return mpSet;
        }

        private MaliciousPoint getNext(){
            if(pattern.isEmpty())
                return null;
            return pattern.poll();
        }

        private MaliciousPoint getNextWithoutRemove(){
            if(pattern.isEmpty())
                return null;
            return pattern.peek();
        }

        private void removeNext(){
            pattern.poll();
        }
    }

    public static class MaliciousPatternWarning{
        private final APICallNode startPoint;
        private final String pattern;

        public MaliciousPatternWarning(APICallNode s, String pn){
            this.startPoint = s;
            this.pattern = pn;
        }

        @Override
        public String toString(){
            return pattern + " in a flow started from " + startPoint;
        }

        @Override
        public int hashCode(){
            return startPoint.hashCode() + pattern.hashCode();
        }

        public APICallNode getStartPoint(){
            return this.startPoint;
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof MaliciousPatternWarning){
                MaliciousPatternWarning mpw = (MaliciousPatternWarning) o;
                if(mpw.startPoint.equals(this.startPoint) && mpw.pattern.equals(this.pattern))
                    return true;
            }
            return false;
        }
    }
}
