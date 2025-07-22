package ua.varus.iclock.utils;

import com.zkteco.terminal.ZKTerminal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zkteco.Enum.AttendanceStateEnum;
import com.zkteco.Enum.AttendanceTypeEnum;
import com.zkteco.Enum.CommandCodeEnum;
import com.zkteco.Enum.CommandReplyCodeEnum;
import com.zkteco.Enum.OnOffenum;
import com.zkteco.Enum.UserRoleEnum;
import com.zkteco.Exception.DeviceNotConnectException;
import com.zkteco.command.events.EventCode;
import com.zkteco.commands.AttendanceRecord;
import com.zkteco.commands.GetTimeReply;
import com.zkteco.commands.SmsInfo;
import com.zkteco.commands.UserInfo;
import com.zkteco.commands.ZKCommand;
import com.zkteco.commands.ZKCommandReply;
import com.zkteco.utils.HexUtils;
import com.zkteco.utils.SecurityUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

public class ZKTerminalNEw {

    private DatagramSocket socket;
    private InetAddress address;
    private final String ip;
    private final int port;
    private int sessionId;
    private int replyNo;

    public ZKTerminalNEw(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ZKCommandReply connect() throws IOException, DeviceNotConnectException {
        if (!this.testPing()) {
            throw new DeviceNotConnectException("Device Not connect...!");
        } else {
            this.sessionId = 0;
            this.replyNo = 0;
            this.socket = new DatagramSocket(this.port);
            this.address = InetAddress.getByName(this.ip);
            int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_CONNECT, this.sessionId, this.replyNo, (int[])null);
            byte[] buf = new byte[toSend.length];
            int index = 0;

            for(int byteToSend : toSend) {
                buf[index++] = (byte)byteToSend;
            }

            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
            this.socket.send(packet);
            ++this.replyNo;
            int[] response = this.readResponse();
            CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
            this.sessionId = response[4] + response[5] * 256;
            int replyId = response[6] + response[7] * 256;
            int[] payloads = new int[response.length - 8];
            System.arraycopy(response, 8, payloads, 0, payloads.length);
            return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
        }
    }

