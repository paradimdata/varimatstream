This program is cythonize version of [EMPAD Background Subtract](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad_bg_subtract.py). Also, the program has a higher speed than the Python version, and its execution speed for very large data (> 20GB) is even faster than [original MATLAB's script](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/big%20empad%20data/bgSubtract.m).

## Installation:
To install the program, you will need to install the following libraries:
1) `pip install Cython --install-option="--no-cython-compile"`
2) `pip install tqdm`
3) `pip install numpy`
4) `pip install psutil`

## [Cythonization](https://cython.readthedocs.io/en/latest/src/tutorial/cython_tutorial.html)
You will need to cythonize [empad.pyx ](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/empad.pyx)to make it executable. Make sure that all these file exist:
[empad.pyx](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/empad.pyx), [setup.py](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/setup.py), and [empad.c](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/empad.c)

From terminal/console type: 
`python setup.py build_ext --inplace`

## Configuration
Open [empad.cfg](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/empad.cfg) and modify the paths for both raw and background files, [calibration directory](https://github.com/paradimdata/pyempadcalibratescript/tree/main/related_data/EMPAD2-calib_oct2020), the output path and output name.
The calibration directory must include all these eight mask files: 
1. [G1A_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G1A_prelim.r32)
2. [G1B_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G1B_prelim.r32)
3. [G2A_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G2A_prelim.r32)
4. [G2B_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/G2B_prelim.r32)
5. [FFA_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/FFA_prelim.r32)
6. [FFB_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/FFB_prelim.r32)
7. [B2A_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/B2A_prelim.r32)
8. [B2B_prelim.r32](https://github.com/paradimdata/pyempadcalibratescript/blob/main/related_data/EMPAD2-calib_oct2020/B2B_prelim.r32)



## Execution
You should run the program by calling `run_empadx` function from [empad.pyx](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/empad.pyx) and need to pass the path of the config file to this function.
You can also simply run [empad_run.py](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/empad_run.py) from terminal: 
`python empad_run.py empad.cfg`

# Runtime program
If you also run the corresponding MATLAB program, you can compare the results and execution speed of the both cython and MATLAB.
At the end, the program will tell you how long it took to run.
As we cythonized the python program it would run faster. This program calculates the number of processes based on the amount of available memory. So, in order to achieve a high speed, it is better not to run unnecessary processes (i.e. opening Google Chrome, etc... ) before and during the execution of the program.

# Output accuracy
I compared the obtained results from the original MATLAB program with both python and chyton programs. Both outputs are highly accurate:
* Python accuracy: 10^-2
* **Cython accuracy (extremely accurate): 4.6582e-06**

This is an example of a MATLAB script to verify the results:
- [[compare_results.m](https://github.com/paradimdata/pyempadcalibratescript/blob/main/EMPADCalibrate/empad-cython/compare_results.m)] 

