package kr.ac.kaist.wala.hybridroid.ardetector;

import com.google.common.collect.Lists;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import kr.ac.kaist.wala.hybridroid.ardetector.analyzer.APITarget;
import kr.ac.kaist.wala.hybridroid.ardetector.analyzer.MaliciousPatternChecker;
import kr.ac.kaist.wala.hybridroid.ardetector.callgraph.CallGraphBuilderForHybridSDK;
import kr.ac.kaist.wala.hybridroid.ardetector.model.ARModeling;
import kr.ac.kaist.wala.hybridroid.util.graph.visualize.Visualizer;
import kr.ac.kaist.wala.hybridroid.util.print.IRPrinter;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GettingLocation1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GettingLocation2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GettingLocation3",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("requestLocationUpdates(Landroid/location/LocationRequest;Landroid/location/LocationListener;Landroid/os/Looper;Landroid/app/PendingIntent;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("MaliciousFileDownload",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([BII)I")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([BII)V"))),

            new MaliciousPatternChecker.MaliciousPattern("HttpRequest1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getOutputStream()Ljava/io/OutputStream;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/Writer"), Selector.make("write(Ljava/lang/String;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("HttpRequest2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/os/Vibrator"), Selector.make("vibrate([JI)V"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;I)Z"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl3",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;II)Z"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl4",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;IILandroid/os/Handler;)Z"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl5",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;ILandroid/os/Handler;)Z"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl6",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorListener;I)Z"))),

            new MaliciousPatternChecker.MaliciousPattern("SensorControl7",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorListener;II)Z"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getApplicationInfo(Ljava/lang/String;II)Landroid/content/pm/ApplicationInfo;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo2",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getApplicationInfo(Ljava/lang/String;II)Landroid/content/pm/ApplicationInfo;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo3",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledApplications(I)Ljava/util/List;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo4",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledApplications(I)Ljava/util/List;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo5",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(I)Ljava/util/List;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo6",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(I)Ljava/util/List;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo7",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(II)Ljava/util/List;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("GetAppInfo8",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(II)Ljava/util/List;")),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"))),

            new MaliciousPatternChecker.MaliciousPattern("FileDelete",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("delete()Z")))
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

        long start = System.currentTimeMillis();
        long cgStart = System.currentTimeMillis();
        CallGraphBuilderForHybridSDK builder = new CallGraphBuilderForHybridSDK(prop, sdk, InitInstsParser.parse(initInstFile));
        CallGraph cg = builder.makeCallGraph();
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

        // Malicious Pattern Checking
        MaliciousPatternChecker pc = new MaliciousPatternChecker(cg.getClassHierarchy());
        pc.addMaliciousPatterns(maliciousPatterns);
        Set<MaliciousPatternChecker.MaliciousPatternWarning> mpwSet = pc.checkPatterns(cg);

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

        if(DEBUG) {
            Visualizer vis = Visualizer.getInstance();
            vis.setType(Visualizer.GraphType.Digraph);
            Set<CGNode> visited = new HashSet<>();
            Queue<CGNode> queue = new LinkedBlockingQueue<>();

            for (CGNode n : cg) {
                if (n.toString().contains("Application, Lcom/millennialmedia/internal/JSBridge$JSBridgeMMJS, vibrate(Ljava/lang/String;)V")) {
                    queue.add(n);
                    break;
                }
            }

            while (!queue.isEmpty()) {
                CGNode n = queue.poll();
                visited.add(n);

                Iterator<CGNode> iSucc = cg.getSuccNodes(n);
                while (iSucc.hasNext()) {
                    CGNode succ = iSucc.next();
                    if (!visited.contains(succ)) {
                        if (succ.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)) {
                            if (!ARModeling.isModelingMethod(cg.getClassHierarchy(), succ.getMethod()))
                                continue;
                        }
                        vis.fromAtoB(n, succ);
                        queue.add(succ);
                    }
                }
            }

            vis.printGraph("test.dot");
            vis.clear();


            IRPrinter.printIR(cg, "ir", new IRPrinter.Filter(){
                @Override
                public boolean filter(CGNode n) {
                    if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)){
                        if(!ARModeling.isModelingMethod(cg.getClassHierarchy(), n.getMethod()))
                            return false;
                    }
                    return true;
                }
            });
        }

        long end = System.currentTimeMillis();
        System.out.println("#AnalysisTime: " + ((end - start)/1000d) + "s");
    }
}
