#!/usr/bin/env groovy
package com.sharedlib

class DockerImageBuild implements Serializable {
	def script

	DockerImageBuild(script) { this.script = script }

	def dockerImageBuild(Map config = [:]) {

		def required = ["PROJECT_NAME", "COMPONENT", "MY_GIT_LATEST_COMMIT_ID"]
	    required.each { key ->
	        if (!config[key] || config[key]?.toString().trim() == "") {
	           error "❌ DOCKER IMAGE BUILD: Missing required parameter '${key}'"
	        }
	    }

	    def projectName   = config.PROJECT_NAME
		def component     = config.COMPONENT
		def imageTag      = config.MY_GIT_LATEST_COMMIT_ID
		def dockerImage   = "${projectName}-${component}:${imageTag}"

    	script.echo "🔨 Building Docker Image: ${dockerImage}"

    	def status = script.sh(
        	script: "docker build . -t ${dockerImage}",
        	returnStatus: true
    	)
		
        # def output = script.sh(script: "docker build . -t ${dockerImage}", returnStdout: true).trim()
		
    	if (status == 0) {
        	script.echo "✔ Docker image '${dockerImage}' built successfully."
        	return dockerImage
    	} else {
        	script.echo "❌ Docker build failed for image: ${dockerImage}"
        	script.error("Stopping build because building Docker image failed.")
       	}
	}
}
