package org.paradim.empad.dto;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.6
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 05/25/2023
         @last modified: 07/06/2023
*/

public class MaskTO {
    private final float[][] g1A;
    private final float[][] g1B;
    private final float[][] g2A;
    private final float[][] g2B;
    private final float[][] offA;
    private final float[][] offB;
    private final float[][] flatfA;
    private final float[][] flatfB;

    public MaskTO(float[][] g1A, float[][] g1B, float[][] g2A, float[][] g2B,
                  float[][] offA, float[][] offB, float[][] flatfA, float[][] flatfB) {
        this.g1A = g1A;
        this.g1B = g1B;
        this.g2A = g2A;
        this.g2B = g2B;
        this.offA = offA;
        this.offB = offB;
        this.flatfA = flatfA;
        this.flatfB = flatfB;
    }

    public float[][] getG1A() {
        return g1A;
    }

    public float[][] getG1B() {
        return g1B;
    }

    public float[][] getG2A() {
        return g2A;
    }

    public float[][] getG2B() {
        return g2B;
    }

    public float[][] getOffA() {
        return offA;
    }

    public float[][] getOffB() {
        return offB;
    }

    public float[][] getFlatfA() {
        return flatfA;
    }

    public float[][] getFlatfB() {
        return flatfB;
    }
}