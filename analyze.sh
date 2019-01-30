#!/bin/bash
SDK_DIR="/home/eshaj/Documents/repo/AdSDKs"
adsdks=( "appnext" "cheetah" "mdotm" "inmobi" "ironsource" "leadbolt" "mm" "nativex" "smaato" "startapp" "tapjoy" "upsight" "verve" )

#["cheeta"]=("cheetah.jar" "cheetah_input") ["crosschannel"]=("mdotm.jar" "mdotm_input") ["inmobi"]=("inmobi.jar" "inmobi_input") ["ironsource"]=("ironsource.jar" "ironsource_input") ["leadbolt"]=("leadbolt.jar" "leadbolt_input") ["mm"]=("mm.jar" "mm_input") ["nativex"]=("nativex.jar" "nativex_input") ["smaato"]=("smaato.jar" "smaato_input") ["startapp"]=("startapp.jar" "startapp_input") ["tapjoy"]=("tapjoy.jar" "tapjoy_input") ["upsight"]=("upsight.jar" "upsight_input") ["verve"]=("verve.jar" "verve_input") )
if [ "$1" = "all" ]; then
    for sdk in "${adsdks[@]}"
    do
        java -Xmx8G -jar Adlib.jar wala.properties $SDK_DIR/${sdk}.jar $SDK_DIR/${sdk}_input
    done
elif [ "$1" = "" ] ; then
    echo "an argument is missing: ex> ./analysis.sh cheetah"
else
    java -Xmx8G -jar Adlib.jar wala.properties $SDK_DIR/${1}.jar $SDK_DIR/${1}_input
fi
