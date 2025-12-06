#!/usr/bin/env groovy
import com.sharedlib.EcrPush

def call(Map config = [:]) {
   return new EcrPush(this).ecrPush(config)
}
