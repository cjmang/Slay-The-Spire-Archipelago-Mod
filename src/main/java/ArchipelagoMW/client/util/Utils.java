package ArchipelagoMW.client.util;

public class Utils {

    @FunctionalInterface
    public static interface TriFunction<A,B,C,R>
    {

        public R apply(A a, B b, C c);
    }
}
