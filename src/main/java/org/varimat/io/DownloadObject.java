package org.varimat.io;

import io.minio.*;
import io.minio.errors.MinioException;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;

public class DownloadObject {
    /** MinioClient.getObject() example. */
    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            /* play.min.io for test and development. */
//            MinioClient minioClient = MinioClient.builder().endpoint("https://play.min.io").build();

            /* Amazon S3: */
             MinioClient minioClient =
                 MinioClient.builder()
                     .endpoint("http://localhost:9000")
                     .credentials("masoud", "Strong#Pass#2022")
                     .build();

            InputStream stream =
                    minioClient.getObject(
                            GetObjectArgs.builder().bucket("empad").object("HW_9.png").build());


            IOUtils.buffer(stream);
            IOUtils.buffer(new FileOutputStream("/Users/amir/IdeaProjects/VariMatStream/HW_9.png"));

            // Read the input stream and print to the console till EOF.
            byte[] buf = new byte[16384];
            int bytesRead;
            while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
                System.out.println(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
            }

//            FileOutputStream outputStream = new FileOutputStream("/Users/amir/IdeaProjects/VariMatStream/image.png");
//            outputStream.write(buf);
//
//            outputStream.flush();
//            outputStream.close();

//            {
//                // Download 'my-objectname' from 'my-bucketname' to 'my-filename'
//                minioClient.downloadObject(
//                        DownloadObjectArgs.builder()
//                                .bucket("empad")
//                                .object("HW_9.png")
//                                .filename("HW_9.png")
//                                .build());
//                System.out.println("my-objectname is successfully downloaded to my-filename");
//            }


//            {
//                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//                keyGen.init(256);
////                ServerSideEncryptionCustomerKey ssec =
////                        ServerSideEncryption. withCustomerKey(keyGen.generateKey());
//
//                // Download SSE-C encrypted 'my-objectname' from 'my-bucketname' to 'my-filename'
//                minioClient.downloadObject(
//                        DownloadObjectArgs.builder()
//                                .bucket("my-bucketname")
//                                .object("my-objectname")
//                                .filename("my-filename")
////                                .ssec(ssec) // Replace with same SSE-C used at the time of upload.
//                                .build());
//                System.out.println("my-objectname is successfully downloaded to my-filename");
//            }
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }
    }
}