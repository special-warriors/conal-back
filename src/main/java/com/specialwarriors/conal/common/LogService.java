package com.specialwarriors.conal.common;

import org.springframework.stereotype.Service;

@Service
public class LogService {

    public String getClassName() {

        return getClass().getSimpleName();
    }
}
