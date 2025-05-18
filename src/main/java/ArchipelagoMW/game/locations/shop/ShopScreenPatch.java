package ArchipelagoMW.game.locations.shop;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.ShopManager;
import ArchipelagoMW.mod.Archipelago;
import basemod.BaseMod;
import basemod.interfaces.PostCreateShopPotionSubscriber;
import basemod.interfaces.PostCreateShopRelicSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.shop.OnSaleTag;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CannotCompileException;
import javassist.bytecode.SignatureAttribute;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;

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
            shopManager.mangleNeutralCards(colorlessCards, __instance);
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
//                    Archipelago.logger.info(method.getClassName());
//                    Archipelago.logger.info(method.getMethodName());
//                    Archipelago.logger.info(method.getSignature());
                    if(method.getClassName().equals("com.megacrit.cardcrawl.random.Random") && method.getMethodName().equals("random") && method.getSignature().equals("(II)I"))
                    {
                        Archipelago.logger.info(method.getSignature());
                        method.replace("{ $2 = this.coloredCards.size() - 1; $_ = $proceed($$); }");
                    }
                }
            };
        }

        @SpirePostfixPatch
        public static void changeAPPrice(ShopScreen __instance, ArrayList<AbstractCard> ___coloredCards, ArrayList<AbstractCard> ___colorlessCards, OnSaleTag ___saleTag)
        {
            ShopManager shopManager = APContext.getContext().getShopManager();
            for(AbstractCard c : ___coloredCards)
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

            for(AbstractCard c : ___colorlessCards)
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
                APContext.getContext().getLocationManager().checkLocation(fake.getLocationId());
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
                APFakePotion fake = (APFakePotion) potionToObtain;
                APContext.getContext().getLocationManager().checkLocation(fake.getLocationId());
                return SpireReturn.Return(true);
            }
            return SpireReturn.Continue();
        }
    }

}
