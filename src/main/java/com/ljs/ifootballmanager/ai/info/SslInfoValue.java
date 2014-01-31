package com.ljs.ifootballmanager.ai.info;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;

/**
 *
 * @author lstephen
 */
public final class SslInfoValue implements InfoValue {

    private static SslInfoValue INSTANCE = new SslInfoValue();

    private SslInfoValue() { }

    public Double get(Player p) {

        Double mult = 930.0 + 25 * p.getSkill(p.getSecondarySkill()) + 15 * p.getSkill(p.getTertiarySkill());

        if (p.getPrimarySkill() == Rating.STOPPING) {
            mult = 850.0;;
        }

        return mult * getValueFactor(p) * getAgeFactor(p);
    }

    private Double getAgeFactor(Player p) {
        switch (p.getAge()) {
            case 17: return .75;
            case 18: return .5;
            case 19: return .25;

            case 21: return .75;

            case 23: return .9;
            case 24: return .8;
            case 25: return .7;
            case 26: return .6;
            case 27: return .5;
            case 28: return .4;
            case 29: return .3;

            case 31: return .75;
            case 32: return .5;
            case 33: return .25;
            case 34: return .1;
            case 35: return .5;

            default:
                return p.getAge() > 35 ? 0.0 : 1.0;
        }
    }


    private Integer getValueFactor(Player p) {
        Integer pskill = (int) Math.round(p.getSkill(p.getPrimarySkill()));

        if (p.getAge() < 20) {
            switch (pskill) {
                case 11: return 100;
                case 12: return 250;
                case 13: return 2500;
                case 14: return 6000;
                case 15: return 9000;
                case 16: return 12000;
                case 17: return 20000;
                case 18: return 30000;
                case 19: return 52500;
                default: return pskill > 19 ? 67500 : 0;
            }
        } else if (p.getAge() < 22) {
            switch (pskill) {
                case 14: return 750;
                case 15: return 1500;
                case 16: return 2500;
                case 17: return 3500;
                case 18: return 6000;
                case 19: return 11000;
                case 20: return 18750;
                case 21: return 22500;
                case 22: return 26250;
                case 23: return 30000;
                default: return pskill > 23 ? 37500 : 0;
            }
        } else if (p.getAge() < 30) {
            switch (pskill) {
                case 15: return 375;
                case 16: return 1000;
                case 17: return 2000;
                case 18: return 2000;
                case 19: return 5000;
                case 20: return 12000;
                case 21: return 15000;
                case 22: return 18000;
                case 23: return 20250;
                case 24: return 22500;
                case 25: return 24750;
                case 26: return 26000;
                case 27: return 28000;
                case 28: return 30000;
                default: return pskill > 28 ? 33000 : 0;
            }
        } else {
            switch (pskill) {
                case 17: return 500;
                case 18: return 750;
                case 19: return 1000;
                case 20: return 1500;
                case 21: return 2250;
                case 22: return 3100;
                case 23: return 3375;
                case 24: return 3650;
                case 25: return 3840;
                case 26: return 4500;
                case 27: return 6000;
                case 28: return 7500;
                default: return pskill > 28 ? 9000 : 0;
            }
        }

    }

    public static SslInfoValue get() {
        return INSTANCE;
    }
}
