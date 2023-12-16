package ibm.mq.demo.enums;

public enum EsbServiceIdEnum {
    _000000(SystemCode.CORE, "000000", "*****查询"),
    ;

    private final SystemCode systemCode;
    private final String code;
    private final String msg;

    EsbServiceIdEnum(SystemCode systemCode, String code, String msg) {
        this.systemCode = systemCode;
        this.code = code;
        this.msg = msg;
    }

    public SystemCode getSystemCode() {
        return systemCode;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
