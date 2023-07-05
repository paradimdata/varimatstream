select EMPAD_TBL_OPR.subdir_str as opr_subdir_str,
       EMPAD_TBL_RAW.chunk_i           as raw_chunk_i,
       EMPAD_TBL_RAW.n_total_chunks    as raw_n_total_chunks,
       EMPAD_TBL_RAW.subdir_str        as raw_subdir_str,
       EMPAD_TBL_RAW.filename          as raw_filename,
       EMPAD_TBL_RAW.data              as raw_data,
       EMPAD_TBL_OPR.filename          as opr_filename
    from EMPAD_TBL EMPAD_TBL_RAW, EMPAD_TBL EMPAD_TBL_OPR
    where (((SUBSTR(EMPAD_TBL_OPR.filename, 1, CHAR_LENGTH (EMPAD_TBL_OPR.filename) - 4) = EMPAD_TBL_OPR.subdir_str) and
            EMPAD_TBL_RAW.filename = 'scan_x256_y256.raw'))
  and
    (EMPAD_TBL_OPR.filename not like '%odd%'
  and EMPAD_TBL_OPR.filename not like '%.tif%'
  and EMPAD_TBL_OPR.filename not like '%.log%'
  and EMPAD_TBL_OPR.filename not like '%stream_operations%'
  and EMPAD_TBL_OPR.filename not like '%even%'))) and EMPAD_TBL_OPR.subdir_str =
 (select subdir_str from EMPAD_TBL where filename = 'scan_x256_y256.raw')
