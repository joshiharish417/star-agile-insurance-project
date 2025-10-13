node {

    def mavenHome
    def mavenCMD
    def tagName

    stage('Prepare Environment') {
        echo 'Initializing all variables...'
        mavenHome = tool name: 'mymaven', type: 'maven'
        mavenCMD = "${mavenHome}/bin/mvn"
        tagName = "3.0"
    }

    stage('Git Checkout') {
        try {
            echo 'Checking out source code from GitHub...'
            git 'https://github.com/joshiharish417/star-agile-insurance-project.git'
        } catch (Exception e) {
            echo "❌ Exception occurred in Git checkout: ${e.getMessage()}"
            currentBuild.result = "FAILURE"
            emailext(
                subject: "Job ${JOB_NAME} ${BUILD_NUMBER} failed at checkout",
                to: 'joshiharish417@gmail.com',
                body: "Dear Harish,<br>The Jenkins job <b>${JOB_NAME}</b> failed during Git checkout.<br><br>See details: <a href='${BUILD_URL}'>${BUILD_URL}</a>"
            )
            error("Stopping pipeline")
        }
    }

    stage('Build the Application') {
        echo "Cleaning, compiling, and packaging..."
        // Runs only unit tests (not UI)
        sh "${mavenCMD} clean package"
    }
    
    stage('Publish Test Reports') {
        echo "Publishing test reports..."
        publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: false,
            keepAll: false,
            reportDir: "${env.WORKSPACE}/target/surefire-reports",
            reportFiles: 'index.html',
            reportName: 'Unit Test Report'
        ])
    }

    stage('Containerize the Application') {
        echo "🐳 Creating Docker image..."
        sh "docker build -t joshiharish417/insure-me:${tagName} ."
    }

    stage('Push to DockerHub') {
        echo "📦 Pushing Docker image to DockerHub..."
        withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}"
            sh "docker push joshiharish417/insure-me:${tagName}"
        }
    }

    stage('Deploy to Test Environment') {
        echo "🚀 Deploying to test environment using Ansible..."
        withCredentials([sshUserPrivateKey(credentialsId: 'Testenv_Key', keyFileVariable: 'EC2_SSH_KEY_TEST')]) {
            ansiblePlaybook(
                installation: 'ansible',
                playbook: 'ansible-playbook.yml',
                inventory: 'inventory.ini',
                disableHostKeyChecking: true,
                become: true,
                becomeUser: 'root',
                extras: "--private-key=${EC2_SSH_KEY_TEST} -e env=test -e docker_image=joshiharish417/insure-me:${tagName}"
            )
        }
    }

    stage('Run Selenium UI Tests') {
        echo "🧪 Running Selenium UI Tests on Test Environment..."
        withCredentials([sshUserPrivateKey(credentialsId: 'Testenv_Key', keyFileVariable: 'EC2_SSH_KEY_TEST')]) {
            sshagent(credentials: ['Testenv_Key']) {
                // Replace the URL below with your test environment URL or domain
                def TEST_ENV_URL = "http://13.126.40.86:8080"

                sh """
                    echo "Installing Chrome + ChromeDriver (if not already installed)..."
                    ssh -o StrictHostKeyChecking=no -i ${EC2_SSH_KEY_TEST} ubuntu@13.126.40.86 '
                        sudo apt update &&
                        sudo apt install -y google-chrome-stable ||
                        echo "Chrome already installed"
                    '
                    ssh -o StrictHostKeyChecking=no -i ${EC2_SSH_KEY_TEST} ubuntu@13.126.40.86 '
                        sudo apt install -y unzip wget &&
                        wget -q https://chromedriver.storage.googleapis.com/114.0.5735.90/chromedriver_linux64.zip &&
                        unzip -o chromedriver_linux64.zip &&
                        sudo mv chromedriver /usr/bin/ &&
                        sudo chmod +x /usr/bin/chromedriver
                    '

                    echo "Running UI Tests on EC2 via Maven Failsafe..."
                    ssh -o StrictHostKeyChecking=no -i ${EC2_SSH_KEY_TEST} ubuntu@13.126.40.86 '
                        cd /home/ubuntu/star-agile-insurance-project &&
                        export TEST_ENV_URL=${TEST_ENV_URL} &&
                        mvn verify -DskipUnitTests=true
                    '
                """
            }
        }
    }

    stage('Deploy to Production') {
        input message: '✅ Selenium tests passed. Proceed to Production Deployment?'
        echo "🚀 Deploying to Production..."
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
