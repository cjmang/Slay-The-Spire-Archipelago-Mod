package ArchipelagoMW.game.items;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum APItemID {
    CARD_DRAW(1),
    RARE_CARD_DRAW(2),
    RELIC(3),
    BOSS_RELIC(4),
    ONE_GOLD(5),
    FIVE_GOLD(6),
    PROGRESSIVE_REST(7, true),
    PROGRESSIVE_SMITH(8, true),
    CARD_SLOT(9, true),
    NEUTRAL_CARD_SLOT(10, true),
    RELIC_SLOT(11, true),
    POTION_SLOT(12, true),
    CARD_REMOVE_SLOT(13, true),
    CHAR_UNLOCK(14),
    FIFTEEN_GOLD(15, true),
    THIRTY_GOLD(16, true),
    BOSS_GOLD(17, true),
    ;
    public final long value;
    public final boolean isSanity;
    private static final Map<Long, APItemID> inverseMap = Arrays.stream(APItemID.values())
                    .collect(Collectors.toMap(id -> id.value, Function.identity()));

    APItemID(long val)
    {
        this(val, false);
    }
    APItemID(long val, boolean isSanity)
    {
        this.value = val;
        this.isSanity = isSanity;
    }

    public static APItemID fromLong(long lookup)
    {
        return inverseMap.get(lookup);
    }
}
