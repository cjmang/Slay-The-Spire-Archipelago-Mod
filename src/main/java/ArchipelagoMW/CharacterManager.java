package ArchipelagoMW;

import ArchipelagoMW.teams.PlayerManager;
import ArchipelagoMW.teams.TeamManager;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CharacterManager {

    private List<CharacterConfig> characters = Collections.emptyList();
    private AbstractPlayer currentCharacter;
    private CharacterConfig currentCharacterConfig;
    private final Set<String> availableAPChars = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final List<CharacterConfig> unrecognizedCharacters = new CopyOnWriteArrayList<>();

    public void initialize(List<CharacterConfig> configs)
    {
        characters = new ArrayList<>(configs);
        availableAPChars.clear();
        for(CharacterConfig config : characters)
        {
            APClient.logger.info(config.name);
            availableAPChars.add(config.officialName);
        }

    }

    public List<CharacterConfig> getCharacters() {
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
        if(officialName == null || officialName.isEmpty())
        {
            return false;
        }
        for(CharacterConfig config : characters)
        {
            if(officialName.equals(config.officialName))
            {
                selectCharacter(config);
                return true;
            }
        }
        return false;
    }

//    public void markUnrecognziedCharacters(Collection<String> names)
    public void markUnrecognziedCharacters()
    {
        unrecognizedCharacters.clear();
        List<String> names = getCharacters().stream()
                .map(c -> c.officialName)
                .filter(n -> CardCrawlGame.characterManager.getAllCharacters().stream().noneMatch(o -> o.chosenClass.name().equals(n)))
                .collect(Collectors.toList());
        APClient.logger.info("Unrecognized Characters {}", names);
        for(String name : names)
        {
            for(CharacterConfig config : characters)
            {
                if(config.officialName.equals(name)) {
                    APClient.logger.info("Found config for unrecognized character {}", name);
                    unrecognizedCharacters.add(config);
                }
            }
        }
    }

    public void handleIroncladOverride()
    {
        CharacterConfig config = new CharacterConfig();
        config.name = "Ironclad";
        config.officialName = AbstractPlayer.PlayerClass.IRONCLAD.name();
        config.modNum = 0;
        config.optionName = "the_ironclad";
        config.charOffset = 0;
        config.ascension = 0;
        config.downfall = false;
        config.finalAct = false;
        characters.add(config);
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
        if(config == currentCharacterConfig)
        {
            return;
        }

        currentCharacterConfig = config;
        currentCharacter = CardCrawlGame.characterManager.getAllCharacters()
                .stream()
                .filter(c -> currentCharacterConfig.officialName.equalsIgnoreCase(c.chosenClass.name()))
                .findFirst()
                .get();
        LocationTracker.initialize(config.charOffset, unrecognizedCharacters.stream()
                .map(c -> (long) c.charOffset)
                .collect(Collectors.toList()));
        ArchipelagoRewardScreen.rewards.clear();
        ArchipelagoRewardScreen.receivedItemsIndex = 0;
        ArchipelagoRewardScreen.apRareReward = false;
        ArchipelagoRewardScreen.apReward= false;
        ArchipelagoRewardScreen.APScreen= false;
        LocationTracker.scoutAllLocations();
        TeamManager.initialLoad();
        PlayerManager.initialLoad();
    }

}
