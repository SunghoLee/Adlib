package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ComposedEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.types.*;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.strings.Atom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by leesh on 06/01/2017.
 */
public class HybridSDKModel {

    public HybridSDKModel(){

    }

    public Iterable<Entrypoint> getEntrypoints(){
        Iterable<Entrypoint> entrypoints = null;


            Set<AndroidEntryPointLocator.LocatorFlags> flags = HashSetFactory.make();
            flags.add(AndroidEntryPointLocator.LocatorFlags.INCLUDE_CALLBACKS);
            flags.add(AndroidEntryPointLocator.LocatorFlags.EP_HEURISTIC);
            flags.add(AndroidEntryPointLocator.LocatorFlags.CB_HEURISTIC);
            AndroidEntryPointLocator eps = new AndroidEntryPointLocator(flags);
            List<AndroidEntryPoint> es = eps.getEntryPoints(cha);

            final List<Entrypoint> entries = new ArrayList<Entrypoint>();
            for (AndroidEntryPoint e : es) {
                entries.add(e);
            }

            entrypoints = new Iterable<Entrypoint>() {
                @Override
                public Iterator<Entrypoint> iterator() {
                    return entries.iterator();
                }
            };
        return entrypoints;
    }
}
