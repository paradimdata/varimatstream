# Introduction:
This program is a Python version of exiting a MATLAB code for Electron Microscope Pixel Array Detector (EMPAD) Calibration. The original MATLAB code implemented based on “Very-High Dynamic Range, 10,000 frames/second Pixel Array Detector for Electron Microscopy” article.
We followed the MATLAB structure to gain Python code. Because some MATLAB functions and types were inconsistent with Python, we included some changes in this conversion process.


# The input contains:
1.	Original (RAW) data
2.	The background file (that operates our trains)
3.	Calibration FILES include several constants that each of them operates as a filter/mask


# Procedure:
1.	Loads and process calibrations: 
2.	Loads the background frames
3.	Loads main frames
4.	Classification Frames (Evens and Odds):
    4.1. Calculates mean of even frames
      4.1.1 Calculates the difference of even frames and mean
    4.2. Calculate mean of odd frames
      4.2.1 Calculate the difference of odd frames and mean
5	Applies debounce function on the (all) those latest calculated frames
6	Multiplies even and odd frames by those matrices flatfA and flatfB respectively
7	Write the final frames into hard disk

# Clarifications: 
1. Since our operations (g1A, g1B, g2A, g2B, … ) are floating point, our process would be the same as floating point as well.
2. We used a float64 to not encounter an overflow problem while running the application.
3. In function PAD_AB_bin2data we apply constant scalar sequences (these constants effect on our data like a mask)
    3.1. ana: apply XOR bit with a constant sequence (16383)
    3.2. dig: apply XOR bit with a constant sequence (1073725440) 
    3.3 gn: apply XOR bit with a constant sequence (2147483648)

4. Applies some mathematical processes for even and odd frames separately. Because the even and odd operations are not the same where g1A and g1B are odd, and g2A, g2B are even operations.

5. Debounce is an important function that generally shows us statistical behavior of our data (like AI and Image Processing). Here we used histogram:
6. Calculate the maximum of histogram to detect the most important feature (histMaxVal). If histMaxVal > 40:
  6.1. Applies some statistical features: summations, weighted, maximums, and minimums of histogram
  6.2. Calculates the final feature (aVal)
  6.3. Detects offset from histogram (finally remove offset and background)

# Challenges/Constraints:
1.	The current Python application is slower than correspondence MATLAB code. Because in general MALAB is much faster than numpylib. To solve the issue, we will split input files and run them in multiple processors. 
2.	The behavior of reshape function in MATLAB is exactly the opposite of the reshape function in numpy library in Python. (Python starts from the last index and so on…). Therefore, to obtain the same results from MATLAB, we either must transpose our vectors/matrices, or we need to consider that into our future processes. Here we did not apply any transpose to our matrices.

3.	In MATLAB code, the scalar operations seem to be useless and it leads gn2 and gn matrices to be zero, most of the time. This is not clear that what’s the advantage of having a zero matrix. Also, 2147483648 is result of multiplying of these three numbers: 65536*16384*2= 2147483648. So, it’s not clear why do we have such extra calculations:
      
gn=single(bitand(hex2dec('80000000'),vals,'uint32'));
      gn = reshape(gn,128,128,[])/65536/16384/2;

4.	Since our inputs are very large (Giga bytes), it’s better to read and process data in multiple sections. Here in MATLAB, all data is read and processed in one step, which slows down the program. We can’t run this MATLAB code with a regular computer.

5.	In MATLAB code, the debounce function 
    5.1. there are two hardcode/constant values:
     5.1.1. the dynamic range of histogram is not based on the data input: [-200-wide/2,220-wide/2]
     5.1.2  the maximum histogram is always compared to a condition greater than 40, and it would better if that condition determined dynamically based on data.
   	5.2. It is not verified that the denominator is opposite to zero: comx=-b/2/a;
