pipeline {
    agent any

    triggers {
        cron('H 6 * * *')
    }

    environment {
        CONFLUENCE_BASE_URL  = credentials('CONFLUENCE_BASE_URL')
        CONFLUENCE_EMAIL     = credentials('CONFLUENCE_EMAIL')
        CONFLUENCE_API_TOKEN = credentials('CONFLUENCE_API_TOKEN')
        CONFLUENCE_PAGE_ID   = credentials('CONFLUENCE_PAGE_ID')
        JAVA_HOME            = '/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home'
        PATH                 = "${JAVA_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean jar'
            }
        }

        stage('Run Karate Tests') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Publish to Confluence') {
            steps {
                sh '''
                    java -jar build/libs/karate-api-automation-1.0-SNAPSHOT.jar \
                    build/karate-reports/features.users.json
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed — Confluence page updated successfully.'
        }
        failure {
            echo 'Pipeline failed — check the logs above for details.'
        }
        always {
            archiveArtifacts artifacts: 'build/karate-reports/**/*', allowEmptyArchive: true
        }
    }
}