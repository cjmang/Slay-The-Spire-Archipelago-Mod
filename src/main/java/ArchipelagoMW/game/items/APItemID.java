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
    ;
    public final long value;
    public final boolean isSanity;
    public final boolean shouldNotify;
    private static final Map<Long, APItemID> inverseMap = Arrays.stream(APItemID.values())
                    .collect(Collectors.toMap(id -> id.value, Function.identity()));

    APItemID(long val)
    {
        this(val, false, true);
    }
    APItemID(long val, boolean isSanity)
    {
        this(val, isSanity, true);
    }
    APItemID(long val, boolean isSanity, boolean shouldNotify)
    {
        this.value = val;
        this.isSanity = isSanity;
        this.shouldNotify = shouldNotify;
    }

    public static APItemID fromLong(long lookup)
    {
        return inverseMap.get(lookup);
    }
}
