package org.vchat.Model;

/**
 * Author: SACHIN
 * Date: 5/21/2016.
 */
public class Member {

    private String userName;
    private String status;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;

    public Member(String userName, String status) {
        this.userName = userName;
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
