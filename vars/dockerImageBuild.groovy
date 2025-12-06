#!/usr/bin/env groovy
import com.sharedlib.DockerImageBuild

def call(Map config = [:]) {
  return new DockerImageBuild(this).dockerImageBuild(config)
}
