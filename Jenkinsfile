node{
    
    def mavenHome
    def mavenCMD
    def docker
    def dockerCMD
    def tagName
    
    stage('prepare enviroment'){
        echo 'initialize all the variables'
        mavenHome = tool name: 'mymaven' , type: 'maven'
        mavenCMD = "${mavenHome}/bin/mvn"
        tagName="3.0"
    }
    
    stage('git code checkout'){
        try{
            echo 'checkout the code from git repository'
            git 'https://github.com/joshiharish417/star-agile-insurance-project.git'
        }
        catch(Exception e){
            echo 'Exception occured in Git Code Checkout Stage'
            currentBuild.result = "FAILURE"
            emailext body: '''Dear All,
            The Jenkins job ${JOB_NAME} has been failed. Request you to please have a look at it immediately by clicking on the below link. 
            ${BUILD_URL}''', subject: 'Job ${JOB_NAME} ${BUILD_NUMBER} is failed', to: 'joshiharish417@gmail.com'
        }
    }
    
    stage('Build the Application'){
        echo "Cleaning... Compiling...Testing... Packaging..."
        //sh 'mvn clean package'
        sh "${mavenCMD} clean package"        
    }
    
    stage('publish test reports'){
        publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: '/var/lib/jenkins/workspace/Capstone-Project-Live-Demo/target/surefire-reports', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: '', useWrapperFileDirectly: true])
    }
    
    stage('Containerize the application'){
        echo 'Creating Docker image'
        sh "docker build -t joshiharish417/insure-me:${tagName} ."
    }
    
    stage('Pushing it ot the DockerHub'){
        echo 'Pushing the docker image to DockerHub'
        withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
        sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}"
        sh "docker push joshiharish417/insure-me:${tagName}"
            
        }
        
    stage('Configure and Deploy to the test-server'){
                withCredentials([sshUserPrivateKey(credentialsId: 'Testenv_Key', keyFileVariable: 'EC2_SSH_KEY_TEST')]) {
                    ansiblePlaybook(
                        installation: 'ansible',
                        playbook: 'ansible-playbook.yml',
                        inventory: 'inventory.ini',
                        disableHostKeyChecking: true,
                        become: true,
                        becomeUser: 'root',
                        executable: '/usr/bin/ansible-playbook',
                        extras: "--private-key=${EC2_SSH_KEY_TEST} -e env=test -e docker_image=joshiharish417/insure-me:${tagName}"
                    )
                }
            
    }
        
        
    }
}




