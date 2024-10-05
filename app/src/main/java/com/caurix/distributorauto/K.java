package com.caurix.distributorauto;

public class K {
    // #143#1*nnnnnnnn*TTTTTT*PPPPP#
    public static String SERVER_URL_BASE = "http://www.caurix.net";
    //	public static String SERVER_URL_BASE = "http://192.168.50.1:8585";
    public static String APPLICATION_NAME = "";
    //	public static String APPLICATION_NAME = "/caurix_portal";
    public static String DISTRIBUTOR_SERVICE_URL = SERVER_URL_BASE
            + APPLICATION_NAME + "/controller/DistributorTestController.php?cmd=";

    public static final String FMT_DIALLER = "service call phone 2 s16 \"%s\"",
            LOGTAG = "DistPro",
            ARG_MONTH = "arg_month",
            ARG_YEAR = "arg_year",
            ARG_TOTAL_MONTH = "arg_total_mon",
            INT_NEWSMSTRX = "com.caurix.NEWSMSTRX",
            AMT = "AMT",
            SEC = "SEC",
            PHONE = "PHONE",

    // FORMAT_USSD_CASH_IN = "#145#1*%s*%s*%s#",// target phone, amount,
    // secret // original

    ///////////////  OPERATOR 1 Orange ///////////////

    FORMAT_USSD_CASH_IN = String.format("#145#1*%s*%s*%s#", PHONE, AMT,
            SEC),// target phone, amount, secret

    FORMAT_USSD_CREDIT = String.format("#145#7*1*%s*%s*%s#",   PHONE,AMT,
            SEC),// target phone, amount, secret


    // FORMAT_USSD_CASH_OUT = "#145#2*%s*%s*%s#",// original USSD
    // command
    FORMAT_USSD_CASH_OUT = String.format("#145#2*1*%s*%s*%s#", PHONE,
            AMT, SEC), // number, amount, secret // test command 144

    DATEFORMAT = "yyyy-MM-dd HH:mm:ss",
            KEY_COMMISSION_AMT = "key_comm", KEY_SDNUMBER = "key_sdnum",
            KEY_SDSECRET = "key_sdsec";


}
