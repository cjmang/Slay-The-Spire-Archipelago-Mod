//package ArchipelagoMW.saythespire.patches;
//
//import ArchipelagoMW.saythespire.buffer.TextBoxBuffer;
//import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
//import sayTheSpire.Output;
//
//public class BufferPatch {
//
//    @SpirePatch(cls="sayTheSpire.Output", requiredModId = "Say_the_Spire", method="setupBuffers")
//    public static class AddBuffersPatch
//    {
//        public static void PostFix()
//        {
//            Output.buffers.add(new TextBoxBuffer());
//        }
//    }
//}
