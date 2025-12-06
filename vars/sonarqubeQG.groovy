#!/usr/bin/env groovy
import com.sharedlib.SonarqubeQG

def call(Map config = [:]) {
    return new SonarqubeQG(this).sonarqubeQG(config)
}
