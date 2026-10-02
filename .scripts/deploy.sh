#!/bin/bash
set -e

echo "Deployment started..."

# Reset local changes and pull the latest version of the app
echo "Resetting local changes..."
# GIT_SSH_COMMAND="ssh -i ~/.ssh/lms_ed25519" git fetch origin
GIT_SSH_COMMAND="ssh -i ~/.ssh/tutorio-backend" git reset --hard origin/main
GIT_SSH_COMMAND="ssh -i ~/.ssh/tutorio-backend" git pull
echo "New changes copied to server!"

# echo rm database.mv.db
# rm database.mv.db

# echo rm database.trace.db
# rm database.trace.db

# Build the project using Gradle
sudo chmod +x gradlew
echo "Making build by using Gradle..."
./gradlew build

echo restart tutorio.service

systemctl restart tutorio.service

echo "Deployment Finished!"

