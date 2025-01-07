package cn.moongn.coworkhub.common.constant;

public class Constants {
    
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    
    public static final Integer STATUS_NORMAL = 1;
    public static final Integer STATUS_DISABLE = 0;
    
    public static final String DEFAULT_PASSWORD = "123456";
    
    public static class RoleType {
        public static final Integer ADMIN = 0;
        public static final Integer LEADER = 1;
        public static final Integer EMPLOYEE = 2;
    }
    
    public static class TaskStatus {
        public static final Integer ASSIGNED = 0;
        public static final Integer IN_PROGRESS = 1;
        public static final Integer COMPLETED = 2;
        public static final Integer TESTING = 3;
        public static final Integer CLOSED = 4;
        public static final Integer PAUSED = 5;
    }
    
    public static class IssueStatus {
        public static final Integer ASSIGNED = 0;
        public static final Integer IN_PROGRESS = 1;
        public static final Integer RESOLVED = 2;
        public static final Integer CLOSED = 3;
    }
} 