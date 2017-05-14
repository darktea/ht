package com.my.ht.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArgsServices {

    @Autowired
    public ArgsServices(ApplicationArguments args) {
        this.port = getOptionByName(args, "port");
    }

    private String getOptionByName(ApplicationArguments args, String optionName) {
        List<String> optionValues = args.getOptionValues(optionName);
        if (optionValues != null && optionValues.size() == 1) {
            return optionValues.get(0);
        }

        return "";
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    private String port;

}
