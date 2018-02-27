package kr.ac.kaist.wala.adlib;

import com.google.common.collect.Lists;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import kr.ac.kaist.wala.adlib.analyzer.APITarget;
import kr.ac.kaist.wala.adlib.analyzer.MaliciousPatternChecker;
import kr.ac.kaist.wala.adlib.callgraph.BranchSlicerForConstant;
import kr.ac.kaist.wala.adlib.callgraph.CallGraphBuilderForHybridSDK;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.flows.PropagateFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.IFDSAnalyzer;
import kr.ac.kaist.wala.adlib.dataflow.ifds.InfeasiblePathException;
import kr.ac.kaist.wala.adlib.dataflow.ifds.LocalDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;
import kr.ac.kaist.wala.adlib.model.ARModeling;
import kr.ac.kaist.wala.hybridroid.util.debug.PointerAnalysisCommandlineDebugger;
import kr.ac.kaist.wala.hybridroid.util.print.IRPrinter;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by leesh on 05/01/2017.
 */
public class Main {
    private static boolean DEBUG = true;

//    private static APITarget[] targetAPIs = {
//            new APITarget(TypeName.findOrCreate("Landroid/content/pm/PackageManager")),//Landroid/content/pm/PackageManager
//            new APITarget("Ljava/io/*"),
//            new APITarget("Ljava/net/*"),
////            new APITarget(TypeName.findOrCreate("Ljava/io/FileOutputStream")),
////            new APITarget(TypeName.findOrCreate("Ljava/io/OutputStream")),
//            new APITarget(TypeName.findOrCreate("Landroid/location/LocationManager")),//Landroid/location/LocationManager
//            new APITarget(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;)V")), //Landroid/app/Activity, startActivity(Landroid/content/Intent;)V
//            new APITarget(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V")),//Landroid/webkit/WebView, loadUrl(Ljava/lang/String;)V
//            new APITarget(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V")),//evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V
//            new APITarget(TypeName.findOrCreate("Ljava/net/URLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;")),// Ljava/net/URLConnection, getInputStream()Ljava/io/InputStream;
//            new APITarget(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;")),//Ljava/net/URL, openConnection()Ljava/net/URLConnection;
//            new APITarget(TypeName.findOrCreate("Ljava/net/HttpURLConnection")),//Ljava/net/HttpURLConnection, setRequestMethod(Ljava/lang/String;)V
//            new APITarget(TypeName.findOrCreate("Landroid/os/Vibrator"), Selector.make("vibrate([JI)V")),//Landroid/os/Vibrator, vibrate([JI)V
////            new APITarget(TypeName.findOrCreate("Ljava/io/InputStream")), //Ljava/io/InputStream, read([BII)I,
//            new APITarget(TypeName.findOrCreate("Landroid/location/Location")),//Ljava/net/URL, openConnection()Ljava/net/URLConnection;
//    };

    private static Set<APITarget> targetAPIs = new HashSet<>();

