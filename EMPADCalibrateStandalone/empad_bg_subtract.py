#      #######      #         #       ##########          #            #########
#      #            # #     # #       #        #         # #           #        #
#      #            #  #   #  #       #        #        #   #          #         #
#      #######      #   # #   #       ##########       #######         #          #
#      #            #    #    #       #               #       #        #         #
#      #            #         #       #              #         #       #        #
#      ######       #         #       #             #           #      #########

# version 1.4
# @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
# @date: 11/13/2022

import math
import platform
import os
import sys
import struct
from multiprocessing import Pool
import multiprocessing as mp

import configparser

import tqdm
import numpy as np
import psutil
import time

def load_calibration_data(calib_path):

    strFilename1 = calib_path + 'G1A_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    g1A = np.reshape(numbers, (128, 128))

    strFilename1 = calib_path + 'G1B_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    g1B = np.reshape(numbers, (128, 128))

    strFilename1 = calib_path + 'G2A_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    g2A = np.reshape(numbers, (128, 128))

    strFilename1 = calib_path + 'G2B_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    g2B = np.reshape(numbers, (128, 128))

    strFilename1 = calib_path + 'FFA_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    flatfA = np.reshape(numbers, (128, 128))

    strFilename1 = calib_path + 'FFB_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    flatfB = np.reshape(numbers, (128, 128))

    strFilename1 = calib_path + 'B2A_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    offA = np.reshape(numbers, (128, 128))

    strFilename1 = calib_path + 'B2B_prelim.r32'
    with open(strFilename1, "rb") as file:
        numbers = struct.unpack('f' * 128 * 128, file.read(4 * 128 * 128))
    offB = np.reshape(numbers, (128, 128))

    return g1A, g1B, g2A, g2B, flatfA, flatfB, offA, offB


def PAD_AB_bin2data(nVals, g1A, g1B, g2A, g2B, offA, offB, indStart, npFrames_file_name):
    nLen = len(nVals)
    nFrames = round(nLen / 128 / 128)

    ana = np.zeros(nLen, 'uint32')
    for i in range(0, nLen):
        ana[i] = nVals[i] & 16383
    ana = np.reshape(ana, (nFrames, 128, 128))
    ana = np.array(ana, 'float32')

    dig = np.zeros(nLen, 'uint32')
    for i in range(0, nLen):
        dig[i] = nVals[i] & 1073725440
    dig = dig / 16384
    dig = np.array(dig, 'float32')
    dig = np.reshape(dig, (nFrames, 128, 128))

    gn2 = np.zeros(nLen, 'uint32')
    for i in range(0, nLen):
        gn2[i] = nVals[i] & 2147483648
    gn = gn2 / 65536 / 16384 / 2
    gn = np.array(gn, 'float32')
    gn = np.reshape(gn, (nFrames, 128, 128))

    npFrames = np.copy(ana)

    for i in range(0, nFrames, 2):
        Term1 = ana[i, :, :] * (1 - gn[i, :, :])
        Term1 = np.array(Term1, 'float32')
        Term2 = g1A[:, :] * (ana[i, :, :] - offA[:, :]) * gn[i, :, :]
        Term2 = np.array(Term2, 'float32')
        Term3 = g2A[:, :] * dig[i, :, :]
        Term3 = np.array(Term3, 'float32')
        npFrames[i, :, :] = Term1 + Term2 + Term3

    for i in range(1, nFrames, 2):
        Term1 = ana[i, :, :] * (1 - gn[i, :, :])
        Term1 = np.array(Term1, 'float32')
        Term2 = g1B[:, :] * (ana[i, :, :] - offB[:, :]) * gn[i, :, :]
        Term2 = np.array(Term2, 'float32')
        Term3 = g2B[:, :] * dig[i, :, :]
        Term3 = np.array(Term3, 'float32')
        npFrames[i, :, :] = Term1 + Term2 + Term3

    npFrames_total = np.load(npFrames_file_name, mmap_mode='r+')
    TotFrame = npFrames.shape[0]
    for j in range(0, TotFrame):
        npFrames_total[indStart] = npFrames[j]
        indStart += 1



