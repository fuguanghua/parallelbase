package me.fuguanghua.concurrentcollection.blockingqueue.estimation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class PercentileFile {

    public static void main(final String[] arg) throws IOException {


        for(int i=0; i<arg.length; i++) {
            final String fileName = arg[i];
            final Percentile pFile = new Percentile();
            final BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while((line = br.readLine()) != null) {
                final float sample = Float.parseFloat(line.trim());
                pFile.add(sample);
            }
            br.close();

            Percentile.print(System.out, fileName, pFile);
        }

    }

}
