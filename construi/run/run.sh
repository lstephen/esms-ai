#!/bin/bash

set -e
set -x

if [[ -z "$SKIP_GIT_SYNC" ]]
then
  if [ -d "esms-ai-data/.git" ]; then
    echo "Pulling latest data..."
    pushd $ESMSAI_DATA
    git reset --hard HEAD
    git clean -fd || true
    git pull --rebase
    popd
  else
    echo "Cloning latest data..."
    find $ESMSAI_DATA -mindepth 1 -delete
    git clone "https://${GITHUB_TOKEN}@github.com/lstephen/esms-ai-data.git" $ESMSAI_DATA
  fi
fi

echo "Running..."
mvn -B exec:java

if [[ -z "$SKIP_GIT_SYNC" ]]
then

  echo "Updating data..."
  cd $ESMSAI_DATA

  [[ -n "$GIT_AUTHOR_NAME" ]] && git config user.name $GIT_AUTHOR_NAME
  [[ -n "$GIT_AUTHOR_EMAIL" ]] && git config user.email $GIT_AUTHOR_EMAIL

  if [[ $(git status --porcelain) ]]; then
    git add --all
    git commit -m "$(date)"
    git pull --rebase
    git push origin master
  fi
fi

echo "Done."

