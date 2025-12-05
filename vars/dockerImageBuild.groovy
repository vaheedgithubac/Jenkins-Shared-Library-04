#!/usr/bin/env groovy
import com.sharedlib.DockerImageBuild

def call(Map config = [:]) {
	def required = ["PROJECT_NAME", "COMPONENT", "MY_GIT_LATEST_COMMIT_ID"]
    required.each { key ->
        if (!config[key]) {
           error "❌ DOCKER IMAGE BUILD: Missing required parameter '${key}'"
        }
    }

  def projectName   = config.PROJECT_NAME
  def component     = config.COMPONENT
  def imageTag      = config.MY_GIT_LATEST_COMMIT_ID

  def dockerImage   = "${projectName}-${component}:${imageTag}"
  def call(String dockerImage) { return new DockerImageBuild(this).buildDockerImage(dockerImage) }
}

