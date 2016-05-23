package org.vchat.Model;

/**
 * Author: SACHIN
 * Date: 5/21/2016.
 */
public class Friends {

    private int memberOne;
    private int memberTwo;
    private int status;

    public Friends(int memberOne, int memberTwo, int status) {
        this.memberOne = memberOne;
        this.memberTwo = memberTwo;
        this.status = status;
    }

    public int getMemberOne() {
        return memberOne;
    }

    public void setMemberOne(int memberOne) {
        this.memberOne = memberOne;
    }

    public int getMemberTwo() {
        return memberTwo;
    }

    public void setMemberTwo(int memberTwo) {
        this.memberTwo = memberTwo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
