package com.njkremer.Sqlite.TestClass;

public class UserAccessGroup {

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public long getGroupId() {
        return groupId;
    }
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
    
    private long userId;
    private long groupId;
}
