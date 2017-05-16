#!/bin/bash

set -e
set -x

if [[ -z "$SKIP_GIT_SYNC" ]]
then
  mkdir -p /root/.ssh

  printf "Host github.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config

  cp /ssh/id_rsa /root/.ssh/id_rsa
  chmod 0600 /root/.ssh/id_rsa

  if [ -d "esms-ai-data/.git" ]; then
    echo "Pulling latest data..."
    cd esms-ai-data
    git reset --hard HEAD
    git clean -fd || true
    git pull --rebase
    cd ..
  else
    echo "Cloning latest data..."
    rm -rf esms-ai-data
    git clone git@github.com:lstephen/esms-ai-data.git
  fi
fi

echo "Running..."
mvn -B exec:java

if [[ -z "$SKIP_GIT_SYNC" ]]
then

  echo "Updating data..."
  cd esms-ai-data

  [[ -n "$GIT_AUTHOR_NAME" ]] && git config user.name $GIT_AUTHOR_NAME
  [[ -n "$GIT_AUTHOR_EMAIL" ]] && git config user.email $GIT_AUTHOR_EMAIL

  git add --all
  git commit -m "$(date)"
  git pull --rebase
  git push origin master
fi

echo "Done."

