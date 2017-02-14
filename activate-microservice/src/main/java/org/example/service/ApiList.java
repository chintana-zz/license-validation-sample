package org.example.service;

public class ApiList {
    public int getCount() {
        return count;
    }

    public ApiInfo[] getList() {
        return list;
    }

    private int count;

    private ApiInfo[] list;

    public String getApiID(String name) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].getName().equals(name)) {
                return list[i].getId();
            }
        }
        return "";
    }
}
