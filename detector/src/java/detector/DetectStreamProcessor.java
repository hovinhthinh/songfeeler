package detector;

import org.apache.log4j.Logger;

import java.io.DataInputStream;

public class DetectStreamProcessor implements Runnable {
    private static final Logger LOG = Logger.getLogger(DetectStreamProcessor.class);
    private DataInputStream in;
    private DetectStream detectStream;


    public DetectStreamProcessor(DetectStream detectStream) throws Exception {
        in = detectStream.in;
        this.detectStream = detectStream;
    }

    @Override
    public void run() {
        while (!detectStream.detected) {
            try {
                /* 1-based tryMatchTime index; 0 is break */
                byte p = in.readByte();
                if (p == 0) {
                    break;
                }
                detectStream.increaseSignalCount();
                --p;
                int hashCode = in.readInt();
                short offset = in.readShort();
                detectStream.hash[p][detectStream.fingerprintCursor[p]] = hashCode;
                detectStream.offset[p][detectStream.fingerprintCursor[p]] = offset;
                ++detectStream.fingerprintCursor[p];
                synchronized (detectStream.newDataNotifyObject) {
                    detectStream.newDataNotifyObject.notifyAll();
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        detectStream.streaming = false;
        LOG.info("stream interrupted");
        synchronized (detectStream.newDataNotifyObject) {
            detectStream.newDataNotifyObject.notifyAll();
        }
    }
}