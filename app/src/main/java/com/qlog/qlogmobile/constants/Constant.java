package com.qlog.qlogmobile.constants;

public class Constant {
    public static final String URL = "http://192.168.254.106/";
    public static final String HOME = URL+"api";
    public static final String LOGIN = HOME+"/login";
    public static final String LOGOUT = HOME+"/logout";
    public static final String PURPOSES = HOME+"/purposes";
    public static final String FACILITIES = HOME+"/facilities";
    public static final String DEPARTMENTS = FACILITIES+"/departments";
    public static final String USERS = HOME+"/users";
    public static final String LOGS = HOME+"/logs";
    public static final String ADD_LOG = LOGS+"/create";
    public static final String WALK_IN = LOGS+"/walk-in";
}
