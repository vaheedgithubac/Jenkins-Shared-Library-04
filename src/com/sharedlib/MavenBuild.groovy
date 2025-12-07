#!/usr/bin/env groovy
package com.sharedlib

class MavenBuild implements Serializable {
    def script

    MavenBuild(script) { this.script = script }

    def mavenBuild(Map config = [:]) {

        def required = ["MAVEN_SKIP_TESTS"]
        required.each { key ->
            if (!config[key] || config[key]?.toString().trim() == "") {
                script.error "❌ MAVEN BUILD: Missing required parameter '${key}'"
            }
        }
                        
        // Convert MAVEN_SKIP_TESTS to boolean safely
        def mavenSkipTests = config.MAVEN_SKIP_TESTS?.toString()?.toBoolean() ?: false

        // Accept MAVEN_GOALS or default
        def mavenGoals = config.MAVEN_GOALS ?: "clean package"
         
        /*
        def maven_skip_tests = ((config.MAVEN_SKIP_TESTS in [true, 'true', "true"]) ? 'true' : 'false') 
        def maven_goals = config.maven_goals ?: "clean package"
        */

        script.echo "🚀 Running Maven build"
        script.echo "⚙️  Goals     : ${mavenGoals}"
        script.echo "🧪 Skip tests: ${mavenSkipTests}"

        
        try {
            script.sh """
                mvn "${mavenGoals} -DskipTests=${mavenSkipTests}"
            """
        } catch (Exception ex) {
            script.error "❌ Maven Build failed: ${ex.message}"
          }

        script.echo "✔ Maven Build Completed Successfully"
    }
}
