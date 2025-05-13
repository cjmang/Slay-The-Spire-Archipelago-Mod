package ArchipelagoMW;

public class APContext {

    private static final APContext INSTANCE = new APContext();

    public static APContext getInstance()
    {
        return INSTANCE;
    }


}
