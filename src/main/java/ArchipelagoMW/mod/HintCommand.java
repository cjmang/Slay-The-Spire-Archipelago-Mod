package ArchipelagoMW.mod;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import basemod.DevConsole;
import basemod.devcommands.ConsoleCommand;
import io.github.archipelagomw.APResult;
import io.github.archipelagomw.Print.APPrintJsonType;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.PrintJSONEvent;
import io.github.archipelagomw.network.client.SayPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HintCommand extends ConsoleCommand {

    public HintCommand() {
        maxExtraTokens = 10;
        minExtraTokens = 0;
        requiresPlayer = false;
        simpleCheck = true;

    }

    @Override
    protected void execute(String[] strings, int depth) {
        APClient client = APContext.getContext().getClient();
        if(client == null)
        {
            DevConsole.log("Not Connected to AP");
            return;
        }
        if(!strings[depth].startsWith("!"))
        {
            DevConsole.log("apCommand's first argument must start with '!', got '" + strings[depth] + "'");
            return;
        }
        if("!admin".equals(strings[depth]))
        {
            DevConsole.log("!admin commands are not supported through StS");
            return;
        }
        String msg = String.join(" ", Arrays.asList(strings).subList(1, strings.length)).trim();
        SayPacket sayPacket = new SayPacket(msg);
        APResult<Void> result = client.sendPackets(Collections.singletonList(sayPacket));
        if(result.getCode() != APResult.ResultCode.SUCCESS)
        {
            DevConsole.log("Failed to send request to server: " + result.getCode());
        }
        APClient.logger.info("Request sent to server: '{}'", msg);
    }

    @Override
    protected ArrayList<String> extraOptions(String[] tokens, int depth) {
        if(tokens.length > 1 && tokens[1].startsWith("!"))
        {
            complete = true;
        }
        return new ArrayList<>();
    }

    @Override
    protected void errorMsg() {
        DevConsole.log("apCommand !<server Command> [parameters...]");
    }

    @ArchipelagoEventListener
    public static void handleCommandResult(PrintJSONEvent event)
    {
        APClient client = APContext.getContext().getClient();
        if(client == null)
        {
            APClient.logger.warn("Client is null in handleCommandResult");
            return;
        }
        if(event.type != APPrintJsonType.CommandResult)
        {
            return;
        }
        DevConsole.log(event.apPrint.getPlainText());
    }

}
