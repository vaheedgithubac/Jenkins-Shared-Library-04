#!/usr/bin/env groovy
import com.sharedlib.SonarqubeScan

def call(Map config = [:]) {
  return new SonarqubeScan(this).sonarqubeScan(config)
}
