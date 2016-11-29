package com.sample;

import java.io.Serializable;

@org.kie.api.remote.Remotable
public class MyPojo2 implements Serializable {

    private String name;
    
    public MyPojo2() {
        // TODO Auto-generated constructor stub
    }
    
    public MyPojo2(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
