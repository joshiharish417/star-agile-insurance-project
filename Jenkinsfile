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
    withCredentials([sshUserPrivateKey(credentialsId: 'Testenv_Key', keyFileVariable: 'SSH_KEY')]) {
        ansiblePlaybook(
            installation: 'ansible',
            playbook: 'ansible-playbook.yml',
            inventory: 'inventory.ini',
            disableHostKeyChecking: true,
            become: true,
            becomeUser: 'root',
            extras: "--private-key=${SSH_KEY} -e env=test -e docker_image=joshiharish417/insure-me:${tagName}"
        )
    }
}

    stage('Run UI Tests on EC2') {
        withCredentials([sshUserPrivateKey(credentialsId: 'Testenv_Key', keyFileVariable: 'SSH_KEY')]) {
            sh '''
                set -x
                chmod 600 ${SSH_KEY}
                echo "[STEP] Creating deploy_ui_tests.sh script"
                cat > /tmp/deploy_ui_tests.sh << 'EOF'
                #!/bin/bash
                set -e
                echo "[STEP] Creating project directory"
                mkdir -p /home/ubuntu/star-agile-insurance-project/
                # Chrome is already installed, skip installation
                echo "[STEP] Downloading and installing ChromeDriver"
                wget -q https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/141.0.7390.76/linux64/chromedriver-linux64.zip
                unzip -o chromedriver-linux64.zip
                sudo mv chromedriver-linux64/chromedriver /usr/bin/chromedriver
                sudo chmod +x /usr/bin/chromedriver
                echo "[STEP] Cleaning up project directory"
                rm -rf /home/ubuntu/star-agile-insurance-project/*
                echo "[STEP] Setup script completed"
                EOF

                echo "[STEP] Copying setup script to EC2"
                scp -o StrictHostKeyChecking=no -o ConnectTimeout=30 -i ${SSH_KEY} /tmp/deploy_ui_tests.sh ubuntu@13.126.40.86:/tmp/
                echo "[STEP] Running setup script on EC2"
                ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 -i ${SSH_KEY} ubuntu@13.126.40.86 "chmod +x /tmp/deploy_ui_tests.sh && /tmp/deploy_ui_tests.sh"
                echo "[STEP] Copying project to EC2"
                scp -o StrictHostKeyChecking=no -o ConnectTimeout=30 -i ${SSH_KEY} -r . ubuntu@13.126.40.86:/home/ubuntu/star-agile-insurance-project/
                echo "[STEP] Running UI tests on EC2"
                ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 -i ${SSH_KEY} ubuntu@13.126.40.86 "\
                    cd /home/ubuntu/star-agile-insurance-project && \
                    export TEST_ENV_URL=http://13.126.40.86:8080 && \
                    export CHROMEDRIVER_PATH=/usr/bin/chromedriver && \
                    export CHROME_BINARY_PATH=/usr/bin/google-chrome && \
                    timeout 600 mvn test -Dtest=InsureMeUITest\
                "
            '''
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
