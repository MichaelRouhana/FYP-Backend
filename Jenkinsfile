pipeline {
    agent any

    environment {
        VERSION = "latest"
    }

    tools {
        maven 'Maven 3.x' 
    }

    stages {
        stage('Checkout') {
            steps {
                // FIXED: Removed the hardcoded token from the URL.
                // You must configure 'github_credentials' in Jenkins UI later.
                git branch: 'main',
                    credentialsId: 'github_credentials',
                    url: 'https://github.com/MichaelRouhana/FYP-Backend.git'
            }
        }

        stage('Set Version') {
            steps {
                script {
                    // Generates a version based on git tags, defaults to 'latest'
                    def ref = sh(script: "git describe --tags --exact-match 2>/dev/null || echo latest", returnStdout: true).trim()
                    env.VERSION = ref
                    echo "Using version: ${env.VERSION}"
                }
            }
        }

        stage('Build with Maven') {
            steps {
                sh "${tool 'Maven 3.x'}/bin/mvn -B package --file pom.xml"
                sh 'ls -l target/'
            }
        }

        stage('Setup Docker') {
            steps {
                sh 'docker --version'
                // Installs docker-compose if missing
                sh 'docker-compose --version || (curl -L "https://github.com/docker/compose/releases/download/v2.23.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose)'
            }
        }

        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker_credentials1', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh 'echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker_credentials1', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh """
                        docker build -t $DOCKER_USERNAME/erp:$VERSION .
                        docker tag $DOCKER_USERNAME/erp:$VERSION $DOCKER_USERNAME/erp:latest
                        docker push $DOCKER_USERNAME/erp:$VERSION
                        docker push $DOCKER_USERNAME/erp:latest
                    """
                }
            }
        }

        stage('Deploy to Azure') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'docker_credentials1', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD'),
                    string(credentialsId: 'VM_HOST', variable: 'VM_HOST'),
                    string(credentialsId: 'VM_USERNAME', variable: 'VM_USERNAME'),
                    string(credentialsId: 'VM_PASSWORD', variable: 'VM_PASSWORD')
                ]) {
                    // Added SCP to move the docker-compose file to the server before running it
                    script {
                         sh "sshpass -p '$VM_PASSWORD' scp -o StrictHostKeyChecking=no docker-compose.light.yml $VM_USERNAME@$VM_HOST:~/"
                         sh """
                            sshpass -p "$VM_PASSWORD" ssh -o StrictHostKeyChecking=no $VM_USERNAME@$VM_HOST '
                                echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin &&
                                docker pull $DOCKER_USERNAME/erp:latest &&
                                docker-compose -f ~/docker-compose.light.yml up -d
                            '
                        """
                    }
                }
            }
        }
    }
}