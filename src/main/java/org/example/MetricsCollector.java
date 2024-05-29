package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.example.cong.Configuration.*;

public class MetricsCollector {

    static File f = new File("network_" + System.currentTimeMillis() + ".csv");
//    static File f = new File("./src/main/java/org/example/eva/logs/Pub_"+PRODUCER_NUM+"_Msg_"+MESSAGE_NUM+"_MI_"+MESSAGE_INTERVAL+"_PI_"+PRODUCER_INTERVAL+"_BI_"+System.currentTimeMillis()+ ".csv");
//    static File f = new File("./src/main/java/org/example/eva/logs/Sub_"+CONSUMER_NUM+System.currentTimeMillis()+ ".csv");
    static FileWriter fw;
    static FileWriter fw_sub;
    static BufferedWriter bw;
    static BufferedWriter bw_sub;

    static {
        try {
            fw = new FileWriter(f);
            bw = new BufferedWriter(fw);
            bw.write("event,timestamp\n");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private static void logMetric(String message) {
        try {
            bw.write(message + "\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logTimestamp(String event, long timestamp) {
        logMetric(event + "," + timestamp);
    }

    public void logMiddlewareFilterTimestamp(String clientID, String recvEvent, long recvTimestamp, String sendEvent, long sentTimestamp){
        logMetric(clientID + "," + recvEvent + "," + recvTimestamp + "," + sendEvent + "," + sentTimestamp);
    }

    public void logNoFilterMsgTimestamp(String event, long sentTime, String msgEvent, String msgContent, String geneEvent, long msgGenerateTimestamp){
        logMetric(event + "," + sentTime + "," + msgEvent + "," + msgContent + "," + geneEvent + "," + msgGenerateTimestamp );

    }

    public void logFilterMsgTimestamp(String event, long sentTime, String msgEvent, String msgContent, String geneEvent, long msgGenerateTimestamp, String filterTopicEvent, String filterTopic,  String filterConstraints){
        logMetric(event + "," + sentTime + "," + msgEvent + "," + msgContent + "," + geneEvent + "," + msgGenerateTimestamp + "," + filterTopicEvent + "," + filterTopic + "," + filterConstraints );
    }

    public void logPlanMsg(String event, long sentTime, String msgEvent, String msgContent){
        logMetric(event + "," + sentTime + "," + msgEvent + "," + msgContent );
    }

    public void logNewSubscription(String subscriber, long timestamp) {
        //logMetric("sub," + subscriber + "," + topic + "," + timestamp);
        logMetric("sub," + subscriber + "," + timestamp);
    }
}
