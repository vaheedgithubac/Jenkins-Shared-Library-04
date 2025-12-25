// DOCKER BUILD Variables
EXECUTE_DOCKER_IMAGE_BUILD_STAGE: "yes",   // required (yes/no)
############################################################################################################################
stage("BUILD DOCKER IMAGE") {
                steps {
                    script {
                        if (config.EXECUTE_DOCKER_IMAGE_BUILD_STAGE.equalsIgnoreCase("yes")) {
                            echo "Running...BUILD DOCKER IMAGE"
                            env.DOCKER_IMAGE = dockerImageBuild([
                                PROJECT_NAME:            config.PROJECT_NAME,
                                COMPONENT:               config.COMPONENT,
                                MY_GIT_LATEST_COMMIT_ID: env.MY_GIT_LATEST_COMMIT_ID
                            ])
                            echo "IMAGE BUILT SUCCESSFULLY: ${DOCKER_IMAGE}"
                        } else { echo "Skipping... STAGE - BUILD DOCKER IMAGE" }
                    }
                }
            }

##########################################################################################################################
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

                
        // Echo for debugging
        script.echo "🔨 Building Docker Image: ${dockerImage}"

        script.echo "Docker Context : ${dockerContext}"
        script.echo "Dockerfile     : ${dockerFile}"
        script.echo "currentWorkingDirectory = ${script.pwd()}"     // Returns Jenkins workspace

        // Build the Docker image
        def status = script.sh(
            script: "docker build . -t ${dockerImage}",
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
#########################################################################    frontend    #########################################################################################
# docker build app-code/frontend -t frontend-img:1.0.0 -f Docker/Dockerfile
FROM nginx:stable-alpine

# Remove default server and default index
RUN rm -f \
    /usr/share/nginx/html/index.html \
    /etc/nginx/conf.d/default.conf

# Prepare directories for non-root nginx
RUN mkdir -p \
    /var/cache/nginx/client_temp \
    /var/cache/nginx/proxy_temp \
    /var/cache/nginx/fastcgi_temp \
    /var/cache/nginx/uwsgi_temp \
    /var/cache/nginx/scgi_temp \
    /run \
    /etc/nginx/ssl \
  && chown -R nginx:nginx \
    /var/cache/nginx \
    /var/log/nginx \
    /etc/nginx \
    /run \
  && chmod 755 /etc/nginx

# Copy custom NGINX config
COPY files/expense.conf /etc/nginx/conf.d/expense.conf

# Ensure config file permissions
RUN chmod 644 /etc/nginx/conf.d/*.conf

# Copy frontend files
COPY code/. /usr/share/nginx/html/

EXPOSE 8080

USER nginx

CMD ["nginx", "-g", "daemon off;"]
#################################################################################   backend  ##################################################################################################
# docker build app-code/backend -t backend-img:1.0.0 -f Docker/Dockerfile
#FROM node:20
FROM node:20.18.3-alpine3.21 AS builder
WORKDIR /opt/backend
COPY code/package.json ./
COPY code/*.js ./
RUN npm install


FROM node:20.18.3-alpine3.21
RUN addgroup -S expense && adduser -S expense -G expense && \
    mkdir /opt/backend && \
    chown -R expense:expense /opt/backend
ENV DB_HOST         # ="mysql"
WORKDIR /opt/backend
USER expense
COPY --from=builder /opt/backend /opt/backend
CMD ["node", "index.js"]
#################################################################################   database  ##################################################################################################
# docker build app-code/database -t database-img:1.0.0 -f Docker/Dockerfile
FROM mysql:8.0
# ENV MYSQL_ROOT_PASSWORD=ExpenseApp@1
COPY files/mysql_scripts/*.sql /docker-entrypoint-initdb.d
