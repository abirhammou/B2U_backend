pipeline {
    agent any
    tools {
        maven 'Maven 3.9'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('Tests') {
            steps {
                sh 'mvn test'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectKey=B2U-backend \
                    -Dsonar.host.url=http://localhost:9000 \
                    -Dsonar.login=23037d72020b43477019c785868f97599fab79bd
                '''
            }
        }
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t b2u-backend:latest .'
            }
        }
        stage('Docker Run') {
            steps {
                sh '''
                    docker stop b2u-backend || true
                    docker rm b2u-backend || true
                    docker run -d --name b2u-backend \
                      --network b2u-network \
                      -p 8081:8080 \
                      b2u-backend:latest
                '''
            }
        }
    }
    post {
        success {
            echo '✅ Pipeline succeeded!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}