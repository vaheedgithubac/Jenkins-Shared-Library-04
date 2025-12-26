#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {
  config.GIT_USER  = env.GIT_USER
  config.GIT_TOKEN = env.GIT_TOKEN
  return new UpdateImageTag(this).updateImageTag(config)
}