    public boolean testPing() {
        try {
            Process process;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                process = Runtime.getRuntime().exec("ping -n 1 " + this.ip);
            } else {
                process = Runtime.getRuntime().exec("ping -c 1 -W 5 " + this.ip);
            }

            int returnCode = process.waitFor();
            return returnCode == 0;
        } catch (InterruptedException | IOException e) {
            ((Exception)e).printStackTrace();
            return false;
        }
    }

    public void disconnect() throws IOException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_EXIT, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        this.socket.close();
    }

    public void socketClose() {
        if (!this.socket.isClosed()) {
            this.socket.close();
        }

    }

    public ZKCommandReply enableDevice() throws IOException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_ENABLEDEVICE, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply disableDevice() throws IOException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_DISABLEDEVICE, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply connectAuth(int comKey) throws IOException {
        int[] key = SecurityUtils.authKey(comKey, this.sessionId);
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_AUTH, this.sessionId, this.replyNo, key);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply enableRealtime(EventCode... events) throws IOException {
        int allEvents = 0;

        for(EventCode event : events) {
            allEvents |= event.getCode();
        }

        String hex = StringUtils.leftPad(Integer.toHexString(allEvents), 8, "0");
        int[] eventReg = new int[4];

        for(int index = 3; hex.length() > 0; hex = hex.substring(2)) {
            eventReg[index] = (int)Long.parseLong(hex.substring(0, 2), 16);
            --index;
        }

        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_REG_EVENT, this.sessionId, this.replyNo, eventReg);
        byte[] buf = new byte[toSend.length];
        int var16 = 0;

        for(int byteToSend : toSend) {
            buf[var16++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply enableRealtimeAtt() throws IOException, ParseException {
        int allEvents = EventCode.EF_ATTLOG.getCode();
        String hex = StringUtils.leftPad(Integer.toHexString(allEvents), 8, "0");
        int[] eventReg = new int[4];

        for(int index = 3; hex.length() > 0; hex = hex.substring(2)) {
            eventReg[index] = (int)Long.parseLong(hex.substring(0, 2), 16);
            --index;
        }

        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_REG_EVENT, this.sessionId, this.replyNo, eventReg);
        byte[] buf = new byte[toSend.length];
        int var12 = 0;

        for(int byteToSend : toSend) {
            buf[var12++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply Poweroff() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_POWEROFF, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        this.socket.close();
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply restart() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_RESTART, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        this.socket.close();
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public List<AttendanceRecord> getAttendanceRecords() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_ATTLOG_RRQ, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        List<AttendanceRecord> attendanceRecords = new ArrayList();
        StringBuilder attendanceBuffer = new StringBuilder();
        if (replyCode == CommandReplyCodeEnum.CMD_PREPARE_DATA) {
            boolean first = true;

            int lastDataRead;
            do {
                int[] readData = this.readResponse();
                lastDataRead = readData.length;
                String readPacket = HexUtils.bytesToHex(readData);
                attendanceBuffer.append(readPacket.substring(first ? 24 : 16));
                first = false;
            } while(lastDataRead == 1032);
        } else {
            attendanceBuffer.append(HexUtils.bytesToHex(response).substring(24));
        }

        String attendance = attendanceBuffer.toString();

        while(attendance.length() > 0) {
            String record = attendance.substring(0, 80);
            int seq = Integer.valueOf(record.substring(2, 4) + record.substring(0, 2), 16);
            record = record.substring(4);
            String userId = Character.toString((char)Integer.valueOf(record.substring(0, 2), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(2, 4), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(4, 6), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(6, 8), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(8, 10), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(10, 12), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(12, 14), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(14, 16), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(16, 18), 16).intValue());
            record = record.substring(48);
            int method = Integer.valueOf(record.substring(0, 2), 16);
            AttendanceTypeEnum attendanceType = AttendanceTypeEnum.values()[method];
            record = record.substring(2);
            long encDate = (long)Integer.valueOf(record.substring(6, 8), 16) * 16777216L + (long)Integer.valueOf(record.substring(4, 6), 16) * 65536L + (long)Integer.valueOf(record.substring(2, 4), 16) * 256L + (long)Integer.valueOf(record.substring(0, 2), 16);
            Date attendanceDate = HexUtils.extractDate(encDate);
            record = record.substring(8);
            int operation = Integer.valueOf(record.substring(0, 2), 16);
            AttendanceStateEnum attendanceState = AttendanceStateEnum.values()[operation];
            attendance = attendance.substring(80);
            AttendanceRecord attendanceRecord = new AttendanceRecord(seq, userId.trim(), attendanceType, attendanceDate, attendanceState);
            attendanceRecords.add(attendanceRecord);
        }

        return attendanceRecords;
    }

    public List<AttendanceRecord> getAttendanceRecordsForDateRange(String startTime, String endTime) throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_ATTLOG_RRQ, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        List<AttendanceRecord> attendanceRecords = new ArrayList();
        StringBuilder attendanceBuffer = new StringBuilder();
        if (replyCode == CommandReplyCodeEnum.CMD_PREPARE_DATA) {
            boolean first = true;

            int lastDataRead;
            do {
                int[] readData = this.readResponse();
                lastDataRead = readData.length;
                String readPacket = HexUtils.bytesToHex(readData);
                attendanceBuffer.append(readPacket.substring(first ? 24 : 16));
                first = false;
            } while(lastDataRead == 1032);
        } else {
            attendanceBuffer.append(HexUtils.bytesToHex(response).substring(24));
        }

        String attendance = attendanceBuffer.toString();

        while(attendance.length() > 0) {
            String record = attendance.substring(0, 80);
            int seq = Integer.valueOf(record.substring(2, 4) + record.substring(0, 2), 16);
            record = record.substring(4);
            String userId = Character.toString((char)Integer.valueOf(record.substring(0, 2), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(2, 4), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(4, 6), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(6, 8), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(8, 10), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(10, 12), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(12, 14), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(14, 16), 16).intValue())
                    + Character.toString((char)Integer.valueOf(record.substring(16, 18), 16).intValue());
            record = record.substring(48);
            int method = Integer.valueOf(record.substring(0, 2), 16);
            AttendanceTypeEnum attendanceType = AttendanceTypeEnum.values()[method];
            record = record.substring(2);
            long encDate = (long)Integer.valueOf(record.substring(6, 8), 16) * 16777216L + (long)Integer.valueOf(record.substring(4, 6), 16) * 65536L + (long)Integer.valueOf(record.substring(2, 4), 16) * 256L + (long)Integer.valueOf(record.substring(0, 2), 16);
            Date attendanceDate = HexUtils.extractDate(encDate);
            record = record.substring(8);
            int operation = Integer.valueOf(record.substring(0, 2), 16);
            AttendanceStateEnum attendanceState = AttendanceStateEnum.values()[operation];
            attendance = attendance.substring(80);
            AttendanceRecord attendanceRecord = new AttendanceRecord(seq, userId.trim(), attendanceType, attendanceDate, attendanceState);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = dateFormat.parse(startTime);
            Date endDate = dateFormat.parse(endTime);
            if (attendanceDate.after(startDate) && attendanceDate.before(endDate)) {
                attendanceRecords.add(attendanceRecord);
            }
        }

        return attendanceRecords;
    }

    public ZKCommandReply clearAdminData() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_CLEAR_ADMIN, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply clearOpLogData() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_CLEAR_OPLOG, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        this.socket.close();
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply resetDevice() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_CLEAR_DATA, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        this.socket.close();
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply clearAttLogData() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_CLEAR_ATTLOG, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public String IsOnlyRFMachine() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~IsOnlyRFMachine".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getFirmwareVersion() throws IOException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_GET_VERSION, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode != CommandReplyCodeEnum.CMD_ACK_OK) {
            return "";
        } else {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            return responseString.trim();
        }
    }

    public String getProductTime() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~ProductTime".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getDeviceName() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~DeviceName".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getPIN2Width() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~PIN2Width".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getShowState() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~ShowState".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getDeviceIP() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "IPAddress".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getDevicePORT() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "UDPPort".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getCommKey() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "COMKey".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getDeviceId() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "DeviceID".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String iSDHCP() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "DHCP".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getDNS() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "DNS".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String isEnableProxyServer() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "EnableProxyServer".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getProxyServerIP() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "ProxyServerIP".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getProxyServerPort() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "ProxyServerPort".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String isDaylightSavingTime() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "DaylightSavingTime".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getLanguage() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "Language".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String isLockPowerKey() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "LockPowerKey".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String isVoiceOn() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "VoiceOn".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public ZKCommandReply setIPAddress(String ipaddress) throws IOException {
        byte[] ipaddressbyte = ("IPAddress=" + ipaddress).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, ipaddressbyte);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setCommKey(int key) throws IOException {
        byte[] COMKeybyte = ("COMKey=" + key).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, COMKeybyte);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setVoiceOnOff(OnOffenum state) throws IOException {
        byte[] voiceOn = ("VoiceOn=" + state.getOnOffState()).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, voiceOn);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setShowStateOnOff(OnOffenum state) throws IOException {
        byte[] ShowState = ("~ShowState=" + state.getOnOffState()).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, ShowState);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public String getPlatform() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~Platform".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public ZKCommandReply setLockPowerKey(OnOffenum state) throws IOException {
        byte[] LockPowerKey = ("LockPowerKey=" + state.getOnOffState()).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, LockPowerKey);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setDaylightSavingTime(OnOffenum state) throws IOException {
        byte[] DaylightSavingTime = ("DaylightSavingTime=" + state.getOnOffState()).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, DaylightSavingTime);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setProxyServerPort(int devport) throws IOException {
        byte[] ProxyServerPort = ("ProxyServerPort=" + devport).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, ProxyServerPort);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setProxyServerIP(String devIP) throws IOException {
        byte[] ProxyServerIP = ("ProxyServerIP=" + devIP).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, ProxyServerIP);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setEnableProxyServer(OnOffenum state) throws IOException {
        byte[] EnableProxyServer = ("EnableProxyServer=" + state.getOnOffState()).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, EnableProxyServer);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setDNS(String DNS) throws IOException {
        byte[] DNSbyte = ("DNS=" + DNS).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, DNSbyte);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setDHCP(OnOffenum state) throws IOException {
        byte[] DHCPbyte = ("DHCP=" + state.getOnOffState()).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, DHCPbyte);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply setDeviceID(int DeviceID) throws IOException {
        byte[] DeviceIDbyte = ("DeviceID=" + DeviceID).getBytes();
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_WRQ, this.sessionId, this.replyNo, DeviceIDbyte);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public String getSerialNumber() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~SerialNumber".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getMAC() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "MAC".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public String getFaceVersion() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "ZKFaceVersion".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public int getFPVersion() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~ZKFPVersion".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteResponse = new byte[response.length - 8];

            for(int i = 0; i < byteResponse.length; ++i) {
                byteResponse[i] = (byte)response[i + 8];
            }

            String responseString = new String(byteResponse, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                try {
                    return Integer.parseInt(responseParts[1].split("\u0000")[0]);
                } catch (NumberFormatException var11) {
                }
            }
        }

        return 0;
    }

    public String getOEMVendor() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "~OEMVendor".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteArray = new byte[response.length - 8];

            for(int i = 0; i < byteArray.length; ++i) {
                byteArray[i] = (byte)(response[8 + i] & 255);
            }

            String responseString = new String(byteArray, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return responseParts[1].split("\u0000")[0];
            }
        }

        return "";
    }

    public List<UserInfo> getAllUsers() throws IOException, ParseException {
        Integer userCount = this.getDeviceStatus().get("userCount");
        int usercount = Objects.isNull(userCount) ? 0 : userCount;
        if (usercount == 0) {
            return Collections.emptyList();
        } else {
            int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_USERTEMP_RRQ, this.sessionId, this.replyNo, (int[])null);
            byte[] buf = new byte[toSend.length];
            int index = 0;

            for(int byteToSend : toSend) {
                buf[index++] = (byte)byteToSend;
            }

            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
            this.socket.send(packet);
            ++this.replyNo;
            int[] response = this.readResponse();
            CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
            StringBuilder userBuffer = new StringBuilder();
            List<UserInfo> userList = new ArrayList();
            if (replyCode == CommandReplyCodeEnum.CMD_PREPARE_DATA) {
                boolean first = true;

                int lastDataRead;
                do {
                    int[] readData = this.readResponse();
                    lastDataRead = readData.length;
                    String readPacket = HexUtils.bytesToHex(readData);
                    userBuffer.append(readPacket.substring(first ? 24 : 16));
                    first = false;
                } while(lastDataRead == 1032);

                String usersHex = userBuffer.toString();
                byte[] usersData = HexUtils.hexStringToByteArray(usersHex);
                ByteBuffer buffer = ByteBuffer.wrap(usersData);

                while(buffer.remaining() >= 72) {
                    ByteBuffer userBuffer1 = ByteBuffer.allocate(72);
                    buffer.get(userBuffer1.array());
                    UserInfo user = UserInfo.encodeUser(userBuffer1, 72);
                    userList.add(user);
                }
            } else {
                System.out.println("Data Fetch failed or null");
            }

            int replyId = response[6] + response[7] * 256;
            System.out.println(replyId);
            int[] payloads = new int[response.length - 8];
            System.arraycopy(response, 8, payloads, 0, payloads.length);
            return userList;
        }
    }

    public boolean getWorkCode() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_OPTIONS_RRQ, this.sessionId, this.replyNo, "WorkCode".getBytes());
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteResponse = new byte[response.length - 8];

            for(int i = 0; i < byteResponse.length; ++i) {
                byteResponse[i] = (byte)response[i + 8];
            }

            String responseString = new String(byteResponse, StandardCharsets.US_ASCII);
            String[] responseParts = responseString.split("=", 2);
            if (responseParts.length == 2) {
                return "1".equals(responseParts[1].split("\u0000")[0]);
            }
        }

        return false;
    }

    public Map<String, Integer> getDeviceStatus() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_GET_FREE_SIZES, this.sessionId, this.replyNo, (byte[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            byte[] byteResponse = new byte[response.length - 8];

            for(int i = 0; i < byteResponse.length; ++i) {
                byteResponse[i] = (byte)response[i + 8];
            }

            ByteBuffer buffer = ByteBuffer.wrap(byteResponse);
            if (buffer.remaining() >= 92) {
                Map<String, Integer> statusMap = new HashMap();
                statusMap.put("adminCount", Integer.reverseBytes(buffer.getInt(48)));
                statusMap.put("userCount", Integer.reverseBytes(buffer.getInt(16)));
                statusMap.put("fpCount", Integer.reverseBytes(buffer.getInt(24)));
                statusMap.put("pwdCount", Integer.reverseBytes(buffer.getInt(52)));
                statusMap.put("oplogCount", Integer.reverseBytes(buffer.getInt(40)));
                statusMap.put("attlogCount", Integer.reverseBytes(buffer.getInt(32)));
                statusMap.put("fpCapacity", Integer.reverseBytes(buffer.getInt(56)));
                statusMap.put("userCapacity", Integer.reverseBytes(buffer.getInt(60)));
                statusMap.put("attlogCapacity", Integer.reverseBytes(buffer.getInt(64)));
                statusMap.put("remainingFp", Integer.reverseBytes(buffer.getInt(68)));
                statusMap.put("remainingUser", Integer.reverseBytes(buffer.getInt(72)));
                statusMap.put("remainingAttlog", Integer.reverseBytes(buffer.getInt(76)));
                statusMap.put("faceCount", Integer.reverseBytes(buffer.getInt(80)));
                statusMap.put("faceCapacity", Integer.reverseBytes(buffer.getInt(88)));
                return statusMap;
            }
        }

        return Collections.emptyMap();
    }

    public ZKCommandReply setStartVerify() throws IOException {
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_STARTVERIFY, this.sessionId, this.replyNo, (byte[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public Date getDeviceTime() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_GET_TIME, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        GetTimeReply gettime = new GetTimeReply(replyCode, this.sessionId, replyId, payloads);
        return gettime.getDeviceDate();
    }

    public int getState() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_STATE_RRQ, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        String payloadsStr = HexUtils.bytesToHex(payloads);
        System.out.println(payloadsStr);
        return 0;
    }

    public ZKCommandReply cancelEnrollment() throws IOException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_CANCELCAPTURE, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)(byteToSend & 255);
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply syncTime() throws IOException {
        long encodedTime = HexUtils.convertToSeconds();
        int[] timeBytes = HexUtils.convertLongToLittleEndian(encodedTime);
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_SET_TIME, this.sessionId, this.replyNo, timeBytes);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)(byteToSend & 255);
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);

        try {
            System.out.println(HexUtils.extractDate(encodedTime));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply delUser(int delUId) throws IOException {
        int[] delUIdArray = new int[]{delUId & 255, delUId >> 8 & 255};
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_DELETE_USER, this.sessionId, this.replyNo, delUIdArray);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)(byteToSend & 255);
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
            return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
        } else {
            System.out.println("User Not found...!");
            return null;
        }
    }

    public ZKCommandReply modifyUserInfo(UserInfo newUser) throws IOException {
        int uid = newUser.getUid();
        String userid = newUser.getUserid();
        UserRoleEnum role = newUser.getRole();
        int role1d = role.getRole();
        String password = newUser.getPassword();
        String name = newUser.getName();
        long cardno = newUser.getCardno();
        ByteBuffer commandBuffer = ByteBuffer.allocate(72).order(ByteOrder.LITTLE_ENDIAN);
        commandBuffer.putShort((short)uid);
        commandBuffer.putShort((short)role1d);
        byte[] passwordBytes = password.getBytes();
        commandBuffer.position(3);
        commandBuffer.put(passwordBytes, 0, Math.min(passwordBytes.length, 8));
        byte[] nameBytes = name.getBytes();
        commandBuffer.position(11);
        commandBuffer.put(nameBytes, 0, Math.min(nameBytes.length, 24));
        commandBuffer.position(35);
        commandBuffer.putShort((short)((int)cardno));
        commandBuffer.position(40);
        commandBuffer.putInt(0);
        byte[] userIdBytes = userid != null ? userid.getBytes() : new byte[0];
        commandBuffer.position(48);
        commandBuffer.put(userIdBytes, 0, Math.min(userIdBytes.length, 9));
        int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_USER_WRQ, this.sessionId, this.replyNo, commandBuffer.array());
        byte[] buf = new byte[toSend.length];

        for(int i = 0; i < toSend.length; ++i) {
            buf[i] = (byte)toSend[i];
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        return replyCode == CommandReplyCodeEnum.CMD_ACK_OK ? new ZKCommandReply(replyCode, this.sessionId, this.replyNo, (int[])null) : null;
    }

    public ZKCommandReply setSms(int tagp, int IDp, int validMinutesp, long startTimep, String contentp) throws IOException {
        SmsInfo newSms = new SmsInfo(tagp, IDp, validMinutesp, 0, startTimep, contentp);
        ByteBuffer commandBuffer = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        commandBuffer.put((byte)newSms.getTag());
        commandBuffer.putShort((short)newSms.getId());
        commandBuffer.putShort((short)newSms.getValidMinutes());
        commandBuffer.putShort((short)newSms.getReserved());
        if (commandBuffer.capacity() > 0) {
            System.out.println(commandBuffer.capacity());
            return null;
        } else {
            LocalDateTime localDateTime = Instant.ofEpochMilli(startTimep).atZone(ZoneId.systemDefault()).toLocalDateTime();
            commandBuffer.putInt((int)localDateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(localDateTime)));
            byte[] contentBytes = newSms.getContent().getBytes(StandardCharsets.UTF_8);
            commandBuffer.put(contentBytes, 0, Math.min(contentBytes.length, 60));
            SecurityUtils.printHexDump(commandBuffer.array());
            int[] toSend = ZKCommand.getPacketByte(CommandCodeEnum.CMD_SMS_WRQ, this.sessionId, this.replyNo, commandBuffer.array());
            byte[] buf = new byte[toSend.length];

            for(int i = 0; i < toSend.length; ++i) {
                buf[i] = (byte)toSend[i];
            }

            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
            this.socket.send(packet);
            ++this.replyNo;
            int[] response = this.readResponse();
            CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
            return new ZKCommandReply(replyCode, this.sessionId, this.replyNo, (int[])null);
        }
    }

    public SmsInfo getSms(int smsUId) throws IOException, ParseException {
        int[] smsIdArray = new int[]{smsUId & 255, smsUId >> 8 & 255};
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_SMS_RRQ, this.sessionId, this.replyNo, smsIdArray);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)(byteToSend & 255);
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        if (response[0] + response[1] * 256 == 4993) {
            return null;
        } else {
            CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
            int replyId = response[6] + response[7] * 256;
            byte[] byteResponse = SecurityUtils.convertIntArrayToByteArray(response);
            SecurityUtils.printHexDump(byteResponse);
            int[] payloads = new int[response.length - 8];
            System.arraycopy(response, 8, payloads, 0, payloads.length);
            if (replyCode != CommandReplyCodeEnum.CMD_ACK_OK) {
                return null;
            } else {
                int tag = response[8];
                int id = (response[9] & 255) + ((response[10] & 255) << 8);
                int reserved = (response[13] & 255) << 8 | response[12] & 255;
                long startTime = (long)response[15] & 255L | ((long)response[14] & 255L) << 8 | ((long)response[13] & 255L) << 16 | ((long)response[12] & 255L) << 24;
                int validMinutes = Short.reverseBytes((short)(response[11] << 8 | response[10] & 255)) & '\uffff';
                System.out.println("Raw response[11]: " + response[12]);
                System.out.println("Raw response[10]: " + response[13]);
                System.out.println("Raw response[10]: " + response[14]);
                System.out.println("Raw response[10]: " + response[15]);
                System.out.println("Raw response[10]: " + response[16]);
                System.out.println("Raw response[10]: " + response[17]);
                System.out.println("Raw response[10]: " + response[18]);
                long encDate = ((long)response[15] & 255L) << 24 | ((long)response[14] & 255L) << 16 | ((long)response[13] & 255L) << 8 | (long)response[12] & 255L;
                System.out.println("Decoded Date: " + encDate);
                Date startDate = HexUtils.extractDate(encDate);
                int contentOffset = 19;
                byte[] contentBytes = new byte[321];

                for(int i = 0; i < 321; ++i) {
                    contentBytes[i] = (byte)(response[i + contentOffset] & 255);
                }

                String content = (new String(contentBytes, StandardCharsets.UTF_8)).trim();
                return new SmsInfo(tag, id, validMinutes, reserved, startTime, content);
            }
        }
    }

    public ZKCommandReply delSMS(int smsId) throws IOException {
        int[] delsmsIdArray = new int[]{smsId & 255, smsId >> 8 & 255};
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_DELETE_SMS, this.sessionId, this.replyNo, delsmsIdArray);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)(byteToSend & 255);
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return replyCode == CommandReplyCodeEnum.CMD_ACK_OK ? new ZKCommandReply(replyCode, this.sessionId, replyId, payloads) : new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply enrollFinger(int uid, int tempId, String userId) throws IOException {
        byte[] enroll_dat = new byte[26];
        Arrays.fill(enroll_dat, (byte)0);
        System.arraycopy(userId.getBytes(), 0, enroll_dat, 0, userId.length());
        enroll_dat[24] = (byte)tempId;
        enroll_dat[25] = 1;
        byte[] startEnrollCommand = new byte[]{97, 0, (byte)uid, (byte)(uid >> 8), (byte)tempId, (byte)(tempId >> 8)};
        byte[] combinedArray = new byte[enroll_dat.length + startEnrollCommand.length];
        System.arraycopy(enroll_dat, 0, combinedArray, 0, enroll_dat.length);
        System.arraycopy(startEnrollCommand, 0, combinedArray, enroll_dat.length, startEnrollCommand.length);
        int[] startEnrollPacket = ZKCommand.getPacketByte(CommandCodeEnum.CMD_STARTENROLL, this.sessionId, this.replyNo, combinedArray);
        byte[] buf = new byte[startEnrollPacket.length];

        for(int i = 0; i < startEnrollPacket.length; ++i) {
            buf[i] = (byte)startEnrollPacket[i];
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = Arrays.copyOfRange(response, 8, response.length);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    private void sendPacket(int[] packetData) throws IOException {
        byte[] buf = new byte[packetData.length];

        for(int i = 0; i < packetData.length; ++i) {
            buf[i] = (byte)packetData[i];
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
    }

    public ZKCommandReply testVoice(int voice) throws IOException {
        int[] voiceArray = new int[]{voice};
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_TESTVOICE, this.sessionId, this.replyNo, voiceArray);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)(byteToSend & 255);
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply RefreshData() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_REFRESHDATA, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public ZKCommandReply FreeDeviceBuffer() throws IOException, ParseException {
        int[] toSend = ZKCommand.getPacket(CommandCodeEnum.CMD_FREE_DATA, this.sessionId, this.replyNo, (int[])null);
        byte[] buf = new byte[toSend.length];
        int index = 0;

        for(int byteToSend : toSend) {
            buf[index++] = (byte)byteToSend;
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        this.socket.send(packet);
        ++this.replyNo;
        int[] response = this.readResponse();
        CommandReplyCodeEnum replyCode = CommandReplyCodeEnum.decode(response[0] + response[1] * 256);
        if (replyCode == CommandReplyCodeEnum.CMD_ACK_OK) {
        }

        int replyId = response[6] + response[7] * 256;
        int[] payloads = new int[response.length - 8];
        System.arraycopy(response, 8, payloads, 0, payloads.length);
        return new ZKCommandReply(replyCode, this.sessionId, replyId, payloads);
    }

    public void createBackup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            FileWriter writer = new FileWriter("device_backup.json");

            try {
                Map<String, Object> deviceInfoMap = new HashMap();
                deviceInfoMap.put("isOnlyRFMachine", this.IsOnlyRFMachine());
                deviceInfoMap.put("firmwareVersion", this.getFirmwareVersion());
                deviceInfoMap.put("productTime", this.getProductTime());
                deviceInfoMap.put("deviceName", this.getDeviceName());
                deviceInfoMap.put("pin2Width", this.getPIN2Width());
                deviceInfoMap.put("attendanceRecords", this.getAttendanceRecords());
                deviceInfoMap.put("isOnlyRFMachine", this.IsOnlyRFMachine());
                deviceInfoMap.put("firmwareVersion", this.getFirmwareVersion());
                deviceInfoMap.put("productTime", this.getProductTime());
                deviceInfoMap.put("deviceName", this.getDeviceName());
                deviceInfoMap.put("pin2Width", this.getPIN2Width());
                deviceInfoMap.put("showState", this.getShowState());
                deviceInfoMap.put("deviceIP", this.getDeviceIP());
                deviceInfoMap.put("devicePort", this.getDevicePORT());
                deviceInfoMap.put("commKey", this.getCommKey());
                deviceInfoMap.put("deviceId", this.getDeviceId());
                deviceInfoMap.put("isDHCP", this.iSDHCP());
                deviceInfoMap.put("dns", this.getDNS());
                deviceInfoMap.put("enableProxyServer", this.isEnableProxyServer());
                deviceInfoMap.put("proxyServerIP", this.getProxyServerIP());
                deviceInfoMap.put("proxyServerPort", this.getProxyServerPort());
                deviceInfoMap.put("daylightSavingTime", this.isDaylightSavingTime());
                deviceInfoMap.put("language", this.getLanguage());
                deviceInfoMap.put("lockPowerKey", this.isLockPowerKey());
                deviceInfoMap.put("voiceOn", this.isVoiceOn());
                deviceInfoMap.put("platform", this.getPlatform());
                deviceInfoMap.put("serialNumber", this.getSerialNumber());
                deviceInfoMap.put("mac", this.getMAC());
                deviceInfoMap.put("faceVersion", this.getFaceVersion());
                deviceInfoMap.put("fpVersion", this.getFPVersion());
                deviceInfoMap.put("oemVendor", this.getOEMVendor());
                deviceInfoMap.put("workCode", this.getWorkCode());
                deviceInfoMap.put("deviceStatus", this.getDeviceStatus());
                deviceInfoMap.put("state", this.getState());
                objectMapper.writeValue(writer, deviceInfoMap);
                System.out.println("Device information backup created successfully.");
            } catch (Throwable var6) {
                try {
                    writer.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }

                throw var6;
            }

            writer.close();
        } catch (ParseException | IOException e) {
            ((Exception)e).printStackTrace();
            System.out.println("Error creating device information backup.");
        }

    }

    public int[] readResponse() throws IOException {
        byte[] buf = new byte[1000000];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        this.socket.receive(packet);
        int[] response = new int[packet.getLength()];

        for(int i = 0; i < response.length; ++i) {
            response[i] = buf[i] & 255;
        }

        return response;
    }
}

