#!/usr/bin/env groovy
def AnalysisTools(){
 dir("${WORKSPACE}/${ONESOURCE_DIR}/applications.infrastructure.services-framework.pre-silicon-triage"){
  
        sh '''
            alias dotriage='docker run -it --rm -w `pwd` -v `pwd`:`pwd` -e no_proxy=".intel.com, 10.0.0.0/8" triage-builder'
            dotriage ./build-database/generate-wiki-validation-report.py --collection "executions" --test > b.json
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
        LOG_DIR = "${WORKSPACE}/results/workload"
        ONESOURCE_DIR = "pre-silicon-triage"
        ONESOURCE_REPO =   "github.com/AdrianaDiAvi/applications.infrastructure.services-framework.pre-silicon-triage.git"
        
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
            pip3 install --upgrade pip
            pip3 install -r requirements.txt
            black .
            flake8 .
            pre-commit install
            pre-commit run --all-files

            '''
            }

            }
        
        }

        stage('Docker-compose install'){
            steps {
            
            sh '''
            sudo curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose
            docker-compose --version
            
            '''
        dir("${WORKSPACE}/${ONESOURCE_DIR}"){
            sh '''
            docker-compose up -d
            docker ps

            '''
        }
            }
        }

        stage("Backup from artifactory"){
            steps{
        dir("${WORKSPACE}/${ONESOURCE_DIR}/applications.infrastructure.services-framework.pre-silicon-triage"){
            sh '''
            curl -sSf -H "X-JFrog-Art-Api:AKCp8kq2vs8PPFLb37nAsPU7uMHMWXwqe4L2dy1DVQpc8obVMArgioc9hw3BF62XJwoKGz6qc" -O "https://ubit-artifactory-or.intel.com/artifactory/presipipeline-or-local/db-backup/builds-backup-100322_1603.tar"
            tar xvf builds-backup-100322_1603.tar
            pwd
            ls
            docker cp ./builds-backup-100322_1603 mongodb:/data/db
            '''
        }
            }
        }
        /*
        stage("Restore for the mongo db"){
            steps{
            sh '''
            docker exec --tty mongodb bash
            mongorestore --drop builds-backup-100322_1603

            '''
            }
        
        }

        stage("First function to send wiki validation"){
            steps{
        dir ("${WORKSPACE}") {
            AnalysisTools()
        }
            
            }
        
        }
    */    
}
  post {
    always {
        cleanWs()
        }
    }

}