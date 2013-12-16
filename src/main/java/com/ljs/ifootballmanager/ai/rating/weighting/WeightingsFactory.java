package com.ljs.ifootballmanager.ai.rating.weighting;

import com.ljs.ifootballmanager.ai.rating.Weightings;

/**
 *
 * @author lstephen
 */
public final class WeightingsFactory {

    private WeightingsFactory() { }

    public static Weightings pbemff() {
        return Pbemff.get();
    }

    public static Weightings eliteFootballLeague() {
        return EliteFootballLeague.get();
    }

    public static Weightings ssl() {
        return Ssl.get();
    }

}
