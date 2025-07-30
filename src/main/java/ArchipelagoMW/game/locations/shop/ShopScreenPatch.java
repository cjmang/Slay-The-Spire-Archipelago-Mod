package ArchipelagoMW.game.locations.shop;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.ShopManager;
import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.interfaces.PostCreateShopPotionSubscriber;
import basemod.interfaces.PostCreateShopRelicSubscriber;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.Expectation;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.shop.OnSaleTag;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;
import java.util.List;

@SpireInitializer
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
    public static class InitPatch {

        @SpirePrefixPatch
        public static void interceptCards(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards)
        {
            ShopManager shopManager = APContext.getContext().getShopManager();
            shopManager.initializeShop();
            shopManager.mangleCards(coloredCards, __instance);
        }

        @SpirePostfixPatch
        public static void interceptColorlessCards(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards, OnSaleTag ___saleTag)
        {
            // Mangling neutral cards here because of downfall
            ShopManager shopManager = APContext.getContext().getShopManager();
            shopManager.mangleNeutralCards(colorlessCards, __instance);
            ReflectionHacks.privateMethod(ShopScreen.class, "setStartingCardPositions").invoke(__instance);

            for(AbstractCard c : coloredCards)
            {
                if(!(c instanceof APShopItem))
                {
                    continue;
                }
                shopManager.setPrice(c, 1.0F);
                if(___saleTag.card == c)
                {
                    c.price /= 2;
                }
            }

            for(AbstractCard c : colorlessCards)
            {
                if(!(c instanceof APShopItem))
                {
                    continue;
                }
                shopManager.setPrice(c, 1.2F);
                if(___saleTag.card == c)
                {
                    c.price /= 2;
                }
            }

        }



    }

    @SpirePatch(clz = ShopScreen.class, method="open")
    public static class OpenPatch
    {
        @SpirePostfixPatch
        public static void unsetCardRemove(ShopScreen __instance)
        {
            APClient.logger.info("Card remove available: {}", APContext.getContext().getShopManager().isCardRemoveAvailable());
            if(!APContext.getContext().getShopManager().isCardRemoveAvailable())
            {
                __instance.purgeAvailable = false;
            }
        }
    }

    @SpirePatch(clz=ShopScreen.class, method="initCards")
    public static class InitCardsPatch
    {


        @SpireInstrumentPatch
        public static ExprEditor fixSaleTag()
        {
            return new ExprEditor() {


                @Override
                public void edit(MethodCall method) throws CannotCompileException
                {
                    if(method.getClassName().equals("com.megacrit.cardcrawl.random.Random") && method.getMethodName().equals("random") && method.getSignature().equals("(II)I"))
                    {
                        // original method wasn't dynamic based on the size of the coloredCards list, so it threw
                        // an exception when it went out of range
                        method.replace("{ $2 = this.coloredCards.size() - 1; $_ = $proceed($$); }");
                    }
                }
            };
        }

        @SpireInsertPatch(locator= Locator.class)
        public static SpireReturn<Void> fixSaleTag(ShopScreen __instance, ArrayList<AbstractCard> ___coloredCards)
        {
            if(___coloredCards.isEmpty())
            {
                // This call will get circumvented otherwise
                ReflectionHacks.privateMethod(ShopScreen.class, "setStartingCardPositions").invoke(__instance);
                ReflectionHacks.setPrivate(__instance, ShopScreen.class, "saleTag", new OnSaleTag(null));
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        public static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "get");
                List<Matcher> intermediateMatchers = new ArrayList<>();
                for(int i = 0; i < 4; i++)
                {
                    intermediateMatchers.add(new Matcher.MethodCallMatcher(ArrayList.class, "get"));
                }
                return LineFinder.findInOrder(ctBehavior, intermediateMatchers, finalMatcher);
            }
        }

//        @SpirePostfixPatch
//        public static void changeAPPrice(ShopScreen __instance, ArrayList<AbstractCard> ___coloredCards, ArrayList<AbstractCard> ___colorlessCards, OnSaleTag ___saleTag)
//        {
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
                APShopItem fake = (APShopItem) card;
                APContext.getContext().getShopManager().purchaseItem(fake);
                // Downfall breaks without this.
                ReflectionHacks.setPrivate(__instance, FastCardObtainEffect.class, "card", card);
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

    }

    @SpirePatch(clz = ShopScreen.class, method="updateControllerInput")
    public static class FixControllerBug
    {
        // Vanilla has a game crash here, that probably wouldn't happen in most circumstances.
        @SpireInsertPatch(rloc=1126-811)
        public static SpireReturn<Void> fixBug(ShopScreen __instance, ArrayList<AbstractCard> ___colorlessCards)
        {
            if(!___colorlessCards.isEmpty())
            {
                Gdx.input.setCursorPosition(
                        (int)(___colorlessCards.get(___colorlessCards.size() - 1)).hb.cX, Settings.HEIGHT -
                                (int)(___colorlessCards.get(___colorlessCards.size() - 1)).hb.cY);
            }
            return SpireReturn.Return();
        }

    }

//    // TODO: relics should be handled in the AbstractRelic class instantObtain method
//    @SpirePatch(clz=AbstractPlayer.class, method="obtainPotion", paramtypez = AbstractPotion.class)
//    public static class ObtainPotionPatch
//    {
//
//        @SpirePrefixPatch
//        public static SpireReturn<Boolean> fakePotion(AbstractPlayer __instance, AbstractPotion potionToObtain)
//        {
//            if(potionToObtain instanceof APShopItem)
//            {
//                APFakePotion fake = (APFakePotion) potionToObtain;
//                APContext.getContext().getLocationManager().checkLocation(fake.getLocationId());
//                return SpireReturn.Return(true);
//            }
//            return SpireReturn.Continue();
//        }
//    }

}
