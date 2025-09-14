package ArchipelagoMW.saythespire;


public class SayTheSpireProvider {


    public static SayTheSpire createInstance()
    {
        try
        {
            return new SayTheSpirePassthrough();
        }
        catch(NoClassDefFoundError ex)
        {
            return new NoOpSay();
        }
    }

}
