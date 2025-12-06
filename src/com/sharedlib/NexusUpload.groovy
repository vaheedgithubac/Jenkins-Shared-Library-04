#!/usr/bin/env groovy
package com.sharedlib

class NexusUpload implements Serializable {
    def script

    NexusUpload(script) { this.script = script }

    def nexusUpload(Map config = [:]) {

        //----------------------------------------------------
        // Validate required parameters
        //----------------------------------------------------
        def required = [
            "NEXUS_VERSION", 
            "NEXUS_PROTOCOL", 
            "NEXUS_HOST", 
            "NEXUS_PORT",  
            "NEXUS_GRP_ID", 
            "NEXUS_ARTIFACT_VERSION", 
            "NEXUS_CREDENTIALS_ID", 
            "NEXUS_BASE_REPO"
        ]
        required.each { key ->
            if (!config[key] || config[key]?.toString().trim() == "") {
                script.error "❌ NEXUS: Missing required parameter '${key}'"
            }
        }

        //----------------------------------------------------
        // Assign Parameters to local variables
        //----------------------------------------------------
        def nexus_version          = config.NEXUS_VERSION
        def nexus_protocol         = config.NEXUS_PROTOCOL
        def nexus_host             = config.NEXUS_HOST
        def nexus_port             = config.NEXUS_PORT
        def nexus_grp_id           = config.NEXUS_GRP_ID
        def nexus_artifact_version = config.NEXUS_ARTIFACT_VERSION
        def nexus_credentials_id   = config.NEXUS_CREDENTIALS_ID
        def nexus_base_repo        = config.NEXUS_BASE_REPO
           
        //----------------------------------------------------
        // Read pom.xml
        //----------------------------------------------------
        def pom = script.readMavenPom file: "pom.xml"

        def pom_artifactId  = pom.getArtifactId()                   // pom.artifactId
        def pom_version     = pom.getVersion()                      // pom.version
        def pom_name        = pom.getName() ?: pom.getArtifactId()  // pom.name ?: pom_artifactId
        def pom_groupId     = pom.getGroupId()                      // pom.groupId
        def pom_packaging   = pom.getPackaging()                    // pom.packaging


        //----------------------------------------------------
        // Find artifacts
        //----------------------------------------------------
        def filesByGlob = script.findFiles(glob: "target/*.${pom_packaging}")

        if (!filesByGlob || filesByGlob.size() == 0) {
            script.error "❌ No artifact found in target/ directory with extension *.${pom_packaging}"
        }

        def artifactPath = filesByGlob[0].path;
        def artifactExists = script.fileExists(artifactPath)
        def final_nexus_repo = pom_version.endsWith("SNAPSHOT") ? "${nexus_base_repo}-SNAPSHOT" : "${nexus_base_repo}-RELEASE"

        script.echo """
        📄 Artifact Details:
            Path         : ${artifactPath}
            Name         : ${filesByGlob[0].name}
            Is Directory : ${filesByGlob[0].directory}
            Length       : ${filesByGlob[0].length}
            Modified     : ${filesByGlob[0].lastModified}
        📤 Uploading to Nexus repository: ${final_nexus_repo}
        """

        //----------------------------------------------------
        // Upload artifact
        //----------------------------------------------------

        if (artifactExists) {
            script.nexusArtifactUploader(
                nexusVersion: nexus_version,
                protocol: nexus_protocol,
                nexusUrl: "${nexus_host}:${nexus_port}",
                groupId: nexus_grp_id,                               
                version: nexus_artifact_version,
                repository: final_nexus_repo, 
                credentialsId: nexus_credentials_id,

                artifacts: [
                   [artifactId: pom_artifactId,
                    classifier: '',
                    file: artifactPath,
                    type: pom_packaging]
                ]
            )
            script.echo "✔ Nexus upload successfully completed"
        } 
        else { script.error "❌ File: ${artifactPath}, could not be found" }
    }
}