def combine_concatenated_EMPAD2_AB_big(max_data_load, rawfile, g1A, g1B, g2A, g2B, offA, offB):
    ## Read data section

    fid = open(rawfile, 'rb')
    fid.seek(0, os.SEEK_END)
    nFileLen = fid.tell()
    fid.seek(0, 0)

    # k in range [1..4] TODO
    # max data load is equal to the available ram. K is the parameter that we use to control the balance between the number of processes and
    # the size of the chunk of the file.
    # k = 3
    # chunk_size_power = min(max_data_load - k, int(math.log(nFileLen, 2)))
    # chunk_size = 2 ** (chunk_size_power - k)

    chunk_size_power = min(int(max_data_load), int(math.log(nFileLen, 2)))
    chunk_size = 2 ** chunk_size_power

    max_number_of_process = mp.cpu_count()

    ## OutPut: Init Process
    nLenVals = round(nFileLen / 4)
    nFrames = round(nLenVals / 128 / 128)
    npFrames = np.zeros((nFrames, 128, 128), dtype='float32')
    npFrames_file_name = rawfile + '_npFrames_total'
    with open(npFrames_file_name, 'wb') as f:
        np.save(f, npFrames)

    Nt = int(nFileLen / chunk_size)
    last_nLenVals_i = 0

    # The method PAD_AB_bin2data process every 128 * 128 array of data (starting from the beginning) separately. One can use this feature to apply multiprocessing.
    # We will divide the file into smaller chunks each of them are dividend of 128 * 128 and then we apply PAD_AB_bin2data on a different process.
    with Pool(max_number_of_process, maxtasksperchild=1) as pool:
        for i in tqdm.tqdm(range(0, Nt)):
            nLenVals_i = round(chunk_size / 4)
            # On each step we load a new chunk of the file and pass it to a new process.
            nVals_i = struct.unpack('I' * nLenVals_i, fid.read(chunk_size))
            indStart = i * round(last_nLenVals_i / 128 / 128)
            last_nLenVals_i = nLenVals_i
            # p = Process(target=PAD_AB_bin2data,
            #             args=(nVals_i, g1A, g1B, g2A, g2B, offA, offB, indStart, npFrames_file_name))
            pool.apply_async(PAD_AB_bin2data,
                             args=(nVals_i, g1A, g1B, g2A, g2B, offA, offB, indStart, npFrames_file_name))
        pool.close()
        print("Please wait until all processes are completed.")
        # Finally, we wait for each process to be finished, and then we use the result file for the next part.
        # Note that we cannot move on until all processes are finished, because the output is not ready.
        pool.join()

    fid.close()
    f.close()

    npFrames = np.load(rawfile + '_npFrames_total', mmap_mode='r+')
    return npFrames, nFrames

def debounce_f(npMat, wide, w2):
    range1 = -200 - (wide / 2)
    range2 = 220 - (wide / 2)

    nEdges = np.arange(range1, range2 + wide, wide)
    histVal, histEdge = np.histogram(npMat, bins=nEdges, range=(range1, range2))

    histMaxArg = np.argmax(histVal)
    histMaxVal = histVal[histMaxArg]
    nNumPoint = 2 * w2 + 1

    if (histMaxVal > 40):

        wVal = range(-w2, w2 + 1)
        nInd1 = max(histMaxArg - w2, 0)
        nInd2 = min(histMaxArg + w2 + 1, len(histVal))
        wVal2 = range(nInd1, nInd2)
        CurrentHist = histVal[nInd1:nInd2]

        sum_y = sum(CurrentHist)
        sum_xy = sum(wVal * CurrentHist)
        sum_x2y = sum(np.power(wVal, 2) * CurrentHist)
        sum_x2 = sum(np.power(wVal, 2))
        sum_x4 = sum(np.power(wVal, 4))

        bVal = sum_xy / sum_x2
        aVal = (nNumPoint * sum_x2y - sum_x2 * sum_y) / (nNumPoint * sum_x4 - sum_x2 * sum_x2)
        if abs(aVal) > 0.0001:
            comx = -bVal / (2 * aVal)
        else:
            comx = 0

        offset = histEdge[histMaxArg] + (wide / 2) + (comx * wide)
        if abs(offset) > 200:
            offset = 0
    else:
        offset = 0

    npNewMat = npMat - offset
    return npNewMat

def combine_from_concat_EMPAD2_AB_big(max_data_load, raw_file, backgd_file, g1A, g1B, g2A, g2B, offA, offB, flatfA, flatfB):
    npFramesBack, nFramesBack = combine_concatenated_EMPAD2_AB_big(max_data_load, backgd_file, g1A, g1B, g2A, g2B, offA, offB)
    bkgodata = np.mean(npFramesBack[range(0, nFramesBack, 2), :, :], axis=0)
    if (nFramesBack > 1):
        bkgedata = np.mean(npFramesBack[range(1, nFramesBack, 2), :, :], axis=0)
    else:
        bkgedata = np.zeros(bkgodata.shape)

    npFrames, nFrames = combine_concatenated_EMPAD2_AB_big(max_data_load, raw_file, g1A, g1B, g2A, g2B, offA, offB)

    with open('Combined_npFrames', 'wb') as f:
        np.save(f, npFrames)

    npFrames = np.load('Combined_npFrames', mmap_mode='r+')
    for i in tqdm.tqdm(range(0, nFrames, 2)):
        npFrames[i, :, :] -= bkgodata
    for i in tqdm.tqdm(range(1, nFrames, 2)):
        npFrames[i, :, :] -= bkgedata

    for i in tqdm.tqdm(range(0, nFrames)):
        npFrames[i, :, :] = debounce_f(npFrames[i, :, :], 10, 3)

    for i in tqdm.tqdm(range(0, nFrames, 2)):
        npFrames[i, :, :] *= flatfA
    for i in tqdm.tqdm(range(1, nFrames, 2)):
        npFrames[i, :, :] *= flatfB
    return npFrames


