package org.varimat.util;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.varimat.com.EMPADStreamCommand;
import org.varimat.com.StreamingSignalProcessing;
import org.varimat.dto.DataFileChunk;
import org.varimat.dto.OperationTO;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.varimat.com.EMPADConstants.OPERATION_EXT;
import static org.varimat.com.EMPADConstants.SUBDIR_STR;

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
         @date: 06/09/2023
*/

public class OperationalUtil {

    private static SAXBuilder sax = new SAXBuilder();

    public static OperationTO getOperation(String xmlContent) {
        OperationTO operationTO = new OperationTO();
        ;
        try {
            Document doc = sax.build(new StringReader(xmlContent));
            Element rootNode = doc.getRootElement();
            String contentName;
            List<Element> rawElement = rootNode.getChildren("rawfile");
            List<Element> sensorElement = rootNode.getChildren("sensor");
            for (Element element : rawElement) {
                List<Content> cons = element.getContent();
                for (Content content : cons) {
                    if (content instanceof Element) {
                        contentName = ((Element) content).getName();
                        switch (contentName) {
                            case "framecount":
                                operationTO.setFramecount(Integer.parseInt(((Element) content).getContent().get(0).getValue()));
                                break;
                            case "datatype":
                                operationTO.setDatatype(((Element) content).getContent().get(0).getValue());
                                break;
                            case "filename":
                                operationTO.setFilename(((Element) content).getContent().get(0).getValue());
                                break;
                        }
                    }
                }
            }

            for (Element element : sensorElement) {
                List<Content> cons = element.getContent();
                for (Content content : cons) {
                    if (content instanceof Element) {
                        Tuple2<Integer, Integer> shape = new Tuple2<>();
                        contentName = ((Element) content).getName();
                        if (contentName.equals("shape")) {
                            String val = ((Element) content).getContent().get(0).getValue().replace(")", "").
                                    replace("(", "").replace(" ", "");
                            String[] parts = val.split(",", 2);
                            shape.f0 = Integer.parseInt(parts[0]);
                            shape.f1 = Integer.parseInt(parts[1]);
                            operationTO.setSensor(shape);
                            return operationTO;
                        }
                    }
                }
            }
            return operationTO;
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String str = "128,126";
        String[] arrOfStr = str.split(",", 2);
        System.out.println(Integer.parseInt(arrOfStr[0]));
        System.out.println(Integer.parseInt(arrOfStr[1]));

        for (String a : arrOfStr)
            System.out.println(a);

        String xmlContent = "<root name=\"3-right_CL1p2m_conv1p5mrad_probeCur40pA_1p8Mx_dwell100us_concat_bg\">\n" +
                "    <timestamp timestamp=\"1624351503.609866\" isoformat=\"2021-06-22T04:45:03.609866\"/>\n" +
                "    <software_version>v2.0_3_71e38ff_behind_dirty</software_version>\n" +
                "    <reconstructions>\n" +
                "        <reconstruction roi_idx=\"0\">\n" +
                "            <roi_name>Roi0</roi_name>\n" +
                "            <filename>Roi0_x64_y64_ri29_ro40.tif</filename>\n" +
                "            <Xc>64.0</Xc>\n" +
                "            <Yc>64.0</Yc>\n" +
                "            <R0>29</R0>\n" +
                "            <R1>40</R1>\n" +
                "        </reconstruction>\n" +
                "        <reconstruction roi_idx=\"1\">\n" +
                "            <roi_name>Roi1</roi_name>\n" +
                "            <filename>Roi1_x64_y64_ri20_ro40.tif</filename>\n" +
                "            <Xc>64</Xc>\n" +
                "            <Yc>64</Yc>\n" +
                "            <R0>20</R0>\n" +
                "            <R1>40</R1>\n" +
                "        </reconstruction>\n" +
                "        <reconstruction roi_idx=\"2\">\n" +
                "            <roi_name>Roi2</roi_name>\n" +
                "            <filename>Roi2_x64_y64_ri20_ro40.tif</filename>\n" +
                "            <Xc>64</Xc>\n" +
                "            <Yc>64</Yc>\n" +
                "            <R0>20</R0>\n" +
                "            <R1>40</R1>\n" +
                "        </reconstruction>\n" +
                "    </reconstructions>\n" +
                "    <scan>\n" +
                "        <type>scan</type>\n" +
                "        <shape>(256, 256)</shape>\n" +
                "        <exposure_time>0.0001</exposure_time>\n" +
                "    </scan>\n" +
                "    <pia_scan>\n" +
                "        <scan_overhead_period>20e-6</scan_overhead_period>\n" +
                "        <exposure_time>0.0001</exposure_time>\n" +
                "        <flyback_dwell_periods>4</flyback_dwell_periods>\n" +
                "    </pia_scan>\n" +
                "    <sensor>\n" +
                "        <type>EMPAD2</type>\n" +
                "        <shape>(128,128)</shape>\n" +
                "        <exposure_time>0.0001</exposure_time>\n" +
                "    </sensor>\n" +
                "    <pdcu>\n" +
                "        <PnLow>76140000</PnLow>\n" +
                "        <PnHigh>65381802</PnHigh>\n" +
                "        <ManufacturerInfo>6538-1802-7614 2020-07-28 09:29:09</ManufacturerInfo>\n" +
                "        <SerialNumber>20-12-A0L-SX3</SerialNumber>\n" +
                "    </pdcu>\n" +
                "    <grabber>\n" +
                "        <type>Kaya Komodo Fiber Frame Grabber</type>\n" +
                "        <SegmentsPerBuffer>260</SegmentsPerBuffer>\n" +
                "        <LeftTrim>4</LeftTrim>\n" +
                "    </grabber>\n" +
                "    <grabber>\n" +
                "        <avg_series_even_offset>0.0</avg_series_even_offset>\n" +
                "        <avg_series_odd_offset>0.0</avg_series_odd_offset>\n" +
                "        <avg_scan_even_offset>7756.468</avg_scan_even_offset>\n" +
                "        <avg_scan_odd_offset>7953.17</avg_scan_odd_offset>\n" +
                "    </grabber>\n" +
                "    <acquisition>\n" +
                "        <start_time>Tue Jun 22 00:44:37 2021</start_time>\n" +
                "        <end_time>Tue Jun 22 00:44:46 2021</end_time>\n" +
                "        <frame_count>66560</frame_count>\n" +
                "        <fps>6833.3761457878945</fps>\n" +
                "    </acquisition>\n" +
                "    <iom_measurements>\n" +
                "        <DetectorsFluScreenLastMeasuredScreenCurrentCalibratedScreenCurrent>0.0\n" +
                "        </DetectorsFluScreenLastMeasuredScreenCurrentCalibratedScreenCurrent>\n" +
                "        <ColumnGetBeamBlanked>True</ColumnGetBeamBlanked>\n" +
                "        <ColumnGetModeColumnOperatingMode>1</ColumnGetModeColumnOperatingMode>\n" +
                "        <ColumnGetScanRotation>-2.8274333882308174</ColumnGetScanRotation>\n" +
                "        <ColumnOpticsSpotSizeIndex>8</ColumnOpticsSpotSizeIndex>\n" +
                "        <ColumnOpticsGetCameraLengthNominalCameraLength>1.197</ColumnOpticsGetCameraLengthNominalCameraLength>\n" +
                "        <ColumnOpticsGetMagnificationNominalMagnification>186966.919</ColumnOpticsGetMagnificationNominalMagnification>\n" +
                "        <ColumnOpticsGetFullScanFieldOfViewx>5.337940092642583e-08</ColumnOpticsGetFullScanFieldOfViewx>\n" +
                "        <ColumnSourceHighVoltageState>1</ColumnSourceHighVoltageState>\n" +
                "        <ColumnSourceHighVoltage>300000.0</ColumnSourceHighVoltage>\n" +
                "        <VacuumState>3</VacuumState>\n" +
                "        <VacuumColumnValvesState>1</VacuumColumnValvesState>\n" +
                "        <calibrated_diffraction_angle>0.00025898078529657475</calibrated_diffraction_angle>\n" +
                "        <calibrated_pixelsize>2.1351760370570334e-12</calibrated_pixelsize>\n" +
                "    </iom_measurements>\n" +
                "    <rawfile>\n" +
                "        <framecount>66560</framecount>\n" +
                "        <datatype>float32</datatype>\n" +
                "        <filename>scan_x256_y256.raw</filename>\n" +
                "    </rawfile>\n" +
                "    <reconstructions/>\n" +
                "</root>";
    }


//    private static void processWorkflow(StreamTableEnvironment tableEnv, DataStream<DataFileChunk> rawDataStream) throws Exception {
//
//        tableEnv.createTemporaryView("EMPAD_TBL", rawDataStream);
//
//        String operation_query = "select " +
//                "    EMPAD_TBL_OPR.chunk_i as opr_chunk_i, \n" +
//                "    EMPAD_TBL_OPR.n_total_chunks as opr_n_total_chunks,\n" +
//                "    EMPAD_TBL_OPR.subdir_str as opr_subdir_str,\n" +
//                "    EMPAD_TBL_OPR.filename as opr_filename, \n" +
//                "    EMPAD_TBL_OPR.data as opr_data,\n" +
//                "    EMPAD_TBL_OPR.chunk_hash_base64 as opr_chunk_hash_base64\n" +
//                "    from EMPAD_TBL EMPAD_TBL_OPR where\n" +
//                "    ((SUBSTR(EMPAD_TBL_OPR.filename, 1, CHAR_LENGTH(EMPAD_TBL_OPR.filename) - " + OPERATION_EXT.length() + ") = EMPAD_TBL_OPR.subdir_str and \n" +
//                "    (" + exclusiveQuery("EMPAD_TBL_OPR.filename") + "))" +
//                ")";
//
//        Table operation_table = tableEnv.sqlQuery(operation_query);
//        DataStream<Row> operation_stream = tableEnv.toDataStream(operation_table).assignTimestampsAndWatermarks(EMPADStreamCommand.IngestionTimeWatermarkStrategy.create());
//
//        String raw_query = "select " +
//                "    EMPAD_TBL_RAW.chunk_i as raw_chunk_i, \n" +
//                "    EMPAD_TBL_RAW.n_total_chunks as raw_n_total_chunks,\n" +
//                "    EMPAD_TBL_RAW.subdir_str as raw_subdir_str,\n" +
//                "    EMPAD_TBL_RAW.filename as raw_filename, \n" +
//                "    EMPAD_TBL_RAW.data as raw_data,\n" +
//                "    EMPAD_TBL_RAW.chunk_hash_base64 as raw_chunk_hash_base64\n" +
//                "    from EMPAD_TBL EMPAD_TBL_RAW where\n" +
//                "    ((EMPAD_TBL_RAW.filename = 'scan_x256_y256.raw') and \n" +
//                "    (" + exclusiveQuery("EMPAD_TBL_RAW.filename") + ")" +
//                ")";
//
//        Table raw_table = tableEnv.sqlQuery(raw_query);
//        DataStream<Row> raw_stream = tableEnv.toDataStream(raw_table).assignTimestampsAndWatermarks(EMPADStreamCommand.IngestionTimeWatermarkStrategy.create());
//
//        raw_stream.keyBy((KeySelector<Row, String>) row -> String.valueOf(row.getField(SUBDIR_STR))).
//                intervalJoin(operation_stream.keyBy((KeySelector<Row, String>)
//                                row -> String.valueOf(row.getField(SUBDIR_STR))).keyBy((KeySelector<Row, String>) row -> String.valueOf(row.getField(SUBDIR_STR))).
//                        reduce(new StreamingSignalProcessing());
////                    between(Time.seconds(-100000000), Time.seconds(100000000)).process(new StreamingSignalProcessing());
//    }
}
