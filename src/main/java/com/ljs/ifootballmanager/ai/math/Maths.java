package com.ljs.ifootballmanager.ai.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author lstephen
 */
public final class Maths {

    private Maths() { }

    public static Integer round(BigDecimal d) {
        return d.setScale(0, RoundingMode.HALF_EVEN).intValueExact();
    }

    public static Integer round(Double d) {
        return (int) Math.round(d);
    }


}
