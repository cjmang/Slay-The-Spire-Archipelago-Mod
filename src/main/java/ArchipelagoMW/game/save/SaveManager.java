package ArchipelagoMW.game.save;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import com.megacrit.cardcrawl.saveAndContinue.SaveFileObfuscator;
import io.github.archipelagomw.network.client.SetPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SaveManager {
    private static final Logger logger = LogManager.getLogger(SaveManager.class);

    private final Map<String, String> saveCache = new ConcurrentHashMap<>();
    private final APContext context;

    public SaveManager(APContext context)
    {
        this.context = context;
    }

    public String getAPSaveKey()
    {
        return "spire_" + context.getTeam() + "_" + context.getSlot() + "_save";
    }

    public boolean hasSave(String charName)
    {
        String data = saveCache.get(charName);
        return data != null && !data.isEmpty();
    }

    public void loadSaves()
    {
        APClient client = context.getClient();
        logger.info("Attempting to load saves");
        client.asyncDSGet(Collections.singleton(getAPSaveKey()), event -> {
            logger.info("Got Response for saves.");
            if (event == null) {
                logger.debug("No save files found");
                return;
            }
            saveCache.clear();
            Object o = event.data.get(getAPSaveKey());
            if(o instanceof Map) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) o).entrySet()) {
                    if (entry.getValue() instanceof String) {
                        saveCache.put(entry.getKey(), (String) entry.getValue());
                    }
                }
            }
            logger.debug("Found saves for {}", saveCache.entrySet());
        });
    }

    public String loadSaveString(String charName)
    {
        String result = saveCache.get(charName);
        if(result == null || result.isEmpty())
        {
            logger.info("No save file found for character {}", charName);
            return "";
        }
        String saveString = decompress(result);
        return SaveFileObfuscator.isObfuscated(saveString) ? SaveFileObfuscator.decode(saveString, "key") : saveString;
    }

    public void saveString(String charName, String saveData)
    {
        Map<String, String> saveMe = new HashMap<>();
        String save = saveData;
        if(save != null && !save.isEmpty()) {
            save = compress(save);
        } else {
            save = "";
        }
        saveMe.put(charName, save);
        SetPacket packet = new SetPacket(getAPSaveKey(), new HashMap<String, String>());
        packet.addDataStorageOperation(SetPacket.Operation.DEFAULT, "");
        packet.addDataStorageOperation(SetPacket.Operation.UPDATE, saveMe);
//        logger.info("Sending save data {}", saveMe);
        context.getClient().asyncDSSet(packet, __ -> this.loadSaves());
    }

    /**
     * return a Gziped and base64 encoded string.
     *
     * @param str String to be compressed
     * @return a compressed String.
     */
    private String compress(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        try {
//            System.out.println("String length : " + str.length());
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(obj);
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
            gzip.flush();
            gzip.close();
            String outStr = Base64.getEncoder().encodeToString(obj.toByteArray());
//            System.out.println("Output String length : " + outStr.length());
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
    private String decompress(String data) {
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

    private boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
