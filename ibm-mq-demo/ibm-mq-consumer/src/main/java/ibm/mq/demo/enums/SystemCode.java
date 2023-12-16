package ibm.mq.demo.enums;


public enum SystemCode {
    //
    CORE("core", "0000", "", "核心系统"),
    ;
    private final String sysCode;
    private final String sysNum;
    private final String channelCode;
    private final String sysName;

    SystemCode(String sysCode, String sysNum, String channelCode, String sysName) {
        this.sysCode = sysCode;
        this.sysNum = sysNum;
        this.channelCode = channelCode;
        this.sysName = sysName;
    }

    public String getSysCode() {
        return sysCode;
    }

    public String getSysNum() {
        return sysNum;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getSysName() {
        return sysName;
    }
}
