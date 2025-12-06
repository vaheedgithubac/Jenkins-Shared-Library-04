#!/usr/bin/env groovy
package com.sharedlib

class SonarqubeScan implements Serializable {
    def script

    SonarqubeScan(script) { this.script = script }

    def sonarqubeScan(Map config = [:]) {

        def required = [ "SONARQUBEAPI", "SCANNER_HOME", "PROJECT_NAME", "PROJECT_KEY" ]
        required.each { key ->
            if (!config[key] || config[key].toString().trim() == "") {
                script.error "❌ SONARQUBE: Missing required parameter '${key}'"
            }
        }
           
        // Validate required parameters
        def sources = config.sources ?: "."             

        def sonarqubeAPI = config.SONARQUBEAPI
        def scannerHome  = config.SCANNER_HOME
        def projectName  = config.PROJECT_NAME
        def projectKey   = config.PROJECT_KEY
        
        script.echo "🔹 SonarQube Server: ${sonarqubeAPI}"
        script.echo "🔹 Scanner Home  : ${scannerHome}"
        script.echo "🔹 Project Name  : ${projectName}"
        script.echo "🔹 Project Key   : ${projectKey}"

        script.withSonarQubeEnv(sonarqubeAPI) {
            script.sh """
                ${scannerHome}/bin/sonar-scanner \
                -Dsonar.projectName="${projectName}" \
                -Dsonar.projectKey="${projectKey}" -X
            """
        }
        script.echo "✔ Sonarqube Scan completed Successfully"
    }
}
