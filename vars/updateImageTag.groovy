#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {
  return new UpdateImageTag(this).updateImageTag(config)
}
