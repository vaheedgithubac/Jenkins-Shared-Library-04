#!/usr/bin/env groovy
import com.sharedlib.JacocoCodeCoverage

def call(Map config = [:]) {
    return new JacocoCodeCoverage(this).jacocoCodeCoverage(config) 
}
