package kr.ac.kaist.wala.adlib.types;

import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import sun.jvm.hotspot.types.WrongTypeException;

/**
 * Created by leesh on 08/02/2017.
 */
public class Intent {
    public static final TypeName INTENT_TYPE = TypeName.findOrCreate("Landroid/content/Intent");
    public static final int NONE = -1;

    public enum PutExtraSelector{
        PUT_EXTRA1(1, Selector.make("putExtra(Ljava/lang/String;B)Landroid/content/Intent;")),
        PUT_EXTRA2(1, Selector.make("putExtra(Ljava/lang/String;C)Landroid/content/Intent;")),
        PUT_EXTRA3(1, Selector.make("putExtra(Ljava/lang/String;D)Landroid/content/Intent;")),
        PUT_EXTRA4(1, Selector.make("putExtra(Ljava/lang/String;F)Landroid/content/Intent;")),
        PUT_EXTRA5(1, Selector.make("putExtra(Ljava/lang/String;I)Landroid/content/Intent;")),
        PUT_EXTRA6(1, Selector.make("putExtra(Ljava/lang/String;J)Landroid/content/Intent;")),
        PUT_EXTRA7(1, Selector.make("putExtra(Ljava/lang/String;Landroid/os/Bundle;)Landroid/content/Intent;")),
        PUT_EXTRA8(1, Selector.make("putExtra(Ljava/lang/String;Landroid/os/IBinder;)Landroid/content/Intent;")),
        PUT_EXTRA9(1, Selector.make("putExtra(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;")),
        PUT_EXTRA10(1, Selector.make("putExtra(Ljava/lang/String;Ljava/io/Serializable;)Landroid/content/Intent;")),
        PUT_EXTRA11(1, Selector.make("putExtra(Ljava/lang/String;Ljava/lang/CharSequence;)Landroid/content/Intent;")),
        PUT_EXTRA12(1, Selector.make("putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;")),
        PUT_EXTRA13(1, Selector.make("putExtra(Ljava/lang/String;S)Landroid/content/Intent;")),
        PUT_EXTRA14(1, Selector.make("putExtra(Ljava/lang/String;Z)Landroid/content/Intent;")),
        PUT_EXTRA15(1, Selector.make("putExtra(Ljava/lang/String;[B)Landroid/content/Intent;")),
        PUT_EXTRA16(1, Selector.make("putExtra(Ljava/lang/String;[C)Landroid/content/Intent;")),
        PUT_EXTRA17(1, Selector.make("putExtra(Ljava/lang/String;[D)Landroid/content/Intent;")),
        PUT_EXTRA18(1, Selector.make("putExtra(Ljava/lang/String;[F)Landroid/content/Intent;")),
        PUT_EXTRA19(1, Selector.make("putExtra(Ljava/lang/String;[I)Landroid/content/Intent;")),
        PUT_EXTRA20(1, Selector.make("putExtra(Ljava/lang/String;[J)Landroid/content/Intent;")),
        PUT_EXTRA21(1, Selector.make("putExtra(Ljava/lang/String;[Landroid/os/Parcelable;)Landroid/content/Intent;")),
        PUT_EXTRA22(1, Selector.make("putExtra(Ljava/lang/String;[Ljava/lang/CharSequence;)Landroid/content/Intent;")),
        PUT_EXTRA23(1, Selector.make("putExtra(Ljava/lang/String;[Ljava/lang/String;)Landroid/content/Intent;")),
        PUT_EXTRA24(1, Selector.make("putExtra(Ljava/lang/String;[S)Landroid/content/Intent;")),
        PUT_EXTRA25(1, Selector.make("putExtra(Ljava/lang/String;[Z)Landroid/content/Intent;")),
        PUT_EXTRA26(NONE, Selector.make("putExtras(Landroid/content/Intent;)Landroid/content/Intent;")),
        PUT_EXTRA27(NONE, Selector.make("putExtras(Landroid/os/Bundle;)Landroid/content/Intent;")),
        ;
        private final int keyIndex;
        private final Selector s;

