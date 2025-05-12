package ArchipelagoMW.patches;

import ArchipelagoMW.APClient;
import ArchipelagoMW.teams.TeamManager;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.helpers.AsyncSaver;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.saveAndContinue.SaveFileObfuscator;
import dev.koifysh.archipelago.network.client.SetPacket;
import javassist.CtBehavior;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SavePatch {

    public static String compressedSave = "";
    public static String savedChar = "";

    public static String AP_SAVE_STRING = "spire_" + APClient.apClient.getSlot() + "_save";
    public static String AP_SAVE_CHAR = "spire_" + APClient.apClient.getSlot() + "_char";

    @SpirePatch(clz = SaveAndContinue.class, method = "loadSaveString", paramtypez = {String.class})
    public static class LoadSaveString {

        public static SpireReturn<String> Prefix() {
            String saveString = decompress(compressedSave);
            return SpireReturn.Return(SaveFileObfuscator.isObfuscated(saveString) ? SaveFileObfuscator.decode(saveString, "key") : saveString);
        }
    }

    @SpirePatch(clz = SaveAndContinue.class, method = "save")
    public static class Save {

        @SpireInsertPatch(locator = Locator.class, localvars = {"data", "save"})
        public static SpireReturn<Void> Insert(String data, SaveFile save) {
            // hopefully Save data?
            if (save.current_room.equals(TreasureRoomBoss.class.getName()) && TeamManager.myTeam == null) {
                compressedSave = compress(data);
                SetPacket savePacket = new SetPacket(AP_SAVE_STRING, 0);
                SetPacket charPacket = new SetPacket(AP_SAVE_CHAR, APClient.charManager.getCurrentCharacter().chosenClass.name());
                savePacket.addDataStorageOperation(SetPacket.Operation.REPLACE, compressedSave);
                APClient.apClient.dataStorageSet(savePacket);
                APClient.apClient.dataStorageSet(charPacket);
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.MethodCallMatcher(AsyncSaver.class, "save");
                return LineFinder.findAllInOrder(ctBehavior, match);
            }
        }
    }

    /**
     * return a Gziped and base64 encoded string.
     *
     * @param str String to be compressed
     * @return a compressed String.
     */
    private static String compress(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        try {
            System.out.println("String length : " + str.length());
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(obj);
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
            gzip.flush();
            gzip.close();
            String outStr = Base64.getEncoder().encodeToString(obj.toByteArray());
            System.out.println("Output String length : " + outStr.length());
            return outStr;
        } catch (Exception ignored) {
            return Base64.getEncoder().encodeToString(str.getBytes());
        }
    }

    /**
     * Takes a Compressed Base64 encoded string and decompresses it.
     *
     * @param data Base64 string
     * @return uncompressed decoded String
     */
    private static String decompress(String data) {
        byte[] compressed = Base64.getDecoder().decode(data);
        final StringBuilder outStr = new StringBuilder();
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        if (isCompressed(compressed)) {
            try {
                final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    outStr.append(line);
                }
            } catch (IOException ignored) {
                return "";
            }
        } else {
            outStr.append(data);
        }
        return outStr.toString();
    }

    private static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
