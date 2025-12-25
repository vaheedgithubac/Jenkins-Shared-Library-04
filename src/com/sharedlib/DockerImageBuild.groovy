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

        // Docker build context & Dockerfile
        def dockerContext = config.DOCKER_CONTEXT
        def dockerFile    = config.DOCKERFILE ?: "Dockerfile"
        def dockerContextFullPath = "${script.pwd()}/${dockerContext}" ?: "." 
        def dockerFileFullPath    = "${dockerContextFullPath}/${dockerFile}"

        // Echo for debugging
        script.echo "🔨 Building Docker Image: ${dockerImage}"
        script.echo "Docker Context : ${dockerContext}"
        script.echo "Dockerfile     : ${dockerFile}"
        script.echo "currentWorkingDirectory = ${script.pwd()}"     // Returns Jenkins workspace

        // Check Dockerfile exists relative to workspace
        //if (!script.fileExists(dockerFile)) {
        //    script.error("❌ Dockerfile not found at ${dockerFile}")
        //}

        // Build the Docker image
        def status = script.sh(
            script: "docker build ${dockerContextFullPath} -t ${dockerImage} -f ${dockerFileFullPath} ",
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
