clear;clc;clearvars

py_id= fopen('/output/scan_512_y512_Py.raw','rb');
Data_python = fread(py_id, 'float32');
fclose(py_id);
Data_python(1:100)

fid_mat = fopen('/output/20GB_large_matlab_Combined_scan_128x128_x512_y512.raw','rb');
Data_mat = fread(fid_mat, 'float32');
fclose(fid_mat);
Data_mat(1:100)

norm(Data_python-Data_mat)/norm(Data_python)