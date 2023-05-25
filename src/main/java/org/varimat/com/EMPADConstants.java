package org.varimat.com;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.2
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 05/23/2023
*/

public final class EMPADConstants {
    public final static int ERR_COMMAND = -1;

    /**** Map constants***/
    public final static int ROW_CHUNK = 0;
    public final static int ROW_IMAGE_TOTAL_CHUNK = 1;
    public final static int ROW_IMAGE_NAME = 2;
    public final static int ROW_IMAGE_DATA_CHUNK = 3;
    public final static int ROW_NOISE_TOTAL_CHUNK = 5;
    public final static int ROW_NOISE_NAME = 6;
    public final static int ROW_NOISE_DATA_CHUNK = 7;


    /**** MessageUnpacker constants***/
    public final static int MSG_FILE_NAME = 0;
    public final static int MSG_CHUNK_HASH = 2;
    public final static int MSG_CHUNK_I = 4;
    public final static int MSG_N_TOTAL_CHUNKS = 5;
    public final static int MSG_SUBDIR_STR = 6;
    public final static int MSG_FILENAME_APPEND = 7;
    public final static int MSG_DATA = 8;

    public final static int UUID_LEN = 40;

}