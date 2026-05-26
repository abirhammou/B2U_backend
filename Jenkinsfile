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