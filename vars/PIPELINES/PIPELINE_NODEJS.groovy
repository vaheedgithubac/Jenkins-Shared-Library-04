def call(Map config = [:]) {
	pipeline {

		agent any 

		options { 
        	skipDefaultCheckout true
        	disableConcurrentBuilds()
        	timeout(time: 30, unit: 'MINUTES')
        	timestamps() 
        	ansiColor('xterm')                 // plugin: "AnsiColor"
        }

       environment {
		    NEXUS_ARTIFACT_VERSION = "${BUILD_ID}-${BUILD_TIMESTAMP}"  // Requires Build Timestamp plugin
	   }

	   stages {
	   		stage("INIT") { steps { cleanWs() } }
		   
		    stage("Setting My Own ENV Vars") {
				steps {
					script {
						env.MY_GIT_LATEST_COMMIT_ID = ''
						env.DOCKER_IMAGE = ''
					}
				}
			}

	   		stage("GIT CHECKOUT") {
	   			steps {
					script {
	   					//env.MY_GIT_LATEST_COMMIT_ID = getLatestCommitIdShort() ( To get this work, you should not declare a variable under pipeline environment{} block )
						// MY_GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
						
						if (config.EXECUTE_GITCHECKOUT_STAGE?.trim().equalsIgnoreCase("yes")) {
							env.MY_GIT_LATEST_COMMIT_ID = gitCheckout([MY_GIT_URL: config.MY_GIT_URL, MY_GIT_REPO_TYPE: config.MY_GIT_REPO_TYPE])
	   						echo "MY_GIT_LATEST_COMMIT_ID: ${env.MY_GIT_LATEST_COMMIT_ID}"
						}
					}
	   			}
	   		}

	   		stage("TRIVY FILE SYSTEM SCAN") {
			   steps {
				   script { 
				   		if (config.EXECUTE_TRIVY_FS_STAGE?.trim().equalsIgnoreCase("yes")) {
				   			echo "Running... TRIVY FILE SYSTEM SCAN"
					   		trivyScan([
					   			MODE:                    "fs",
					   			TARGET:                  config.TRIVY_FS_TARGET ?: ".",
								SCAN_FORMAT:             config.TRIVY_FS_SCAN_FORMAT,
								OUTPUT_FORMAT:           config.TRIVY_FS_OUTPUT_FORMAT,
					   			PROJECT_NAME:            config.PROJECT_NAME,
					   			COMPONENT:               config.COMPONENT,
					   			MY_GIT_LATEST_COMMIT_ID: env.MY_GIT_LATEST_COMMIT_ID	
					   		])
					   	} else { echo "Skipping...STAGE - TRIVY FILE SYSTEM SCAN" }
			       	}
	           	}
		   	}

		    stage("SONARQUBE SCAN - SAST") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_SONARSCAN_STAGE?.trim().equalsIgnoreCase("yes")) {
				   			echo "Running... SONARQUBE SCAN - SAST"
					   		sonarqubeScan([
					   			SONARQUBE_SERVER: config.SONARQUBE_SERVER,
					   			SONAR_SCANNER_NAME: config.SONAR_SCANNER_NAME,  
					   			PROJECT_NAME: config.PROJECT_NAME,
					   			PROJECT_KEY:  config.PROJECT_KEY
					   		])
					   	} else { echo "Skipping...STAGE - SONARQUBE SCAN - SAST" }
		   			}
		   		}
		    }

		    stage("SONARQUBE QUALITY GATE") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_SONAR_QG_STAGE?.trim().equalsIgnoreCase("yes")) {
				   			echo "Running... SONARQUBE QUALITY GATE"
					   		sonarqubeQG([TIMEOUT_MINUTES: config.TIMEOUT_MINUTES])
					   	} else { echo "Skipping...STAGE - SONARQUBE QUALITY GATE" }

		   			}
		   		}
		    }

		    stage("BUILD DOCKER IMAGE") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_DOCKER_IMAGE_BUILD_STAGE?.trim().equalsIgnoreCase("yes")) {
		   					echo "Running...BUILD DOCKER IMAGE"
		   					env.DOCKER_IMAGE = dockerImageBuild([
		   						PROJECT_NAME: 			 config.PROJECT_NAME,
		   						COMPONENT: 				 config.COMPONENT,
		   						MY_GIT_LATEST_COMMIT_ID: env.MY_GIT_LATEST_COMMIT_ID,
								DOCKER_CONTEXT:          config.DOCKER_CONTEXT
		   					])
		   					echo "IMAGE BUILT SUCCESSFULLY: ${DOCKER_IMAGE}"
		   				} else { echo "Skipping... STAGE - BUILD DOCKER IMAGE" }
		   			}
		   		}
		    }

		    stage("DOCKER IMAGE SCAN - TRIVY") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_TRIVY_IMAGE_STAGE?.trim().equalsIgnoreCase("yes")) {
		   					echo ("Running...DOCKER IMAGE SCAN - TRIVY")
		   					trivyScan([
					   			MODE:                    "image",
					   			TARGET:                  env.DOCKER_IMAGE,
								SCAN_FORMAT:             config.TRIVY_IMAGE_SCAN_FORMAT,
								OUTPUT_FORMAT:           config.TRIVY_IMAGE_OUTPUT_FORMAT,
					   			PROJECT_NAME:            config.PROJECT_NAME,
					   			COMPONENT:               config.COMPONENT,
					   			MY_GIT_LATEST_COMMIT_ID: env.MY_GIT_LATEST_COMMIT_ID
					   		])
					    } else { echo "Skipping... STAGE - DOCKER IMAGE SCAN - TRIVY" }
		   			}
		   		}
		    }

		    stage("NEXUS ARTIFACT UPLOAD") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_NEXUS_STAGE?.trim().equalsIgnoreCase("yes")) {
		   					if (configMap.NEXUS_CREDENTIALS_ID?.trim()) {
    							echo "Nexus credentials ID is provided: ${config.NEXUS_CREDENTIALS_ID}"
		   						withCredentials([usernamePassword(
                            		credentialsId: config.NEXUS_CREDENTIALS_ID, 
                            		usernameVariable: 'TMP_NEXUS_USER', 
                            		passwordVariable: 'TMP_NEXUS_PASSWORD'
                            	)]) {
                            			//Assign to ENV variables
                            			env.NEXUS_USER = TMP_NEXUS_USER
                            			env.NEXUS_PASSWORD = TMP_NEXUS_PASSWORD
                               		}
                            }
                            // echo " NEXUS_USER: ${nexus_user} NEXUS_PASSWORD: ${nexus_password}"
                            echo "Running...NEXUS ARTIFACT UPLOAD"
		   					nexusUpload([
					            NEXUS_VERSION:          config.NEXUS_VERSION,
					            NEXUS_PROTOCOL:         config.NEXUS_PROTOCOL,
					            NEXUS_HOST:             config.NEXUS_HOST,
					            NEXUS_PORT:             config.NEXUS_PORT,
					            NEXUS_GRP_ID:           config.NEXUS_GRP_ID,
					            NEXUS_ARTIFACT_VERSION: "${env.MY_GIT_LATEST_COMMIT_ID}-${NEXUS_ARTIFACT_VERSION}",
					            NEXUS_CREDENTIALS_ID:   config.NEXUS_CREDENTIALS_ID,
								NEXUS_BASE_REPO:        config.NEXUS_BASE_REPO
          					])
		   				}  else { echo "Skipping... STAGE - NEXUS ARTIFACT UPLOAD"}	
		   			}
		   		}
		    }

		    stage("DOCKER IMAGE UPLOAD - DOCKER HUB") {
		   		steps {
		   			script {
                        if (config.EXECUTE_DOCKER_HUB_PUSH_STAGE?.trim().equalsIgnoreCase("yes")) {
		   					echo "Running...DOCKER IMAGE UPLOAD - DOCKER HUB"
		   					dockerPush([
		   						DOCKER_IMAGE:              env.DOCKER_IMAGE,
		   						DOCKER_REGISTRY_URI:       config.DOCKER_REGISTRY_URI,
		   						DOCKER_HUB_CREDENTIALS_ID: config.DOCKER_HUB_CREDENTIALS_ID
		   					])
		   				} else { echo "Skipping... STAGE - DOCKER IMAGE UPLOAD - DOCKER HUB" }
		   			}
		   		}
		    }

		    stage("DOCKER IMAGE UPLOAD - ECR") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_ECR_PUSH_STAGE?.trim().equalsIgnoreCase("yes")) {
		   					echo "Running...DOCKER IMAGE UPLOAD - ECR"
		   					ecrPush([
		   						DOCKER_IMAGE:       env.DOCKER_IMAGE,
		   						ECR_REGISTRY_URI:   config.ECR_REGISTRY_URI,
								REGION:             config.REGION,
		   						AWS_CREDENTIALS_ID: config.AWS_CREDENTIALS_ID
		   					])
		   				} else { echo "Skipping... STAGE - DOCKER IMAGE UPLOAD - ECR" }
		   			}
		   		}
		    }

		   stage("UPDATE_IMAGE_TAG_GITHUB") {
			   steps {
				   script {
					   if ("yes".equalsIgnoreCase(config.EXECUTE_UPDATE_IMAGE_TAG_GITHUB_STAGE?.trim())) {
						   withCredentials([
                        		usernamePassword(
                            		credentialsId: config.GIT_DEPLOY_HTTPS_CREDS,
                            		usernameVariable: 'GIT_USER',
                            		passwordVariable: 'GIT_TOKEN'
                        		)
                    		]) {
                        		updateImageTag(
                            		DOCKER_IMAGE:            env.DOCKER_IMAGE,
									MY_GIT_LATEST_COMMIT_ID: env.MY_GIT_LATEST_COMMIT_ID,
									GIT_USER:                env.GIT_USER,
									GIT_TOKEN:               env.GIT_TOKEN,
									GIT_REPO_NAME:           config.GIT_REPO_NAME,
									GIT_BRANCH_NAME:         env.BRANCH_NAME,
									VERSION_CONTROL_SYSTEM:  config.VERSION_CONTROL_SYSTEM,
                            		DEPLOYMENT_FILE:         config.DEPLOYMENT_FILE,
									HELM_VALUES_FILE:        config.HELM_VALUES_FILE
                        		)
                    		}
					   } else { echo "Skipping... Stage - UPDATE_IMAGE_TAG_GITHUB (flag not set to 'yes')" }
				   }
			   }
		   }

		   
	   } // stages

	   post {
	   		always {
	   			script {
	   				if (config.EXECUTE_EMAIL_STAGE.toLowerCase()?.trim() == "yes") {
	   					echo "Sending Email"
	   					sendEmail([
			                 JOB_NAME:        env.JOB_NAME,
			                 BUILD_NUMBER:    env.BUILD_NUMBER,
			                 BUILD_URL:       env.BUILD_URL,
			                 BRANCH_NAME:     env.BRANCH_NAME,
			                 PIPELINE_STATUS: currentBuild.currentResult,
			                 DURATION:        currentBuild.durationString,
			                 FROM_MAIL:       config.FROM_MAIL,
			                 TO_MAIL:         config.TO_MAIL,
			                 REPLY_TO_MAIL:   config.REPLY_TO_MAIL,
			                 CC_MAIL:         config.CC_MAIL,
			                 BCC_MAIL:        config.BCC_MAIL,
			                 ATTACHMENTS:     config.ATTACHMENTS                      // "trivy-reports/*, owasp-reports/*"
			            ])               
	   				} else { echo "Skipping... POST - STAGE - Sending Mail" }
	   			}
	   		}
	   }


    } // pipeline

} // def
