package ua.varus.iclock.utils;

import com.zkteco.utils.SecurityUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ZKCommand {
    public static final int[] PACKET_START = new int[]{80, 80, 130, 125};

    public static int[] getPacket(CommandCodeEnum commandCode, int sessionId, int replyNumber, int[] data) {
        int[] payloadForChecksum = new int[6 + (data == null ? 0 : data.length)];
        int[] finalPayload = new int[8 + (data == null ? 0 : data.length)];
        byte[] commandBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(commandCode.getCode()).array();
        byte[] sessionIdBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sessionId).array();
        byte[] replyNumberBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(replyNumber).array();
        payloadForChecksum[0] = commandBytes[0] & 255;
        payloadForChecksum[1] = commandBytes[1] & 255;
        finalPayload[0] = commandBytes[0] & 255;
        finalPayload[1] = commandBytes[1] & 255;
        payloadForChecksum[2] = sessionIdBytes[0] & 255;
        payloadForChecksum[3] = sessionIdBytes[1] & 255;
        finalPayload[4] = sessionIdBytes[0] & 255;
        finalPayload[5] = sessionIdBytes[1] & 255;
        payloadForChecksum[4] = replyNumberBytes[0] & 255;
        payloadForChecksum[5] = replyNumberBytes[1] & 255;
        finalPayload[6] = replyNumberBytes[0] & 255;
        finalPayload[7] = replyNumberBytes[1] & 255;
        if (data != null) {
            System.arraycopy(data, 0, payloadForChecksum, 6, data.length);
            System.arraycopy(data, 0, finalPayload, 8, data.length);
        }

        int checksum = SecurityUtils.calculateChecksum(payloadForChecksum);
        byte[] checksumBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(checksum).array();
        finalPayload[2] = checksumBytes[0] & 255;
        finalPayload[3] = checksumBytes[1] & 255;
        return finalPayload;
    }

    public static int[] getPacketByte(CommandCodeEnum commandCode, int sessionId, int replyNumber, byte[] data) {
        int[] payloadForChecksum = new int[6 + (data == null ? 0 : data.length)];
        int[] finalPayload = new int[8 + (data == null ? 0 : data.length)];
        byte[] commandBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(commandCode.getCode()).array();
        byte[] sessionIdBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sessionId).array();
        byte[] replyNumberBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(replyNumber).array();
        payloadForChecksum[0] = commandBytes[0] & 255;
        payloadForChecksum[1] = commandBytes[1] & 255;
        finalPayload[0] = commandBytes[0] & 255;
        finalPayload[1] = commandBytes[1] & 255;
        payloadForChecksum[2] = sessionIdBytes[0] & 255;
        payloadForChecksum[3] = sessionIdBytes[1] & 255;
        finalPayload[4] = sessionIdBytes[0] & 255;
        finalPayload[5] = sessionIdBytes[1] & 255;
        payloadForChecksum[4] = replyNumberBytes[0] & 255;
        payloadForChecksum[5] = replyNumberBytes[1] & 255;
        finalPayload[6] = replyNumberBytes[0] & 255;
        finalPayload[7] = replyNumberBytes[1] & 255;
        if (data != null) {
            for(int i = 0; i < data.length; ++i) {
                payloadForChecksum[6 + i] = data[i] & 255;
                finalPayload[8 + i] = data[i] & 255;
            }
        }

        int checksum = SecurityUtils.calculateChecksum(payloadForChecksum);
        byte[] checksumBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(checksum).array();
        finalPayload[2] = checksumBytes[0] & 255;
        finalPayload[3] = checksumBytes[1] & 255;
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(finalPayload.length).array();
        return finalPayload;
    }
}