        PutExtraSelector(int keyIndex, Selector s){ this.keyIndex = keyIndex; this.s = s; }

        public Selector getSelector(){
            return s;
        }

        public int getActionIndex(){
            return this.keyIndex;
        }
    }

    public enum AddCategorySelector{
        ADD_CATEGORY1(1, Selector.make("addCategory(Ljava/lang/String;)Landroid/content/Intent;")),
        ;
        private final int categoryIndex;
        private final Selector s;

        AddCategorySelector(int categoryIndex, Selector s){ this.categoryIndex = categoryIndex; this.s = s; }

        public Selector getSelector(){
            return s;
        }

        public int getCategoryIndex(){
            return this.categoryIndex;
        }
    }

    public enum AddFlagsSelector{
        ADD_FLAGS(1, Selector.make("addFlags(I)Landroid/content/Intent;")),
        ;
        private final int flagIndex;
        private final Selector s;

        AddFlagsSelector(int flagIndex, Selector s){ this.flagIndex = flagIndex; this.s = s; }

        public Selector getSelector(){
            return s;
        }

        public int getFlagIndex(){
            return this.flagIndex;
        }
    }

    public enum SetActionSelector{
        SET_ACTION(1, Selector.make("setAction(Ljava/lang/String;)Landroid/content/Intent;")),
        ;
        private final int actionIndex;
        private final Selector s;

        SetActionSelector(int actionIndex, Selector s){ this.actionIndex = actionIndex; this.s = s; }

        public Selector getSelector(){
            return s;
        }

        public int getFlagIndex(){
            return this.actionIndex;
        }
    }

    public enum InitSelector{
        INIT_INTENT1(Selector.make("<init>()V")),
        INIT_INTENT2(Selector.make("<init>(Landroid/content/Context;Ljava/lang/Class;)V")),
        INIT_INTENT3(Selector.make("<init>(Landroid/content/Intent;)V")),
        INIT_INTENT4(Selector.make("<init>(Landroid/content/Intent;Z)V")),
        INIT_INTENT5(Selector.make("<init>(Landroid/os/Parcel;)V")),
        INIT_INTENT6(Selector.make("<init>(Ljava/lang/String;)V")),
        INIT_INTENT7(Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;)V")),
        INIT_INTENT8(Selector.make("<init>(Ljava/lang/String;Landroid/net/Uri;Landroid/content/Context;Ljava/lang/Class;)V")),
        ;

        private final Selector s;

        InitSelector(Selector s){
            this.s = s;
        }

        public Selector getSelector(){
            return s;
        }

        public static InitSelector matchInit(Selector s){
            for(InitSelector i : InitSelector.values()){
                if(i.getSelector().equals(s))
                    return i;
            }
            return null;
        }
    }

    public enum ActivityAction{
        ACTION_MAIN("android.intent.action.MAIN"),
        ACTION_VIEW("android.intent.action.VIEW"),
        ACTION_ATTACH_DATA("android.intent.action.ATTACH_DATA"),
        ACTION_EDIT("android.intent.action.EDIT"),
        ACTION_PICK("android.intent.action.PICK"),
        ACTION_CHOOSER("android.intent.action.CHOOSER"),
        ACTION_GET_CONTENT("android.intent.action.GET_CONTENT"),
        ACTION_DIAL("android.intent.action.DIAL"),
        ACTION_CALL("android.intent.action.CALL"),
        ACTION_SEND("android.intent.action.SEND"),
        ACTION_SENDTO("android.intent.action.SENDTO"),
        ACTION_ANSWER("android.intent.action.ANSWER"),
        ACTION_INSERT("android.intent.action.INSERT"),
        ACTION_DELETE("android.intent.action.DELETE"),
        ACTION_RUN("android.intent.action.RUN"),
        ACTION_SYNC("android.intent.action.SYNC"),
        ACTION_PICK_ACTIVITY("android.intent.action.PICK_ACTIVITY"),
        ACTION_SEARCH("android.intent.action.SEARCH"),
        ACTION_WEB_SEARCH("android.intent.action.WEB_SEARCH"),
        ACTION_FACTORY_TEST("android.intent.action.FACTORY_TEST"),
        ;