    private static MaliciousPatternChecker.MaliciousPattern[] maliciousPatterns = {
            new MaliciousPatternChecker.MaliciousPattern("LaunchingActivity",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GettingLocation1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/Location"), Selector.make("getLatitude()D"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GettingLocation2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/Location"), Selector.make("getLatitude()D"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GettingLocation3",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("requestLocationUpdates(Landroid/location/LocationRequest;Landroid/location/LocationListener;Landroid/os/Looper;Landroid/app/PendingIntent;)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE))),

            new MaliciousPatternChecker.MaliciousPattern("MaliciousFileDownload1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V"), PropagateFlowFunction.getInstance(1, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([BII)I"), PropagateFlowFunction.getInstance(1, 2)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([BII)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

//            new MaliciousPatternChecker.MaliciousPattern("MaliciousFileDownload2",
//                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
//                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
//                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V"), PropagateFlowFunction.getInstance(1, 1)),
//                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
//                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([BII)I"), PropagateFlowFunction.getInstance(1, 2)),
//                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([BII)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),
//
            new MaliciousPatternChecker.MaliciousPattern("HttpRequest1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V"), PropagateFlowFunction.getInstance(1, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getOutputStream()Ljava/io/OutputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/Writer"), Selector.make("write(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE))),

//            new MaliciousPatternChecker.MaliciousPattern("HttpRequest2",
//                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl8",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/os/Vibrator"), Selector.make("vibrate([JI)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/os/Vibrator"), Selector.make("vibrate(J)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;I)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl3",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;II)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl4",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;IILandroid/os/Handler;)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl5",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;ILandroid/os/Handler;)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl6",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorListener;I)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl7",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorListener;II)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getApplicationInfo(Ljava/lang/String;II)Landroid/content/pm/ApplicationInfo;"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getApplicationInfo(Ljava/lang/String;II)Landroid/content/pm/ApplicationInfo;"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo3",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledApplications(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo4",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledApplications(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo5",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo6",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo7",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(II)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo8",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(II)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("FileDelete1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("delete()Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("FileDelete2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/io/File;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("delete()Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("FileDelete3",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/io/File;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("delete()Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("FileDelete4",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("delete()Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)))
    };

    static{
        for(int i=0; i<maliciousPatterns.length; i++){
            MaliciousPatternChecker.MaliciousPattern p = maliciousPatterns[i];
            for(MaliciousPatternChecker.MaliciousPoint mp : p.getPoints()){
                targetAPIs.add(new APITarget(mp.getTypeName(), mp.getSelector()));
            }
        }
    }

//runOnUiThread(Runnable action) android.app.Activity
    public static void main(String[] args) throws CallGraphBuilderCancelException, ClassHierarchyException, ParseException, IOException {
        String prop = args[0];
        String sdk = args[1];
        String initInstFile = args[2];

        System.out.println("#Prop: " + prop);
        System.out.println("#SDK: " + sdk);
        System.out.println("#Init: " + initInstFile);
        long start = System.currentTimeMillis();
        long cgStart = System.currentTimeMillis();

        CallGraphBuilderForHybridSDK builder = new CallGraphBuilderForHybridSDK(prop, sdk, InitInstsParser.parse(initInstFile));
        CallGraph cg = builder.makeCallGraph();
        PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();

        long cgEnd = System.currentTimeMillis();

        System.out.println("Finish to build a callgraph: " + ((cgEnd - cgStart)/1000d) + "s");

        long fgStart = System.currentTimeMillis();

//        ReachableAPIAnalysis apiAnalysis = new ReachableAPIAnalysis(cg, HybridSDKModel.getBridgeEntries());
//
//        apiAnalysis.addAPITargets(targetAPIs);
//        ReachableAPIFlowGraph apiGraph = apiAnalysis.analyze();

//        if(DEBUG)
//            GraphPrinter.print(apiGraph);

//        long fgEnd = System.currentTimeMillis();
//
//        System.err.println("Finish to construct a reachableAPIFlowGraph: " + ((fgEnd - fgStart)/1000d) + "s");

//        long pcStart = System.currentTimeMillis();

        BranchSlicerForConstant slicer = new BranchSlicerForConstant(cg, pa);
        cg = slicer.prune();

        ICFGSupergraph supergraph = ICFGSupergraph.make(cg,new AnalysisCache());
        IFDSAnalyzer ifds = new IFDSAnalyzer(supergraph, pa);
        try {
            for(CGNode n : cg) {
                if(n.getMethod().getReference().toString().contains("< Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, deleteFile(Ljava/lang/String;)V >")) {
                    System.out.println("# SEED_MATCH: " + n);
                    ifds.analyze(supergraph.getEntriesForProcedure(n)[0], new LocalDataFact(n, 2, NoneField.getInstance()));
                }
            }
        } catch (InfeasiblePathException e) {
            e.printStackTrace();
        }
        /*
        // Malicious Pattern Checking
        MaliciousPatternChecker pc = new MaliciousPatternChecker(cg.getClassHierarchy());
        pc.addMaliciousPatterns(maliciousPatterns);
        Set<MaliciousPatternChecker.MaliciousPatternWarning> mpwSet = pc.checkPatterns(cg, pa);

        List<MaliciousPatternChecker.MaliciousPatternWarning> warns = Lists.newArrayList(mpwSet);
        Collections.sort(warns, new Comparator<MaliciousPatternChecker.MaliciousPatternWarning>() {
            @Override
            public int compare(MaliciousPatternChecker.MaliciousPatternWarning o1, MaliciousPatternChecker.MaliciousPatternWarning o2) {
                if(o1.getStartPoint().toString().compareTo(o2.getStartPoint().toString()) > 0)
                    return 1;
                return -1;
            }
        });

        for(MaliciousPatternChecker.MaliciousPatternWarning mpw : warns){
            System.out.println(mpw.toString());
        }

        long pcEnd = System.currentTimeMillis();

        System.out.println("Finish to detect malicious patterns: " + ((pcEnd - fgStart)/1000d) + "s");
        System.out.println("#Max Stack Size: " + pc.getMaxStackSize());

//        CallingComponentAnalysis cca = new CallingComponentAnalysis(cg, builder.getPointerAnalysis());
//        cca.getCallingContexts();

//        for(String w : cca.getWarnings()){
//            System.out.println("W: " + w);
//        }
*/
        if(DEBUG) {
            String name = "ir_test";
            final CallGraph fcg = cg;
            IRPrinter.printIR(fcg, name, new IRPrinter.Filter(){
                @Override
                public boolean filter(CGNode n) {
                    if(n.getMethod().toString().contains("fakeRootMethod"))
                        return true;
                    if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)){
                        if(!ARModeling.isModelingMethod(fcg.getClassHierarchy(), n.getMethod()))
                            return false;
                    }
                    return true;
                }
            });

            printIR(cg, name);

            PointerAnalysisCommandlineDebugger padebugger = new PointerAnalysisCommandlineDebugger(cg, pa);
