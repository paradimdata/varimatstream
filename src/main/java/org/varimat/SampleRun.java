package org.varimat;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class SampleRun {
    public static void main(String[] args) {
        File[] listOfFiles = new File("/Users/amir/IdeaProjects/VariMatStream/target/dependency-jars").listFiles();
        StringBuilder str = new StringBuilder();
        for (File file: listOfFiles) {
            str = new StringBuilder("COPY target/dependency-jars/" + file.getName() + " /opt/flink/lib/dependency-jars/" + file.getName());
            System.out.println(str);
        }

    }
}
