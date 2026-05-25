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



        stage('Clone Frontend') {
            steps {
                dir('frontend') {
                    git branch: 'main',
                        url: 'https://github.com/Mouhib223/B2U-HUB.git'
                }
                echo '✅ Frontend cloné'
            }
        }
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
    }
    post {
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}