//            padebugger.debug();
        }

//            Visualizer vis = Visualizer.getInstance();
//            vis.setType(Visualizer.GraphType.Digraph);
//            Set<CGNode> visited = new HashSet<>();
//            Queue<CGNode> queue = new LinkedBlockingQueue<>();
//
//            for (CGNode n : cg) {
//                if (n.toString().contains("Application, Lcom/millennialmedia/internal/JSBridge$JSBridgeMMJS, vibrate(Ljava/lang/String;)V")) {
//                    queue.add(n);
//                    break;
//                }
//            }
//
//            while (!queue.isEmpty()) {
//                CGNode n = queue.poll();
//                visited.add(n);
//
//                Iterator<CGNode> iSucc = cg.getSuccNodes(n);
//                while (iSucc.hasNext()) {
//                    CGNode succ = iSucc.next();
//                    if (!visited.contains(succ)) {
//                        if (succ.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)) {
//                            if (!ARModeling.isModelingMethod(cg.getClassHierarchy(), succ.getMethod()))
//                                continue;
//                        }
//                        vis.fromAtoB(n, succ);
//                        queue.add(succ);
//                    }
//                }
//            }
//
//            vis.printGraph("test.dot");
//            vis.clear();

//            for(CGNode n : cg){
//                if(n.toString().contains("Node: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice$JSIAdToDeviceInnerHandler, handleMessage(Landroid/os/Message;)V > Context: FirstMethodContextPair: [First: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, playVideo(Ljava/lang/String;)V >] : Everywhere")){
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 4);
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        System.out.println("\t\t#IK: " + ik);
//                    }
//                }
//
//                if(n.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V > Context: FirstMethodContextPair: [First: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V >] : Everywhere")){
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 5);
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        PointerKey fpk = pa.getHeapModel().getPointerKeyForInstanceField(ik, ik.getConcreteType().getField(Atom.findOrCreateAsciiAtom("what")));
//                        System.out.println("\t\t#IK: " + ik);
//                        System.out.println("\t\t\t#FPK: " + fpk);
//                        for(InstanceKey fik : pa.getPointsToSet(fpk)){
//                            System.out.println("\t\t\t\t#FIK: " + fik);
//                        }
//                    }
//                }
//
//                if(n.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V > Context: FirstMethodContextPair: [First: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V >] : Everywhere")){
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 5);
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        PointerKey fpk = pa.getHeapModel().getPointerKeyForInstanceField(ik, ik.getConcreteType().getField(Atom.findOrCreateAsciiAtom("what")));
//                        System.out.println("\t\t#IK: " + ik);
//                        System.out.println("\t\t\t#FPK: " + fpk);
//                        for(InstanceKey fik : pa.getPointsToSet(fpk)){
//                            System.out.println("\t\t\t\t#FIK: " + fik);
//                        }
//                    }
//                }

                //

