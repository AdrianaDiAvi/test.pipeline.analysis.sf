#!/usr/bin/env groovy

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
            
            git clone --branch master https://AdrianaDiAvi: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@${ONESOURCE_REPO} ${ONESOURCE_DIR}
            git remote add upstream https://github.com/AdrianaDiAvi/applications.infrastructure.services-framework.pre-silicon-triage.git
            dir("${WORKSPACE}/pre-silicon-triage"){
  
            python3 -m venv .my_env
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

        stage('Docker-compose install'){
            steps {
            
            sh '''
            sudo curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose
            docker-compose --version
            docker-compose up -d

            '''
            }
        }

        stage("Backup from artifactory"){
            steps{
            sh '''
            curl -sSf -H "X-JFrog-Art-Api:AKCp8kq2vs8PPFLb37nAsPU7uMHMWXwqe4L2dy1DVQpc8obVMArgioc9hw3BF62XJwoKGz6qc" -O "https://ubit-artifactory-or.intel.com/artifactory/presipipeline-or-local/db-backup/builds-backup-100322_1603.tar"
            tar xvf builds-backup-100322_1603.tar
            docker cp /home/adiazavi/applications.infrastructure.services-framework.pre-silicon-triage/builds-backup-100322_1603 mongodb:/data/db
            '''
            }
        }
        
        stage("Restore for the mongo db"){
            steps{
            sh '''
            docker exec -ti mongodb bash
            mongorestore --drop builds-backup-100322_1603

            '''
            }
        
        }
        
}
  post {
    always {
        cleanWs()
        }
    }

}