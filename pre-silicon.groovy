#!/usr/bin/env groovy
def AnalysisTools(){
 dir("${WORKSPACE}/${ONESOURCE_DIR}/applications.infrastructure.services-framework.pre-silicon-triage"){
  
        sh '''
            alias dotriage='docker run -it --rm -w `pwd` -v `pwd`:`pwd` -e no_proxy=".intel.com, 10.0.0.0/8" triage-builder'
            dotriage ./build-database/generate-wiki-validation-report.py --collection "executions" --test > test1.json
        '''
    
  }
}

pipeline {
    agent {
        label 'service-fw'
    }
    options {
        timestamps()
    }
        environment {
        GITHUB_CREDS = credentials('one-source-token-personal')
        ONESOURCE_DIR = "pre-silicon-triage"
        ONESOURCE_REPO =   "github.com/AdrianaDiAvi/applications.infrastructure.services-framework.pre-silicon-triage.git"
        ARTIFACTORY_CREDS = credentials('artifactory-token')
        ARTIFACTORY_REPO = "https://ubit-artifactory-or.intel.com/artifactory/presipipeline-or-local"
        
    }

    parameters {
        choice(name: 'VERSION',
         choices: ['21.39', '21.47', '21.52', '22.05', '22.13', '22.27', '22.33', '22.44', '22.50','22.54', '22.60'],
         description:'Services Framework Version')
        choice(name: 'TOOL',
         choices: ['Validation Report Wiki', 'KPI Report Wiki', 'Validation Report Cumulus'],
         description:'Services Framework Tools')
    }
    stages {
        stage('Setup Repo Triage'){
            steps {
            
            sh '''

            mkdir ${ONESOURCE_DIR}
            git clone https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@${ONESOURCE_REPO} ${ONESOURCE_DIR}
            '''
        
        dir("${WORKSPACE}/${ONESOURCE_DIR}"){

            sh '''
            python3 -m venv .venv/
            source .venv/bin/activate
            curl -sSf -H "X-JFrog-Art-Api:${ARTIFACTORY_CREDS}" -O ${ARTIFACTORY_REPO}/docker-triage.tar
            docker load -i docker-triage.tar
            docker images
            docker ps
            docker image tag 6ed8a78f6466  triage-builder:latest
            docker images
            '''
            }
            input('Do you want to proceed')
            }
        
        }

        stage("First function to send wiki validation"){
            steps{
        
            AnalysisTools()
            input('Do you want to proceed')

            
            }
        
        }
    
}
  post {
    always {
        cleanWs()
        }
    } 

}