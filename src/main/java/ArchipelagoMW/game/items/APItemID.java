package ArchipelagoMW.game.items;

import ArchipelagoMW.client.APContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum APItemID {
    CARD_DRAW(1),
    RARE_CARD_DRAW(2),
    RELIC(3),
    BOSS_RELIC(4),
    ONE_GOLD(5),
    FIVE_GOLD(6),
    PROGRESSIVE_REST(7, true, false),
    PROGRESSIVE_SMITH(8, true, false),
    CARD_SLOT(9, true, false),
    NEUTRAL_CARD_SLOT(10, true, false),
    RELIC_SLOT(11, true, false),
    POTION_SLOT(12, true, false),
    CARD_REMOVE_SLOT(13, true, false),
    CHAR_UNLOCK(14, false, false),
    FIFTEEN_GOLD(15, true),
    THIRTY_GOLD(16, true),
    BOSS_GOLD(17, true),
    POTION(18, true),
    // Marking as sanity cause it's close enough
    ASCENSION_DOWN(19, true, false),
    SAPPHIRE_KEY(20, true ),
    RUBY_KEY(21, true ),
    EMERALD_KEY(22, true ),

    DEBUFF_TRAP(50000, true, false, false),
    STRONG_DEBUFF_TRAP(50001, true, false, false),
    KILLER_DEBUFF_TRAP(50002, true, false, false),
    BUFF_TRAP(50003, true, false, false),
    STRONG_BUFF_TRAP(50004, true, false, false),
    STATUS_CARD_TRAP(50005, true, false, false),
    GREMLIN_TRAP(50006, true, false, false),

    CAW_CAW(100_000, true, true, false),
    COMBAT_BUFF(100_001, true, false, false),
    ;

    public final long value;
    public final boolean isSanity;
    public final boolean shouldNotify;
    public final boolean isCharacterSpecific;
    private static final Map<Long, APItemID> inverseMap = Arrays.stream(APItemID.values())
            .collect(Collectors.toMap(id -> id.value, Function.identity()));
    private static final Set<Long> genericIds = Collections.unmodifiableSet(Arrays.stream(APItemID.values())
                    .filter(v -> !v.isCharacterSpecific)
            .map(v -> v.value)
            .collect(Collectors.toSet()));

    APItemID(long val) {
        this(val, false, true);
    }

    APItemID(long val, boolean isSanity) {
        this(val, isSanity, true);
    }

    APItemID(long val, boolean isSanity, boolean shouldNotify) {
        this(val, isSanity, shouldNotify, true);
    }

    APItemID(long value, boolean isSanity, boolean shouldNotify, boolean isCharacterSpecific) {
        this.value = value;
        this.isSanity = isSanity;
        this.shouldNotify = shouldNotify;
        this.isCharacterSpecific = isCharacterSpecific;
    }

    public static APItemID fromLong(long lookup) {
        if(!isGeneric(lookup))
        {
            lookup = lookup % APContext.getContext().getCharacterManager().getItemWindow();
        }
        return inverseMap.get(lookup);
    }

    public static boolean isGeneric(long lookup)
    {
        return genericIds.contains(lookup);
    }
}
