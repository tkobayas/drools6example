package com.sample;

import java.io.Serializable;

@org.kie.api.remote.Remotable
public class MyPojo implements Serializable {

    private String name;
    
    public MyPojo() {
        // TODO Auto-generated constructor stub
    }
    
    public MyPojo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
