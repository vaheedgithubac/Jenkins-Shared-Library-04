#!/usr/bin/env groovy
package com.sharedlib

class DockerPush implements Serializable {
	def script

	DockerPush(script) { this.script = script }

	def dockerPush(Map config = [:]) {

		def required = ["DOCKER_IMAGE", "DOCKER_HUB_CREDENTIALS_ID", "DOCKER_REPO_URI"]
    	required.each { key ->
        	if (!config[key]) {
            	script.error "❌ DOCKER REGISTRY: Missing required parameter '${key}'"
        	}
    	}

    	def dockerImage   = config.DOCKER_IMAGE
    	def credentialsId = config.DOCKER_HUB_CREDENTIALS_ID
    	def dockerRepoUri = config.DOCKER_REPO_URI ?: "docker.io"   // optional, default to Docker Hub

    	// Use withCredentials to inject Docker credentials securely
	    script.withCredentials([script.usernamePassword(
	        credentialsId: credentialsId,
	        usernameVariable: 'DOCKER_USER',
	        passwordVariable: 'DOCKER_PASS'
	    )]) {

	        // Tag the Docker image
	        script.sh """
	            echo "🔖 Tagging Docker Image"
	            docker tag ${dockerImage} ${DOCKER_USER}/${dockerImage}
	        """

	        // Login to Docker Hub
	        script.sh """
	            set +x
	            echo "🔐 Logging into Docker Hub as '${DOCKER_USER}'"
	            echo "\${DOCKER_PASS}" | docker login -u "\${DOCKER_USER}" --password-stdin
	            # echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin 
	            set -x
	        """

	        // Push the image
	        script.sh """
	            echo "🚀 Pushing Docker Image to Docker Hub"
	            docker push ${DOCKER_USER}/${dockerImage}
	            echo "✔ Pushed Docker Image Successfully"
	        """

	        // Logout from Docker Hub
	        script.sh """
	            docker logout
	            echo "✔ Logged out from Docker Hub Successfully"
	        """
	    }
	}
}
