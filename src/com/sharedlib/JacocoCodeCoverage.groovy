#!/usr/bin/env groovy
package com.sharedlib

class JacocoCodeCoverage implements Serializable {
	def script

	JacocoCodeCoverage(script) { this.script = script }

	def jacocoCodeCoverage(Map config = [:]) {

		def required = ["JACOCO_GROUPID", "JACOCO_ARTIFACT_ID", "JACOCO_VERSION", "JACOCO_GOAL"]
    	required.each { key ->
        	if (!config[key] || config[key].trim() == "") {
            	script.error "❌ JACOCO: Missing required parameter '${key}'"
        	}
    	}

	    def jacoco_groupId     = config.JACOCO_GROUPID.trim()
	    def jacoco_artifactId  = config.JACOCO_ARTIFACT_ID.trim()
	    def jacoco_version     = config.JACOCO_VERSION.trim()
	    def jacoco_goal        = config.JACOCO_GOAL.trim()

	    script.echo "✔ JACOCO_GROUPID     = ${jacoco_groupId}"
	    script.echo "✔ JACOCO_ARTIFACT_ID = ${jacoco_artifactId}"
	    script.echo "✔ JACOCO_VERSION     = ${jacoco_version}"
	    script.echo "✔ JACOCO_GOAL        = ${jacoco_goal}"

    	def jacocoCmd = "${jacoco_groupId}:${jacoco_artifactId}:${jacoco_version}:${jacoco_goal}"

        try {
            def status = script.sh(returnStatus: true, 
            					   script: """ mvn clean test ${jacocoCmd} jacoco:report """
            					   )

            if (status != 0) { script.error "❌ Jacoco Maven step failed (exit code: ${status})" }

        } catch (Exception ex) { script.error "❌ Jacoco Maven step failed: ${ex.message}" }

    	script.echo "✔ JACOCO CODE COVERAGE completed Successfully"


    	/*
    	script.echo "⏳ Running Jacoco step: ${jacocoCmd}..."
        try { script.sh """ 
                 	mvn ${jacocoCmd}
                 	mvn clean test jacoco:report
              		""" 
        } catch (Exception ex) { script.error "❌ Jacoco Maven step failed: ${ex.message}" }
        */

    	// script.sh 'mvn org.jacoco:jacoco-maven-plugin:0.8.7:prepare-agent'
	}
}
