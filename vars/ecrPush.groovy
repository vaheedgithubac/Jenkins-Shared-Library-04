#!/usr/bin/env groovy
import com.sharedlib.DockerPush

def call(Map config = [:]) {
   return new EcrPush(this).ecrPush(config)
}
