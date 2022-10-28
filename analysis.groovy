def validation_kpi(flag_validation_kpi){
  checkout([$class: 'GitSCM', doGenerateSubmoduleConfigurations: false,
    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "${Workspace}"]],
    submoduleCfg: [], branches: [[name: 'development']], userRemoteConfigs: [[credentialsId: 'one-source-token-personal',
    url: 'https://github.com/AdrianaDiAvi/test.pipeline.analysis.sf.git']]])
 dir("${WORKSPACE}"){
  sh "python3 send-data-wiki.py ${flag_validation_kpi} "
  echo "${flag_validation_kpi}"
  }
}

return this