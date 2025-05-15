package ArchipelagoMW.game.locations.shop;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.ShopManager;
import basemod.BaseMod;
import basemod.interfaces.PostCreateShopPotionSubscriber;
import basemod.interfaces.PostCreateShopRelicSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;

//@SpireInitializer
public class ShopScreenPatch {

    public static void initialize()
    {
        BaseMod.subscribe(new PostCreateShopPotionSubscriber() {
            @Override
            public void receiveCreateShopPotions(ArrayList<StorePotion> potions, ShopScreen shopScreen) {
                ShopManager shopManager = APContext.getContext().getShopManager();
                shopManager.manglePotions(potions, shopScreen);
            }
        });
        BaseMod.subscribe(new PostCreateShopRelicSubscriber() {
            @Override
            public void receiveCreateShopRelics(ArrayList<StoreRelic> relics, ShopScreen shopScreen) {
                ShopManager shopManager = APContext.getContext().getShopManager();
                shopManager.mangleRelics(relics, shopScreen);
            }
        });
    }

    @SpirePatch(clz = ShopScreen.class, method="init")
    public static class initPatch {

//        @SpireInsertPatch(locator=Locator.class)
        @SpirePrefixPatch
        public static void interceptCards(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards)
        {
            ShopManager shopManager = APContext.getContext().getShopManager();
            shopManager.mangleCards(coloredCards, __instance);
            shopManager.mangleNeutralCards(coloredCards, __instance);
        }

        @SpirePostfixPatch
        public static void changePrices(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards)
        {
//            SlotData data = APClient.getSlotData();
//            if(data.shopSanity == 0)
//            {
//                return;
//            }
        }

        @SpirePostfixPatch
        public static void unsetCardRemove(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards)
        {
            if(!APContext.getContext().getShopManager().isCardRemoveAvailable())
            {
                __instance.purgeAvailable = false;
            }
        }

//        public static class Locator extends SpireInsertLocator
//        {
//            @Override
//            public int[] Locate(CtBehavior ctBehavior) throws Exception {
//                Matcher matcher = new Matcher.MethodCallMatcher(ShopScreen.class, "setStartingCardPositions");
//                return LineFinder.findInOrder(ctBehavior, matcher);
//            }
//        }
    }

    @SpirePatch(clz = FastCardObtainEffect.class, method=SpirePatch.CONSTRUCTOR)
    public static class PurchaseCardPatch
    {
        @SpirePrefixPatch
        public static SpireReturn<Void> instrumentCall(FastCardObtainEffect __instance, AbstractCard card, float x, float y)
        {
            if(card instanceof APShopItem)
            {
                __instance.isDone = true;
                __instance.duration = 0.0F;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

    }
    // TODO: relics should be handled in the AbstractRelic class instantObtain method
    @SpirePatch(clz=AbstractPlayer.class, method="obtainPotion", paramtypez = AbstractPotion.class)
    public static class ObtainPotionPatch
    {

        @SpirePrefixPatch
        public static SpireReturn<Boolean> fakePotion(AbstractPlayer __instance, AbstractPotion potionToObtain)
        {
            if(potionToObtain instanceof APShopItem)
            {
                return SpireReturn.Return(true);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz=ShopScreen.class, method="purchasePotion")
    public static class PurchasePotionPatch
    {

    }

    @SpirePatch(clz=ShopScreen.class, method="purchaseRelic")
    public static class PurchaseRelicPatch
    {

    }
}