//                if(n.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/MHandler, handleMessage(Landroid/os/Message;)V > Context: FirstMethodContextPair: [First: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, vibration()V >] : Everywhere")){
//                    int var = 4;
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, var);
//
//
//                    System.out.println("\t#PK: " + pk);
////                    System.out.println("\t\tV: " + n.getIR().getSymbolTable().getConstantValue(var));
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        System.out.println("\t\t#IK: " + ik);
//                    }
//                }
//
//
//                if(n.toString().contains("Node: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, adConverted()V > Context: FirstMethodContextPair: [First: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, adConverted()V >] : Everywhere")){
//                    int var = 9;
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, var);
//
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        System.out.println("\t\t#IK: " + ik);
//                        IField f = ik.getConcreteType().getField(Atom.findOrCreateAsciiAtom("what"));
//                        System.out.println("\t\t\t#F: " + f);
//                        PointerKey fPK = pa.getHeapModel().getPointerKeyForInstanceField(ik, f);
//                        for(InstanceKey fik : pa.getPointsToSet(fPK)){
//                            System.out.println("\t\t\t\t#FIK: " + fik);
//                        }
//                    }
//                }
//            }

        long end = System.currentTimeMillis();
        System.out.println("#AnalysisTime: " + ((end - start)/1000d) + "s");
    }

    public static void printIR(CallGraph cg, String out) {
        File outFile = new File(out);

        try {
            BufferedWriter e = new BufferedWriter(new FileWriter(outFile));
            Iterator var4 = cg.iterator();

            while(var4.hasNext()) {
                CGNode n = (CGNode)var4.next();
                e.write("N" + n.toString() + "\n");
                e.write("=======================================\n");
                IR ir = makeIR(n);
                if(ir != null) {
                    SSAInstruction[] insts = ir.getInstructions();
                    int index = 1;

                    for(int iSucc = 0; iSucc < insts.length; ++iSucc) {
                        SSAInstruction succ = insts[iSucc];
                        if(succ != null) {
                            e.write("( " + succ.iindex + " ) " + succ + "\n");
                        }
                    }

                    e.write("[Succ]=================================\n");
                    Iterator var13 = cg.getSuccNodes(n);

                    while(var13.hasNext()) {
                        CGNode var12 = (CGNode)var13.next();
                        e.write("\t" + var12 + "\n");
                    }
                }

                e.newLine();
                e.newLine();
            }

            e.flush();
            e.close();
        } catch (IOException var11) {
            var11.printStackTrace();
        }

    }

    private static IR makeIR(CGNode n) {
        IR ir = n.getIR();
        if(ir == null) {
            DexIRFactory irFactory = new DexIRFactory();

            try {
                ir = irFactory.makeIR(n.getMethod(), Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
            } catch (NullPointerException var4) {
                return null;
            }
        }

        return ir;
    }
}
