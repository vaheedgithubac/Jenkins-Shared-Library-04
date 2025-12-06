#!/usr/bin/env groovy
package com.sharedlib

class JacocoCodeCoverage implements Serializable {
	def script

	JacocoCodeCoverage(script) { this.script = script }

	def jacocoCodeCoverage(Map config = [:]) {

		def required = ["JACOCO_GROUPID", "JACOCO_ARTIFACT_ID", "JACOCO_VERSION", "JACOCO_GOAL"]
	    required.each { key ->
	        if (!config[key] || config[key]?.toString().trim() == "") {
	           script.error "❌ JACOCO CODE COVERAGE: Missing required parameter '${key}'"
	        }
	    }

	    def jacocoGroupId    = config.JACOCO_GROUPID
		def jacocoArtifactId = config.JACOCO_ARTIFACT_ID
		def jacocoVersion    = config.JACOCO_VERSION
		def jacocoGoal       = config.JACOCO_GOAL

		def jacocoCmd = "${jacocoGroupId}:${jacocoArtifactId}:${jacocoVersion}:${jacocoGoal}"
    	script.echo "Running Jacoco step: ${jacocoCmd}"

		// mvn clean test jacoco:prepare-agent jacoco:report   or  mvn clean test ${jacocoCmd} jacoco:report

		try { script.sh """ 
                 mvn ${jacocoCmd}
                 mvn clean test jacoco:report
             """ 
        }
    	catch (Exception ex) { script.error "❌ Jacoco Maven step failed: ${ex.message}" }
		script.echo "✔ JACOCO CODE COVERAGE completed Successfully"
	}
}
