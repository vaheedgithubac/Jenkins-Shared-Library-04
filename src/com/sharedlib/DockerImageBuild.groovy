#!/usr/bin/env groovy
package com.sharedlib

class BuildDockerImage implements Serializable {
	def script

	BuildDockerImage(script) { this.script = script }

	def buildDockerImage(String dockerImage) {

    	script.echo "🔨 Building Docker Image: ${dockerImage}"

    	def status = script.sh(
        	script: "docker build . -t ${dockerImage}",
        	returnStatus: true
    	)

    	if (status == 0) {
        	script.echo "✔ Docker image '${dockerImage}' built successfully."
        	return dockerImage
    	} else {
        	script.echo "❌ Docker build failed for image: ${dockerImage}"
        	script.error("Stopping build because Docker image failed.")
       	}
	}
}
