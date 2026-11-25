# XOLO keeps its views inflated from XML. R8's Android rules already keep
# constructors used by the inflater; these rules make that explicit.
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve line numbers in release crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
