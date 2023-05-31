from tkinter import filedialog
from tkinter import Tk
import os

nWriteEachTime = 128 * 128

# version 1.0
# @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
# @date: 11/13/2022

def select_file():
    root = Tk()
    root.withdraw()
    file_selected = filedialog.askopenfilename(initialdir=os.getcwd())
    if file_selected == '':
        print('No file selected!')
        exit()
    return file_selected


def generate_new_file_name(strPath):
    strSplit = strPath.rsplit('.', 1)
    strOut = strSplit[0] + '_2.' + strSplit[1]
    return strOut


def get_file_info(strPath):
    f = open(strPath, "rb")
    f.seek(0, os.SEEK_END)
    nFileLen = f.tell()
    return nFileLen


def create_new_file(strPath, strNewFileName, nSplitFromBegin, nWriteEachTime):
    f = open(strPath, "rb")
    f2 = open(strNewFileName, 'wb')
    nWriteLen = 0
    while ((nWriteLen + nWriteEachTime) <= nSplitFromBegin):
        nReadLen = nSplitFromBegin - nWriteLen
        nReadLen = min(nReadLen, nWriteEachTime)
        Content = f.read(nReadLen)
        f2.write(Content)
        nWriteLen += nReadLen
    nReadLen = nSplitFromBegin - nWriteLen
    if (nReadLen > 0):
        Content = f.read(nReadLen)
        f2.write(Content)
        nWriteLen += nReadLen
    f.close()
    f2.close()


nSplitFromBegin = 128 * 128 * 4 * 16
strPath = select_file()
strNewFileName = generate_new_file_name(strPath)
nFileLen = get_file_info(strPath)
if (nSplitFromBegin > nFileLen):
    print('Small file length error')
    exit()
create_new_file(strPath, strNewFileName, nSplitFromBegin, nWriteEachTime)