        private final String v;

        ActivityAction(String v){
            this.v = v;
        }

        public String getValue(){
            return v;
        }

        public static ActivityAction matchActivityAction(String v){
            for(ActivityAction a : ActivityAction.values()){
                if(a.getValue().equals(v))
                    return a;
            }

            return null;
        }
    }


    public enum BroadcastAction{
        ACTION_TIME_TICK("android.intent.action.TIME_TICK"),
        ACTION_TIME_CHANGED("android.intent.action.TIME_SET"),
        ACTION_TIMEZONE_CHANGED("android.intent.action.TIMEZONE_CHANGED"),
        ACTION_BOOT_COMPLETED("android.intent.action.BOOT_COMPLETED"),
        ACTION_PACKAGE_ADDED("android.intent.action.PACKAGE_ADDED"),
        ACTION_PACKAGE_CHANGED("android.intent.action.PACKAGE_CHANGED"),
        ACTION_PACKAGE_REMOVED("android.intent.action.PACKAGE_REMOVED"),
        ACTION_PACKAGE_RESTARTED("android.intent.action.PACKAGE_RESTARTED"),
        ACTION_PACKAGE_DATA_CLEARED("android.intent.action.PACKAGE_DATA_CLEARED"),
        ACTION_PACKAGE_SUSPENDED("android.intent.action.PACKAGES_SUSPENDED"),
        ACTION_PACKAGE_UNSUSPENDED("android.intent.action.PACKAGES_UNSUSPENDED"),
        ACTION_UID_REMOVED("android.intent.action.UID_REMOVED"),
        ACTION_BATTERY_CHANGED("android.intent.action.BATTERY_CHANGED"),
        ACTION_POWER_CONNECTED("android.intent.action.ACTION_POWER_CONNECTED"),
        ACTION_POWER_DISCONNECTED("android.intent.action.ACTION_POWER_DISCONNECTED"),
        ACTION_SHUTDOWN("android.intent.action.ACTION_SHUTDOWN")
        ;

        private final String v;

        BroadcastAction(String v){
            this.v = v;
        }

        public String getValue(){
            return v;
        }

        public static BroadcastAction matchBroadcastAction(String v){
            for(BroadcastAction b : BroadcastAction.values()){
                if(b.getValue().equals(v))
                    return b;
            }
            return null;
        }
    }

    public enum Category{
        CATEGORY_DEFAULT("android.intent.category.DEFAULT"),
        CATEGORY_BROWSABLE("android.intent.category.BROWSABLE"),
        CATEGORY_TAB("android.intent.category.TAB"),
        CATEGORY_ALTERNATIVE("android.intent.category.ALTERNATIVE"),
        CATEGORY_SELECTED_ALTERNATIVE("android.intent.category.SELECTED_ALTERNATIVE"),
        CATEGORY_LAUNCHER("android.intent.category.LAUNCHER"),
        CATEGORY_INFO("android.intent.category.INFO"),
        CATEGORY_HOME("android.intent.category.HOME"),
        CATEGORY_PREFERENCE("android.intent.category.PREFERENCE"),
        CATEGORY_TEST("android.intent.category.TEST"),
        CATEGORY_CAR_DOCK("android.intent.category.CAR_DOCK"),
        CATEGORY_DESK_DOCK("android.intent.category.DESK_DOCK"),
        CATEGORY_LE_DESK_DOCK("android.intent.category.LE_DESK_DOCK"),
        CATEGORY_HE_DESK_DOCK("android.intent.category.HE_DESK_DOCK"),
        CATEGORY_CAR_MODE("android.intent.category.CAR_MODE"),
        CATEGORY_APP_MARKET("android.intent.category.APP_MARKET"),
        ;

        private final String v;