def generate_out_put(out_put, out_put_path, out_put_file_name):
    file_path = out_put_path + out_put_file_name
    with open(file_path, "wb") as file:
        file.write(out_put)
        file.flush()
        file.close()
        print(out_put_file_name + ' was generated into directory: ' + out_put_path + ' successfully.')

def detect_config(config_file: str):
    config = configparser.ConfigParser()
    config.read(config_file)

    slash = ''
    if platform.system() in ['linux', 'linux2', 'SunOS', 'Darwin']:
        slash = '/'
    elif platform.system() in ['win32', 'Windows']:
        slash = '\\'
    else:
        print("The program could not detect the OS. Please talk to your administrator.")
        exit()

    raw_dir_path = config['raw']['raw_dir_path']
    if not raw_dir_path.endswith(slash):
        raw_dir_path += slash
    if not os.path.exists(raw_dir_path):
        print('The raw directory: ' + raw_dir_path + ' does not exist or is not accessible!')
        exit()
    files = []
    for i in os.listdir(raw_dir_path):
        if os.path.isfile(os.path.join(raw_dir_path, i)) and 'scan_x' in i:
            files.append(i)
    if len(files) == 0:
        print('The raw file does not exist or is not accessible!')
        exit()

    raw_file = raw_dir_path + files[0]

    bkgd_path = config['raw']['bkgd_dir_path']
    if not bkgd_path.endswith(slash):
        bkgd_path += slash
    if bkgd_path == None or bkgd_path == '':
        bkgd_path = raw_dir_path + "_bkgd"
    if not os.path.exists(bkgd_path):
        print('The background directory: ' + bkgd_path + ' does not exist or is not accessible!')
        exit()
    files = []
    for i in os.listdir(bkgd_path):
        if os.path.isfile(os.path.join(bkgd_path, i)) and 'scan_x' in i:
            files.append(i)
    if len(files) == 0:
        print('No scan file')
        exit()
    backgd_file = bkgd_path + files[0]

    calib_path = config['mask']['calib_path']
    if not calib_path.endswith(slash):
        calib_path += slash
    if not os.path.exists(calib_path):
        print('The mask directory: ' + calib_path + ' does not exist or is not accessible!')
        exit()

    mask_files = ['G1A_prelim.r32', 'G1B_prelim.r32', 'G2A_prelim.r32', 'G2B_prelim.r32',
                  'FFA_prelim.r32', 'FFB_prelim.r32', 'B2A_prelim.r32', 'B2B_prelim.r32']

    for mask in mask_files:
        if not os.path.exists(calib_path + mask):
            print('The mask file: ' + mask + ' does not exist or is not accessible!')
            exit()

    out_put_path = config['out']['out_put_path']
    if not out_put_path.endswith(slash):
        out_put_path += slash
    if not os.path.exists(calib_path):
        print('The output directory: ' + out_put_path + ' does not exist or is not accessible!')
        exit()

    out_put_file_name = config['out']['out_put_file_name']

    return raw_file, backgd_file, calib_path, out_put_file_name, out_put_path

def revome_temp_frames(raw_file: str, backgd_file: str):
    try:
        if os.path.exists(raw_file + '_npFrames_total'):
            os.remove(raw_file + '_npFrames_total')
        if os.path.exists(backgd_file + '_npFrames_total'):
            os.remove(backgd_file + '_npFrames_total')

        if os.path.exists('Combined_npFrames'):
            os.remove("Combined_npFrames")
        return True
    except:
        print("The system is not able to delete temporary frames!")
        return False

######### Main
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("please enter the config path!")
        exit()

    args = sys.argv[1:]
    config_path = args[0]
    if not os.path.exists(config_path):
        print('The configuration file: ' + config_path + ' does not exist or is not accessible!')
        exit()

    start_time = time.time()
    raw_file, backgd_file, calib_path, out_put_file_name, out_put_path = detect_config(config_path)

    if not revome_temp_frames(raw_file, backgd_file):
        print("The process cannot be continued.")
        exit()

    available = psutil.virtual_memory().available
    print("Available memory: " + str(available / 1000000000) + " GB")
    max_data_load = round(math.log(available, 2)) - 10

    print("Please wait for the processes to run.")

    g1A, g1B, g2A, g2B, flatfA, flatfB, offA, offB = load_calibration_data(calib_path)
    cbed = combine_from_concat_EMPAD2_AB_big(max_data_load, raw_file, backgd_file, g1A, g1B,
                                             g2A, g2B, offA, offB, flatfA, flatfB)
    print("All the processes are done. Please wait until the final results are stored.")

    generate_out_put(cbed, out_put_path, out_put_file_name)

    del cbed

    if not revome_temp_frames(raw_file, backgd_file):
        print("You need to delete temporary frames manually.")

    print("The total execution process: ", time.time() - start_time)
