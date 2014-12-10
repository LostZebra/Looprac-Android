package looprac.app;

import java.io.Serializable;

/**
 * Created by XiaoYong on 2014/6/11.
 */
public class PersonInfo implements Serializable {

    private String usrName;
    private String name;
    private String phoneNumber;
    private int usrType;

    public PersonInfo(String usrName, String name, String phoneNumber, int usrType) {
        this.usrName = usrName;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.usrType = usrType;
    }

    public String getUsrName() {
        return this.usrName;
    }

    public String getName() { return this.name; }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public int getUsrType() {
        return this.usrType;
    }
}