        Category(String v){
            this.v = v;
        }

        public String getValue(){
            return v;
        }

        public static Category matchCategory(String v){
            for(Category c : Category.values()){
                if(c.getValue().equals(v))
                    return c;
            }

            return null;
        }
    }

    public enum ExtraData{
        EXTRA_ALARM_COUNT("android.intent.extra.ALARM_COUNT"),
        EXTRA_BCC("android.intent.extra.BCC"),
        EXTRA_CC("android.intent.extra.CC"),
        EXTRA_CHANGED_COMPONENT_NAME("android.intent.extra.changed_component_name"),
        EXTRA_DATA_REMOVED("android.intent.extra.DATA_REMOVED"),
        EXTRA_DOCK_STATE("android.intent.extra.DOCK_STATE"),
        EXTRA_DOCK_STATE_HE_DESK(4),
        EXTRA_DOCK_STATE_LE_DESK(3),
        EXTRA_DOCK_STATE_CAR(2),
        EXTRA_DOCK_STATE_DESK(1),
        EXTRA_DOCK_STATE_UNDOCKED(0),
        EXTRA_DONT_KILL_APP("android.intent.extra.DONT_KILL_APP"),
        EXTRA_EMAIL("android.intent.extra.EMAIL"),
        EXTRA_INITIAL_INTENTS("android.intent.extra.INITIAL_INTENTS"),
        EXTRA_INTENT("android.intent.extra.INTENT"),
        EXTRA_KEY_EVENT("android.intent.extra.KEY_EVENT"),
        EXTRA_ORIGINATING_URI("android.intent.extra.ORIGINATING_URI"),
        EXTRA_PHONE_NUMBER("android.intent.extra.PHONE_NUMBER"),
        EXTRA_REFERRER("android.intent.extra.REFERRER"),
        EXTRA_REMOTE_INTENT_TOKEN("android.intent.extra.remote_intent_token"),
        EXTRA_REPLACING("android.intent.extra.REPLACING"),
        EXTRA_SHORTCUT_ICON("android.intent.extra.shortcut.ICON"),
        EXTRA_SHORTCUT_ICON_RESOURCE("android.intent.extra.shortcut.ICON_RESOURCE"),
        EXTRA_SHORTCUT_INTENT("android.intent.extra.shortcut.INTENT"),
        EXTRA_STREAM("android.intent.extra.STREAM"),
        EXTRA_SHORTCUT_NAME("android.intent.extra.shortcut.NAME"),
        EXTRA_SUBJECT("android.intent.extra.SUBJECT"),
        EXTRA_TEMPLATE("android.intent.extra.TEMPLATE"),
        EXTRA_TEXT("android.intent.extra.TEXT"),
        EXTRA_TITLE("android.intent.extra.TITLE"),
        EXTRA_UID("android.intent.extra.UID")
        ;

        private final Object v;

        ExtraData(String v){
            this.v = v;
        }

        ExtraData(int v){
            this.v = v;
        }

        public Object getValue(){
            return v;
        }

        public boolean isStringConstant(){
            return (v instanceof String);
        }

        public boolean isIntegerConstant(){
            return (v instanceof Integer);
        }

        public String getStringValue(){
            if(isStringConstant())
                return (String)v;
            throw new WrongTypeException("This data does not have a string constant: " + v);
        }

        public int getIntegerValue(){
            if(isIntegerConstant())
                return (Integer)v;
            throw new WrongTypeException("This data does not have a integer constant: " + v);
        }

        public ExtraData matchExtraData(Object o){
            if(o instanceof String){
                for(ExtraData e : ExtraData.values()){
                    if(e.isStringConstant()){
                        if(e.getStringValue().equals(o))
                            return e;
                    }
                }
            }else if(o instanceof Integer){
                for(ExtraData e : ExtraData.values()){
                    if(e.isIntegerConstant()){
                        if(e.getIntegerValue() == (Integer)o)
                            return e;
                    }
                }
            }else
                throw new WrongTypeException("Extra Data must be String or Integer type.");
            return null;
        }
    }

