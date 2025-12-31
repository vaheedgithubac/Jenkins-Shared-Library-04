#!/usr/bin/env groovy
package com.sharedlib

class DockerImageBuild implements Serializable {

    def script

    DockerImageBuild(script) {
        this.script = script
    }

    def dockerImageBuild(Map config = [:]) {

        // ✅ Required parameters
        def required = ["PROJECT_NAME", "COMPONENT", "MY_GIT_LATEST_COMMIT_ID"]
        required.each { key ->
            if (!config[key] || config[key].toString().trim() == "") {
                script.error("❌ DOCKER IMAGE BUILD: Missing required parameter '${key}'")
            }
        }

        // Project / image info
        def projectName = config.PROJECT_NAME
        def component   = config.COMPONENT
        def imageTag    = config.MY_GIT_LATEST_COMMIT_ID
        def dockerImage = "${projectName}-${component}:${imageTag}" 

        def dockerContext         = config.DOCKER_CONTEXT ?: "."
        //def dockerContextFullPath = "${script.pwd()}/${dockerContext}"
        def dockerContextFullPath = "${script.env.WORKSPACE}/${dockerContext}"
        def dockerFile            = config.DOCKERFILE ?: "Dockerfile"
        def dockerFileFullPath    = "${dockerContextFullPath}/${dockerFile}" 
        
        script.echo """
        📄 Docker Build Details:
            Docker Context            : ${dockerContext}
            Docker Context Full Path  : ${dockerContextFullPath}
            DockerFile                : ${dockerFile}
            DockerFile Full Path      : ${dockerFileFullPath}
        """
        
        // Check Dockerfile exists relative to workspace
        if (!script.fileExists(dockerFileFullPath)) {
            script.error("❌ 'Dockerfile' not found at ${dockerFullContextPath}")
        }
        
        // Echo for debugging
        script.echo "🔨 Building Docker Image: ${dockerImage}"
        
        // Build the Docker image
        def status = script.sh(
            script: "docker build ${dockerContextFullPath} -t ${dockerImage} -f ${dockerFileFullPath}",
            returnStatus: true
        )

        if (status == 0) {
            script.echo "✔ Docker image '${dockerImage}' built successfully."
            return dockerImage
        } else {
            script.error("❌ Docker build failed for image: ${dockerImage}")
        }
    }
}
