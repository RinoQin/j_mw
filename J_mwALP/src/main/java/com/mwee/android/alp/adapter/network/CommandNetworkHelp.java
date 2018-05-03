package com.mwee.android.alp.adapter.network;

import com.mwee.android.alp.AlpLog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class CommandNetworkHelp {

    private static String IP = "www.baidu.com";


    public static boolean isNetworkAvailable(Object obj) {
        /*return executePing("ping -c 1 -s 32 -w 1 " + IP);*/
        return pingIpAddress(IP);
    }

    /**
     * 在linux下支持，在windows下不支持-c 1 -s 32，换命令重试
     * -c是指ping的次数为1次，-w是指超时时间（单位为s），-sping包的大小（默认是64比特）
     * @param ipAddress
     * @return
     */
    private static boolean pingIpAddress(String ipAddress) {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 -s 32 -w 1 " + ipAddress);
            int status = process.waitFor();
            if (status == 0) {
                return true;
            }if(status == 1){
                //可能是windows环境造成以上ping命令不可执行，换命令重试
                Process reprocess = Runtime.getRuntime().exec("ping -w 1"+ ipAddress);
                int restatus = reprocess.waitFor();
                if (restatus == 0) {
                    return true;
                }else{
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }



    /**
     * 在windows平台上，上面代码没有为ping ip 会结束，
     * 而在linux环境中ping命令，ping不通时会卡住，ping通，会不停的输出信息，考虑用另一种方式socket。
     * @param ip
     */
    private static void getNetworkState(String ip) {
        Runtime runtime = Runtime.getRuntime();
        try {
            AlpLog.i("=================正在测试网络连通性ip："+ip);
            Process process = runtime.exec("ping " +ip);
            InputStream iStream = process.getInputStream();
            InputStreamReader iSReader = new InputStreamReader(iStream,"UTF-8");
            BufferedReader bReader = new BufferedReader(iSReader);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = bReader.readLine()) != null) {
                sb.append(line);
            }
            iStream.close();
            iSReader.close();
            bReader.close();
            String result = new String(sb.toString().getBytes("UTF-8"));
            AlpLog.i("ping result:"+result);
            if (result!=null && (result.trim()).length()>0) {
                if (result.indexOf("TTL") > 0 || result.indexOf("ttl") > 0) {
                    AlpLog.i("网络正常，时间: " + System.currentTimeMillis());
                } else {
                    AlpLog.i("网络断开，时间 :" + System.currentTimeMillis());

                }
            }
        } catch (Exception e) {
            AlpLog.e("网络异常：",e);
            e.printStackTrace();
        }

    }







    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_EXIT = "exit\n";

    /**
     * 执行单条命令
     *
     * @param command
     * @return
     */
    public static boolean executePing(String command) {
        boolean isPing = false;
        int status = -1;
        if (command == null || command.length() == 0) {
            return false;
        }
        AlpLog.d("execute command start : " + command);
        Process process = null;
        BufferedReader successReader = null;
        BufferedReader errorReader = null;
        StringBuilder errorMsg = null;

        DataOutputStream dos = null;
        try {
            // TODO
            process = Runtime.getRuntime().exec(COMMAND_SH);
            dos = new DataOutputStream(process.getOutputStream());
            dos.write(command.getBytes());
            dos.writeBytes(COMMAND_LINE_END);
            dos.flush();
            dos.writeBytes(COMMAND_EXIT);
            dos.flush();

            status = process.waitFor();

            errorMsg = new StringBuilder();
            successReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String lineStr;
            while ((lineStr = successReader.readLine()) != null) {

                AlpLog.d(" command line item : " + lineStr);
            }
            while ((lineStr = errorReader.readLine()) != null) {
                errorMsg.append(lineStr);
            }
            if (status == 0) {
                isPing= true;
            } else {
                isPing= false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (successReader != null) {
                    successReader.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        AlpLog.d(String.format(Locale.CHINA,
                "execute command end,errorMsg:%s,and status %d: ", errorMsg, status));
        return isPing;
    }

}
