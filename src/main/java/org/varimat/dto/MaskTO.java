package org.varimat.dto;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.3
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 05/25/2023
         @last modified: 07/06/2023
*/

public class MaskTO {
    private final double[][] g1A;
    private final double[][] g1B;
    private final double[][] g2A;
    private final double[][] g2B;
    private final double[][] offA;
    private final double[][] offB;
    private final double[][] flatfA;
    private final double[][] flatfB;

    public MaskTO(double[][] g1A, double[][] g1B, double[][] g2A, double[][] g2B,
                  double[][] offA, double[][] offB, double[][] flatfA, double[][] flatfB) {
        this.g1A = g1A;
        this.g1B = g1B;
        this.g2A = g2A;
        this.g2B = g2B;
        this.offA = offA;
        this.offB = offB;
        this.flatfA = flatfA;
        this.flatfB = flatfB;
    }

    public double[][] getG1A() {
        return g1A;
    }

    public double[][] getG1B() {
        return g1B;
    }

    public double[][] getG2A() {
        return g2A;
    }

    public double[][] getG2B() {
        return g2B;
    }

    public double[][] getOffA() {
        return offA;
    }

    public double[][] getOffB() {
        return offB;
    }

    public double[][] getFlatfA() {
        return flatfA;
    }

    public double[][] getFlatfB() {
        return flatfB;
    }
}