    public enum Flag{
        FLAG_GRANT_READ_URI_PERMISSION(1, "FLAG_GRANT_READ_URI_PERMISSION"),
        FLAG_GRANT_WRITE_URI_PERMISSION(2, "FLAG_GRANT_WRITE_URI_PERMISSION"),
        FLAG_GRANT_PERSISTABLE_URI_PERMISSION(64, "FLAG_GRANT_PERSISTABLE_URI_PERMISSION"),
        FLAG_GRANT_PREFIX_URI_PREMISSION(128, "FLAG_GRANT_PREFIX_URI_PREMISSION"),
        FLAG_DEBUG_LOG_RESOLUTION(8, "FLAG_DEBUG_LOG_RESOLUTION"),
        FLAG_FROM_BACKGROUND(4, "FLAG_FROM_BACKGROUND"),
        FLAG_ACTIVITY_BROUGHT_TO_FRONT(4194304, "FLAG_ACTIVITY_BROUGHT_TO_FRONT"),
        FLAG_ACTIVITY_CLEAR_TASK(32768, "FLAG_ACTIVITY_CLEAR_TASK"),
        FLAG_ACTIVITY_CLEAR_TOP(67108864, "FLAG_ACTIVITY_CLEAR_TOP"),
        FLAC_ACTIVITY_CLEAR_WHEN_TASK_RESET(524288, "FLAC_ACTIVITY_CLEAR_WHEN_TASK_RESET"),
        FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS(8388608, "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS"),
        FLAG_ACTIVITY_FORWARD_RESULT(33554432, "FLAG_ACTIVITY_FORWARD_RESULT"),
        FLAG_ACTIVITY_LAUNCHED_FROM_HISTROY(1048576, "FLAG_ACTIVITY_LAUNCHED_FROM_HISTROY"),
        FLAG_ACTIVITY_MULTIPLE_TASK(134217728, "FLAG_ACTIVITY_MULTIPLE_TASK"),
        FLAG_ACTIVITY_NEW_DOCUMENT(524288, "FLAG_ACTIVITY_NEW_DOCUMENT"),
        FLAG_ACTIVITY_NEW_TASK(268435456, "FLAG_ACTIVITY_NEW_TASK"),
        FLAG_ACTIVITY_NO_ANIMATION(65536, "FLAG_ACTIVITY_NO_ANIMATION"),
        FLAG_ACTIVITY_NO_HISTORY(1073741824, "FLAG_ACTIVITY_NO_HISTORY"),
        FLAG_ACTIVITY_NO_USER_ACTION(262144, "FLAG_ACTIVITY_NO_USER_ACTION"),
        FLAG_ACTIVITY_PREVIOUS_IS_TOP(16777216, "FLAG_ACTIVITY_PREVIOUS_IS_TOP"),
        FLAG_ACTIVITY_RESET_TASK_IF_NEEDED(2097152, "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED"),
        FLAG_ACTIVITY_REORDER_TO_FRONT(131072, "FLAG_ACTIVITY_REORDER_TO_FRONT"),
        FLAG_ACTIVITY_SINGLE_TOP(536870912, "FLAG_ACTIVITY_SINGLE_TOP"),
        FLAG_ACTIVITY_TASK_ON_HOME(16384, "FLAG_ACTIVITY_TASK_ON_HOME"),
        FLAG_RECEIVER_REGISTERED_ONLY(1073741824, "FLAG_RECEIVER_REGISTERED_ONLY"),
        ;

        private final int v;
        private final String name;

        Flag(int v, String name){
            this.v = v;
            this.name = name;
        }

        public int getValue(){
            return this.v;
        }

        public String getName(){
            return this.name;
        }

        @Override
        public String toString(){
            return this.name;
        }

        public static Flag matchFlag(int v){
            for(Flag f : Flag.values()){
                if(v == f.getValue())
                    return f;
            }

            return null;
        }
    }
}
