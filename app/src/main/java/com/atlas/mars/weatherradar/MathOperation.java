package com.atlas.mars.weatherradar;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by mars on 8/12/15.
 */
public class MathOperation {
    public static double round(double d, int prec) {
        return new BigDecimal(d).setScale(prec, RoundingMode.UP).doubleValue();
    }
}
