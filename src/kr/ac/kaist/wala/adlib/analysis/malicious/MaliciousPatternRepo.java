package kr.ac.kaist.wala.adlib.analysis.malicious;

import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.flows.PropagateFlowFunction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by leesh on 07/04/2018.
 */
public class MaliciousPatternRepo {
    public static MaliciousPatternChecker.MaliciousPattern[] patterns;

    static{
        PatternMaker maker = new PatternMaker();
        Set<MaliciousPatternChecker.MaliciousPattern> patternSet = new HashSet<>();

        patternSet.addAll(Arrays.stream(maker.make("LaunchingActivity1",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("getIntentOld(Ljava/lang/String;)Landroid/content/Intent;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("parseUri(Ljava/lang/String;I)Landroid/content/Intent;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;Landroid/content/Context;Ljava/lang/Class;)V"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;Landroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivities([Landroid/content/Intent;Landroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivities([Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivityForResult(Landroid/content/Intent;I)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivityForResult(Landroid/content/Intent;ILandroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        }
                }
                )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("LaunchingActivity2",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/ComponentName"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("setComponent(Landroid/content/ComponentName;)Landroid/content/Intent;"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;Landroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivities([Landroid/content/Intent;Landroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivities([Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivityForResult(Landroid/content/Intent;I)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivityForResult(Landroid/content/Intent;ILandroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("LaunchingActivity3",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/net/Uri"), Selector.make("create(Ljava/lang/String;)Landroid/net/Uri;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/net/Uri"), Selector.make("parse(Ljava/lang/String;)Landroid/net/Uri;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/net/Uri"), Selector.make("resolve(Ljava/lang/String;)Landroid/net/Uri;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Intent"), Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;Landroid/content/Context;Ljava/lang/Class;)V"), PropagateFlowFunction.getInstance(3, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;Landroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivities([Landroid/content/Intent;Landroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivities([Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivityForResult(Landroid/content/Intent;I)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivityForResult(Landroid/content/Intent;ILandroid/os/Bundle;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        }
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("GettingLocation1",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/Location"), Selector.make("getLatitude()D"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/Location"), Selector.make("getLongitude()D"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/Context"), Selector.make("startActivity(Landroid/content/Intent;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("GettingLocation2",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/location/LocationManager"), Selector.make("requestLocationUpdates(Landroid/location/LocationRequest;Landroid/location/LocationListener;Landroid/os/Looper;Landroid/app/PendingIntent;)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("MaliciousFileDownload1",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(4, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/StringI;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read()I"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([BII)I"), PropagateFlowFunction.getInstance(1, 2)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([B)I"), PropagateFlowFunction.getInstance(1, 2)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([B)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([BII)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write(I)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("MaliciousFileDownload2",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(4, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/StringI;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read()I"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([BII)I"), PropagateFlowFunction.getInstance(1, 2)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([B)I"), PropagateFlowFunction.getInstance(1, 2)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/ByteArrayOutputStream"), Selector.make("write([BII)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/ByteArrayOutputStream"), Selector.make("write(I)V"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/ByteArrayOutputStream"), Selector.make("toByteArray()[B"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/ByteArrayInputStream"), Selector.make("<init>([BII)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/ByteArrayInputStream"), Selector.make("<init>([B)V"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/ByteArrayInputStream"), Selector.make("read([BII)I"), PropagateFlowFunction.getInstance(1, 2)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/ByteArrayInputStream"), Selector.make("read()I"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([B)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([BII)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write(I)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("MaliciousFileDownload3",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/net/Uri"), Selector.make("create(Ljava/lang/String;)Landroid/net/Uri;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/net/Uri"), Selector.make("parse(Ljava/lang/String;)Landroid/net/Uri;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/net/Uri"), Selector.make("resolve(Ljava/lang/String;)Landroid/net/Uri;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/app/DownloadManager$Request"), Selector.make("<init>(Landroid/net/Uri;)V"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/app/DownloadManager"), Selector.make("enqueue(Landroid/app/DownloadManager$Request;)J"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));


        patternSet.addAll(Arrays.stream(maker.make("HttpRequest1",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/methods/HttpGet"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/methods/HttpGet"), Selector.make("<init>(Ljava/net/URI;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/methods/HttpPost"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/methods/HttpPost"), Selector.make("<init>(Ljava/net/URI;)V"), PropagateFlowFunction.getInstance(2, 1)),

                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/HttpClient"), Selector.make("execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/client/HttpResponseHandler;)Ljava/lang/Object;"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/HttpClient"), Selector.make("execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/client/HttpResponseHandler;Lorg/apache/http/protocol/HttpContext;)Ljava/lang/Object;"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/HttpClient"), Selector.make("execute(Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/client/HttpResponseHandler;)Ljava/lang/Object;"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/HttpClient"), Selector.make("execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;)Lorg/apache/http/HttpResponse;"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/HttpClient"), Selector.make("execute(Lorg/apache/http/HttpHost;Lorg/apache/http/HttpRequest;Lorg/apache/http/protocol/HttpContext;)Lorg/apache/http/HttpResponse;"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/HttpClient"), Selector.make("execute(Lorg/apache/http/client/methods/HttpUriRequest;)Lorg/apache/http/HttpResponse;"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/HttpClient"), Selector.make("execute(Lorg/apache/http/client/methods/HttpUriRequest;Lorg/apache/http/protocol/HttpContext;)Lorg/apache/http/HttpResponse;"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("HttpRequest2",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(4, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/StringI;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read()I"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([BII)I"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([B)I"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("HttpRequest3",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(4, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/StringI;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(5, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),

                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/net/URL;Ljava/lang/String;Ljava/net/URLStreamHandler;)V"), PropagateFlowFunction.getInstance(3, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStreamReader"), Selector.make("<init>(Ljava/io/InputStream;)V"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/BufferedReader"), Selector.make("<init>(Ljava/io/Reader;I)V"), PropagateFlowFunction.getInstance(2, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/BufferedReader"), Selector.make("readLine()Ljava/lang/String;"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/BufferedReader"), Selector.make("read()I"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/BufferedReader"), Selector.make("read([CII)I"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("SensorControl",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/os/Vibrator"), Selector.make("vibrate([JI)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;I)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;II)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;IILandroid/os/Handler;)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorEventListener;Landroid/hardware/Sensor;ILandroid/os/Handler;)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorListener;I)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/hardware/SensorManager"), Selector.make("registerListener(Landroid/hardware/SensorListener;II)Z"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("GetAppInfo1",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getApplicationInfo(Ljava/lang/String;II)Landroid/content/pm/ApplicationInfo;"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledApplications(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(II)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("GetAppInfo2",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getApplicationInfo(Ljava/lang/String;II)Landroid/content/pm/ApplicationInfo;"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledApplications(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(I)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/content/pm/PackageManager"), Selector.make("getInstalledPackages(II)Ljava/util/List;"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeReference.JavaLangString.getName(), Selector.make("equals(Ljava/lang/Object;)Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeReference.JavaLangString.getName(), Selector.make("equals(Ljava/lang/Object;)Z"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeReference.JavaLangString.getName(), Selector.make("equalsIgnoreCase(Ljava/lang/String;)Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeReference.JavaLangString.getName(), Selector.make("equalsIgnoreCase(Ljava/lang/String;)Z"), PropagateFlowFunction.getInstance(2, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("loadUrl(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Landroid/webkit/WebView"), Selector.make("evaluateJavascript(Ljava/lang/String;Landroid/webkit/ValueCallback;)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("FileDelete1",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/io/File;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/io/File;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("delete()Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));

        patternSet.addAll(Arrays.stream(maker.make("FileDelete2",
                new MaliciousPatternChecker.MaliciousPoint[][]{
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/io/File;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/io/File;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("<init>(Ljava/lang/String;Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(3, 1)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("listFiles()[Ljava/io/File;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                        },
                        new MaliciousPatternChecker.MaliciousPoint[]{
                                new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/File"), Selector.make("delete()Z"), PropagateFlowFunction.getInstance(1, IFlowFunction.TERMINATE)),
                        },
                }
        )).collect(Collectors.toSet()));


        patterns = patternSet.toArray(new MaliciousPatternChecker.MaliciousPattern[0]);
    }
}


/*

private static MaliciousPatternChecker.MaliciousPattern[] maliciousPatterns = {


            new MaliciousPatternChecker.MaliciousPattern("MaliciousFileDownload1",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V"), PropagateFlowFunction.getInstance(1, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getInputStream()Ljava/io/InputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/InputStream"), Selector.make("read([BII)I"), PropagateFlowFunction.getInstance(1, 2)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/FileOutputStream"), Selector.make("write([BII)V"), PropagateFlowFunction.getInstance(2, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("HttpRequest8",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Lorg/apache/http/client/methods/HttpGet"), Selector.make("<init>(Ljava/net/URI;)V"), PropagateFlowFunction.getInstance(2, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V"), PropagateFlowFunction.getInstance(1, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getOutputStream()Ljava/io/OutputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/Writer"), Selector.make("write(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE))),

            new MaliciousPatternChecker.MaliciousPattern("HttpRequest9",
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("<init>(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(2, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/URL"), Selector.make("openConnection()Ljava/net/URLConnection;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("connect()V"), PropagateFlowFunction.getInstance(1, 1)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/net/HttpURLConnection"), Selector.make("getOutputStream()Ljava/io/OutputStream;"), PropagateFlowFunction.getInstance(1, IFlowFunction.RETURN_VARIABLE)),
                    new MaliciousPatternChecker.MaliciousPoint(TypeName.findOrCreate("Ljava/io/Writer"), Selector.make("write(Ljava/lang/String;)V"), PropagateFlowFunction.getInstance(IFlowFunction.ANY, IFlowFunction.TERMINATE))),
    };
 */