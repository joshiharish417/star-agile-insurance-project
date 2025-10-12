node{
    
    def mavenHome
    def mavenCMD
    def docker
    def dockerCMD
    def tagName
    def PATH
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
        echo "Test report path: ${env.WORKSPACE}/target/surefire-reports"
        publishHTML([
        allowMissing: false,
        alwaysLinkToLastBuild: false,
        keepAll: false,
        reportDir: "${env.WORKSPACE}/target/surefire-reports",
        reportFiles: 'index.html',
        reportName: 'HTML Report',
        reportTitles: '',
        useWrapperFileDirectly: true
        ])
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
        
    stage('Configure and Deploy to the test-server') {
        withCredentials([sshUserPrivateKey(credentialsId: 'Testenv_Key', keyFileVariable: 'EC2_SSH_KEY_TEST')]) {
        try {
            ansiblePlaybook(
                installation: 'ansible',
                playbook: 'ansible-playbook.yml',
                inventory: 'inventory.ini',
                disableHostKeyChecking: true,
                become: true,
                becomeUser: 'root',
                extras: "--private-key=${EC2_SSH_KEY_TEST} -e env=test -e docker_image=joshiharish417/insure-me:${tagName}"
            )
            echo "Ansible playbook succeeded"
            } catch (Exception e) {
                echo "Ansible playbook failed with: ${e.getMessage()}"
                error("Stopping pipeline due to Ansible failure")
                }
            }
        }

        stage('Run Selenium Tests on EC2') {
            withCredentials([sshUserPrivateKey(credentialsId: 'Testenv_Key', keyFileVariable: 'EC2_SSH_KEY')]) {
                sh '''
                    echo "Copying JAR to EC2..."
                    scp -i $EC2_SSH_KEY -o StrictHostKeyChecking=no selenium-insure-me-runnable.jar ubuntu@13.126.40.86:/home/ubuntu/
        
                    echo "Running Selenium tests on EC2..."
                    ssh -i $EC2_SSH_KEY -o StrictHostKeyChecking=no ubuntu@13.126.40.86 \
                    "java -Dwebdriver.chrome.driver=/usr/bin/chromedriver -jar /home/ubuntu/selenium-insure-me-runnable.jar http://localhost:8080/"
                '''
            }
        }


        stage('Deploy to Production') {
            input message: 'Selenium tests passed. Proceed to production deployment?'
            
            withCredentials([sshUserPrivateKey(credentialsId: 'Prodenv_Key', keyFileVariable: 'EC2_SSH_KEY_PROD')]) {
                ansiblePlaybook(
                    installation: 'ansible',
                    playbook: 'ansible-playbook.yml',
                    inventory: 'prod_inventory.ini',
                    disableHostKeyChecking: true,
                    become: true,
                    becomeUser: 'root',
                    extras: "--private-key=${EC2_SSH_KEY_PROD} -e env=prod -e docker_image=joshiharish417/insure-me:${tagName}"
                )
            }
        }
      
        
    }
}




