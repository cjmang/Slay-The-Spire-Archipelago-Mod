package ArchipelagoMW.game;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.game.teams.PlayerManager;
import ArchipelagoMW.game.teams.TeamManager;
import ArchipelagoMW.game.items.ui.ArchipelagoRewardScreen;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CharacterManager {
    private static final Logger logger = LogManager.getLogger(CharacterManager.class);


    private Map<String, CharacterConfig> characters = Collections.emptyMap();
    private final Set<String> availableAPChars = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private AbstractPlayer currentCharacter;
    private CharacterConfig currentCharacterConfig;
    private final List<CharacterConfig> unrecognizedCharacters = new CopyOnWriteArrayList<>();
    private final LocationTracker locationTracker;
    private final APContext ctx;

    public CharacterManager(APContext ctx)
    {
        this.locationTracker = ctx.getLocationTracker();
        this.ctx = ctx;
    }

    public void initialize(List<CharacterConfig> configs)
    {
        characters = configs.stream().collect(Collectors.toMap(c -> c.officialName, Function.identity()));
        availableAPChars.clear();
        for(CharacterConfig config : characters.values())
        {
            logger.info(config.name);
            availableAPChars.add(config.officialName);
        }

        if(currentCharacter != null)
        {
            currentCharacterConfig = characters.get(currentCharacter.chosenClass.name());
        }

    }

    public Map<String, CharacterConfig> getCharacters() {
        return characters;
    }

    public AbstractPlayer getCurrentCharacter() {
        return currentCharacter;
    }

    public CharacterConfig getCurrentCharacterConfig() {
        return currentCharacterConfig;
    }

    public Set<String> getAvailableAPChars() {
        return availableAPChars;
    }

    public List<CharacterConfig> getUnrecognizedCharacters() {
        return unrecognizedCharacters;
    }

    public boolean selectCharacter(String officialName)
    {
        CharacterConfig config = characters.get(officialName);
        if(config == null)
        {
            return false;
        }
        selectCharacter(config);
        return true;
    }

    public long toCharacterLocationID(long id)
    {
        return id + (200L * currentCharacterConfig.charOffset);
    }

    public void markUnrecognziedCharacters()
    {
        unrecognizedCharacters.clear();
        List<String> names = getCharacters().keySet().stream()
                .filter(n -> CardCrawlGame.characterManager.getAllCharacters().stream().noneMatch(o -> o.chosenClass.name().equals(n)))
                .collect(Collectors.toList());
        logger.info("Unrecognized Characters {}", names);
        for(String name : names)
        {
            CharacterConfig config = characters.get(name);
            logger.info("Found config for unrecognized character {}", name);
            unrecognizedCharacters.add(config);
        }
    }

    public void handleIroncladOverride(CharacterConfig other)
    {
        CharacterConfig config = new CharacterConfig();
        config.name = "Ironclad";
        config.officialName = AbstractPlayer.PlayerClass.IRONCLAD.name();
        config.modNum = other.modNum;
        config.optionName = "the_ironclad";
        config.charOffset = other.charOffset;
        config.ascension = other.ascension;
        config.downfall = other.downfall;
        config.finalAct = other.finalAct;
        config.seed = "";
        characters.put(config.officialName,config);
    }

    public boolean isItemIDForCurrentCharacter(long itemID)
    {
        if(currentCharacterConfig == null)
        {
            return false;
        }
        int offset = currentCharacterConfig.charOffset;
        return itemID > offset * 20L && itemID < (offset + 1) * 20L;
    }

    private void selectCharacter(CharacterConfig config)
    {

        currentCharacterConfig = config;
        currentCharacter = CardCrawlGame.characterManager.getAllCharacters()
                .stream()
                .filter(c -> currentCharacterConfig.officialName.equalsIgnoreCase(c.chosenClass.name()))
                .findFirst()
                .get();
        locationTracker.initialize(config.charOffset, unrecognizedCharacters.stream()
                .map(c -> (long) c.charOffset)
                .collect(Collectors.toList()));
        ArchipelagoRewardScreen.rewards.clear();
        ArchipelagoRewardScreen.setReceivedItemsIndex(0);
        ArchipelagoRewardScreen.apRareReward = false;
        ArchipelagoRewardScreen.apReward= false;
        ArchipelagoRewardScreen.APScreen= false;
        ctx.getShopManager().initializeShop();
        locationTracker.scoutAllLocations();
        locationTracker.scoutShop(APContext.getContext().getShopManager().getTotalSlots());
        TeamManager.initialLoad();
        PlayerManager.initialLoad();
    }

}
