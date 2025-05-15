package ArchipelagoMW.game.items;

public enum APItemID {
    CARD_DRAW(1),
    RARE_CARD_DRAW(2),
    RELIC(3),
    BOSS_RELIC(4),
    ONE_GOLD(5),
    FIVE_GOLD(6),
    PROGRESSIVE_REST(7),
    PROGRESSIVE_SMITH(8),
    CARD_SLOT(9),
    NEUTRAL_CARD_SLOT(10),
    RELIC_SLOT(11),
    POTION_SLOT(12),
    CARD_REMOVE_SLOT(13);
    public final long value;

    private APItemID(long val)
    {
        this.value = val;
    }
}
