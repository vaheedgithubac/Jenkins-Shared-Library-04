#!/usr/bin/env groovy
package com.sharedlib

class JacocoCodeCoverage implements Serializable {
	def script

	JacocoCodeCoverage(script) { this.script = script }

	def jacocoCodeCoverage(Map config = [:]) {

		def required = ["JACOCO_GROUPID", "JACOCO_ARTIFACT_ID", "JACOCO_VERSION", "JACOCO_GOAL"]
    	required.each { key ->
        	if (!config[key] || config[key].trim() == "") {
            	error "❌ JACOCO: Missing required parameter '${key}'"
        	}
    	}

    	def jacocoCmd = "${config.JACOCO_GROUPID}:${config.JACOCO_ARTIFACT_ID}:${config.JACOCO_VERSION}:${config.JACOCO_GOAL}"

    	script.echo "⏳ Running Jacoco step: ${jacocoCmd}..."

    	try { script.sh """ 
                 	mvn ${jacocoCmd}
                 	mvn clean test jacoco:report
              		""" 
        } catch (Exception ex) { error "❌ Jacoco Maven step failed: ${ex.message}" }

    	echo "✔ JACOCO CODE COVERAGE completed Successfully"

    	// sh 'mvn org.jacoco:jacoco-maven-plugin:0.8.7:prepare-agent'
	}
}
