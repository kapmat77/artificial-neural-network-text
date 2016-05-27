package org.network.tests;

import org.network.model.Coefficients;
import org.network.model.Node;

import java.util.Map;

/**
 * Created by Kapmat on 2016-05-27.
 */
public class relaksacjaTest {

    public static void main(String[] args) {
        double minusPart = 0;
        double chLevel = 0;
        double alfa = 0.5;
        double beta = 0.7;
        double teta = 1;

        double coeffSum = 1.5;
        double chargingLevel = coeffSum;

        int i=5;
        while(i>0) {
            i--;
            if (chargingLevel<0) {
                minusPart = alfa*chargingLevel+(((alfa-1)*beta*Math.pow(chargingLevel,2))/teta);
                System.out.println("M1.   " + minusPart);
            } else if (chargingLevel>=0 && chargingLevel<teta) {
                minusPart = alfa*chargingLevel+(((1-alfa)*beta*Math.pow(chargingLevel,2))/teta);
                System.out.println("M2.   " + minusPart);
            } else if (chargingLevel>=teta) {
                minusPart = -(beta)*teta;
                System.out.println("M3.   " + minusPart);
            }
            chargingLevel = coeffSum - minusPart;
            System.out.println("\t\t" + coeffSum + " - " + minusPart + " = " + chargingLevel + "\n");
        }
    }

    public static double roundDouble(double oldVar) {
        double newVar = oldVar*10000;
        int helperValue = (int) newVar;
        newVar = ((double) helperValue)/10000;
        return newVar;
    }

}
